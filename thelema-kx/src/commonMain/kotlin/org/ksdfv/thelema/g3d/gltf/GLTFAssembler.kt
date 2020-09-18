/*
 * Copyright 2020 Anton Trushkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ksdfv.thelema.g3d.gltf

import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.ext.contains
import org.ksdfv.thelema.g3d.*
import org.ksdfv.thelema.g3d.node.ITransformNode
import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.IMesh
import kotlin.math.max
import kotlin.math.min

/** @author zeganstyl */
open class GLTFAssembler(open var gltf: IGLTF) {
    /** Some attributes in glTF have predefined names, like POSITION, NORMAL, TANGENT and etc.
     * So this mapping must map some attribute name to glTF name if this attribute have same semantics,
     * otherwise attribute name will be as is.
     *
     * See [attribute names in glTF 2.0](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#meshes) */
    val attributesMapping: MutableMap<String, String> = HashMap()

    /** For each buffer view must be function, that will fill piece of buffer.
     * Key - buffer view index, value - function that must fill buffer */
    val bufferViewBytesProvider = HashMap<Int, () -> Unit>()

    fun createBuffer() {
        var bytesNum = 0
        for (i in gltf.bufferViews.indices) {
            val gltfBufferView = gltf.bufferViews[i]
            bytesNum += gltfBufferView.byteLength
        }

        if (gltf.name.isEmpty()) throw IllegalStateException("GLTF name must be set")

        val gltfBuffer = GLTFBuffer(
            gltf = gltf,
            elementIndex = 0,
            uri = "${gltf.name}.bin",
            bytes = DATA.bytes(bytesNum),
            byteLength = bytesNum
        )
        gltf.buffers.add(gltfBuffer)

        for (i in gltf.bufferViews.indices) {
            bufferViewBytesProvider[i]?.invoke()
        }
    }

    fun addPrimitive(primitive: IMesh, meshIndex: Int) {
        val gltf = gltf

        val gltfMesh = gltf.meshes[meshIndex]

        val gltfPrimitive = GLTFPrimitive(gltf, gltfMesh.primitives.size, meshIndex, primitive)
        gltfMesh.primitives.add(gltfPrimitive)

        gltfPrimitive.name = primitive.name

        // vertices
        val vertices = primitive.vertices
        if (vertices != null) {
            val floatView = vertices.bytes.floatView()
            val attributesMap = gltfPrimitive.attributesMap

            val vertexInputs = vertices.vertexInputs
            val vertexBytes = vertices.bytes
            val bytesPerVertex = vertexInputs.bytesPerVertex
            val verticesNum = vertices.size

            for (i in vertexInputs.indices) {
                val vertexInput = vertexInputs[i]
                val inputSizeInBytes = vertexInput.sizeInBytes

                val lastView = gltf.bufferViews.lastOrNull()

                val bufferViewIndex = gltf.bufferViews.size
                val gltfBufferView = GLTFBufferView(
                    gltf = gltf,
                    elementIndex = bufferViewIndex,
                    buffer = 0,
                    byteLength = verticesNum * vertexInput.sizeInBytes,
                    byteOffset = if (lastView != null) lastView.byteOffset + lastView.byteLength else 0
                )
                gltf.bufferViews.add(gltfBufferView)

                val accessorIndex = gltf.accessors.size
                val gltfAccessor = GLTFAccessor(
                    gltf = gltf,
                    elementIndex = accessorIndex,
                    bufferView = bufferViewIndex,
                    byteOffset = 0,
                    type = when (vertexInput.size) {
                        1 -> "SCALAR"
                        2 -> "VEC2"
                        3 -> "VEC3"
                        4 -> "VEC4"
                        else -> throw IllegalStateException("Vertex attribute size must be in range [1, 4]")
                    },
                    componentType = when (vertexInput.type) {
                        GL_BYTE -> GLTF.Byte
                        GL_UNSIGNED_BYTE -> GLTF.UByte
                        GL_SHORT -> GLTF.Short
                        GL_UNSIGNED_SHORT -> GLTF.UShort
                        GL_UNSIGNED_INT -> GLTF.UInt
                        GL_FLOAT -> GLTF.Float
                        else -> throw IllegalStateException("Vertex attribute type ${vertexInput.type} is not supported in glTF 2.0")
                    },
                    count = verticesNum,
                    normalized = vertexInput.normalized
                )
                gltf.accessors.add(gltfAccessor)

                bufferViewBytesProvider[bufferViewIndex] = {
                    val buffer = gltf.buffers[0].bytes
                    buffer.position = gltfBufferView.byteOffset

                    var iByte = vertexInput.byteOffset
                    var iVertex = 0
                    while (iVertex < verticesNum) {
                        for (j in 0 until inputSizeInBytes) {
                            buffer.put(vertexBytes[iByte + j])
                        }

                        iByte += bytesPerVertex
                        iVertex++
                    }
                }

                val attributeName = attributesMapping[vertexInput.name] ?: vertexInput.name
                attributesMap[attributeName] = gltfAccessor.elementIndex

                if (attributeName == "POSITION") {
                    val min = Vec3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
                    val max = Vec3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)
                    var iFloat = vertexInput.byteOffset / 4
                    var j = 0
                    while (j < verticesNum) {
                        val x = floatView[iFloat]
                        val y = floatView[iFloat + 1]
                        val z = floatView[iFloat + 2]

                        min.x = min(x, min.x)
                        min.y = min(y, min.y)
                        min.z = min(z, min.z)

                        max.x = max(x, max.x)
                        max.y = max(y, max.y)
                        max.z = max(z, max.z)

                        iFloat += vertexInputs.floatsPerVertex
                        j++
                    }

                    gltfAccessor.min = floatArrayOf(min.x, min.y, min.z)
                    gltfAccessor.max = floatArrayOf(max.x, max.y, max.z)
                }
            }
        }

        // indices
        val indices = primitive.indices
        if (indices != null) {
            val lastView = gltf.bufferViews.lastOrNull()

            val bufferViewIndex = gltf.bufferViews.size
            val gltfBufferView = GLTFBufferView(
                gltf = gltf,
                elementIndex = bufferViewIndex,
                buffer = 0,
                byteLength = indices.bytes.size,
                byteOffset = if (lastView != null) lastView.byteOffset + lastView.byteLength else 0,
                byteStride = indices.bytesPerIndex
            )
            gltf.bufferViews.add(gltfBufferView)

            val accessorIndex = gltf.accessors.size
            val gltfAccessor = GLTFAccessor(
                gltf = gltf,
                elementIndex = accessorIndex,
                bufferView = bufferViewIndex,
                byteOffset = 0,
                type = "SCALAR",
                componentType = when (indices.type) {
                    GL_UNSIGNED_BYTE -> GLTF.UByte
                    GL_UNSIGNED_SHORT -> GLTF.UShort
                    GL_UNSIGNED_INT -> GLTF.UInt
                    else -> throw IllegalStateException("Index type ${indices.type} is not supported")
                },
                count = indices.size,
                normalized = false
            )
            gltf.accessors.add(gltfAccessor)

            gltfPrimitive.indices = accessorIndex

            bufferViewBytesProvider[bufferViewIndex] = {
                val buffer = gltf.buffers[0].bytes
                buffer.position = gltfBufferView.byteOffset

                val indexBytes = indices.bytes
                val bytesNum = indexBytes.size
                var i = 0
                while (i < bytesNum) {
                    buffer.put(indexBytes[i])
                    i++
                }
            }
        }

        // materials
        if (!gltf.materials.contains { it.material == primitive.material }) addMaterial(primitive.material)
        gltfPrimitive.material = gltf.materials.indexOfFirst { it.material == primitive.material }
    }

    fun addScene(scene: IScene) {
        val gltf = gltf

        val gltfScene = GLTFScene(gltf, gltf.scenes.size)
        gltf.scenes.add(gltfScene)
        gltfScene.name = scene.name

        for (i in scene.objects.indices) {
            val obj = scene.objects[i]
            addObject3D(obj)
            gltfScene.nodes.add(gltf.nodes.indexOfFirst { it.node == obj.node })
        }

        for (i in scene.nodes.indices) {
            val node = scene.nodes[i]
            if (!gltf.nodes.contains { it.node != node }) addNode(node)
        }
    }

    fun addSkin(skin: IArmature) {
        val gltf = gltf

        val skinIndex = gltf.skins.size
        val gltfSkin = GLTFSkin(gltf, skinIndex)
        gltfSkin.name = skin.name

        val skinNode = skin.node
        if (skinNode != null) {
            if (!gltf.nodes.contains { it.node == skinNode }) addNode(skinNode)
            val skinNodeIndex = gltf.nodes.indexOfFirst { it.node == skinNode }
            gltf.nodes[skinNodeIndex].skin = skinIndex
            gltfSkin.skeleton = skinNodeIndex
        }
    }

    /** Object is node with skin and mesh */
    fun addObject3D(obj: IObject3D) {
        val gltf = gltf

        var skinIndex = -1
        val armature = obj.armature
        if (armature != null) {
            if (!gltf.skins.contains { it.skin == armature }) addSkin(armature)
            skinIndex = gltf.skins.indexOfFirst { it.skin == armature }
        }

        var meshIndex = -1
        if (!gltf.objects.contains(obj)) {
            meshIndex = gltf.meshes.size
            val gltfMesh = GLTFMesh(gltf, meshIndex)
            gltf.meshes.add(gltfMesh)
            gltfMesh.name = obj.name

            val meshes = obj.meshes
            for (i in meshes.indices) {
                addPrimitive(meshes[i], meshIndex)
            }

            gltf.objects.add(obj)
        }

        val node = obj.node
        if (!gltf.nodes.contains { it.node == node }) {
            addNode(node = node, skin = skinIndex, mesh = meshIndex)
        }
    }

    fun addNode(
        node: ITransformNode,
        skin: Int = -1,
        mesh: Int = -1,
        camera: Int = -1,
        recursive: Boolean = true
    ) {
        val gltf = gltf

        val gltfNode = GLTFNode(gltf, gltf.nodes.size, node)
        gltfNode.name = node.name
        gltfNode.translation.set(node.position)
        gltfNode.rotation.set(node.rotation)
        gltfNode.scale.set(node.scale)
        gltfNode.mesh = mesh
        gltfNode.skin = skin
        gltfNode.camera = camera
        gltf.nodes.add(gltfNode)

        if (recursive) {
            for (i in node.childNodes.indices) {
                val child = node.childNodes[i]
                if (gltf.nodes.firstOrNull { it.node == child } == null) {
                    val nodeIndex = gltf.nodes.size
                    addNode(child, recursive = recursive)
                    gltfNode.children.add(nodeIndex)
                }
            }
        }
    }

    //fun addImage(image: IImageData) {}

    //fun addTexture(texture: ITexture) {}

    fun addMaterial(material: IMaterial) {
        val gltf = gltf

        val gltfMaterial = GLTFMaterial(gltf, gltf.materials.size, material)
        gltfMaterial.roughness = material.roughness
        gltfMaterial.metallic = material.metallic
        gltfMaterial.baseColor = material.baseColor
        gltfMaterial.alphaCutoff = material.alphaCutoff
        gltfMaterial.alphaMode = when (material.alphaMode) {
            Blending.Opaque -> "OPAQUE"
            Blending.Clip, Blending.Hashed -> "MASK"
            Blending.Blend -> "BLEND"
            else -> "OPAQUE"
        }
        gltfMaterial.doubleSided = material.cullFaceMode != GL_FRONT_FACE

        gltf.materials.add(gltfMaterial)
    }

    //fun addAnimation(anim: IAnim) {}

    fun setScenes(scenes: List<IScene>, mainScene: Int = 0) {
        gltf.mainSceneIndex = mainScene
        gltf.scene = scenes[mainScene]

        for (i in scenes.indices) {
            addScene(scenes[i])
        }
    }
}