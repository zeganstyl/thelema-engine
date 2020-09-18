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

import org.ksdfv.thelema.g3d.IObject3D
import org.ksdfv.thelema.g3d.Object3D
import org.ksdfv.thelema.g3d.ShaderChannel
import org.ksdfv.thelema.g3d.node.ITransformNode
import org.ksdfv.thelema.g3d.node.Node
import org.ksdfv.thelema.g3d.node.PreviousTRS
import org.ksdfv.thelema.g3d.node.TransformNodeType
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO
import org.ksdfv.thelema.math.*
import org.ksdfv.thelema.shader.node.VelocityNode
import org.ksdfv.thelema.shader.node.VertexNode
import org.ksdfv.thelema.utils.LOG

/** [glTF 2.0 specification - reference](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-node)
 *
 * [glTF 2.0 specification - node](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#nodes-and-hierarchy)
 *
 * @author zeganstyl */
class GLTFNode(
    override val gltf: IGLTF,
    override var elementIndex: Int,
    var node: ITransformNode = Node()
): IJsonObjectIO, IGLTFArrayElement {
    var camera: Int = -1
    val children: MutableList<Int> = ArrayList()
    var skin: Int = -1
    var mesh: Int = -1
    val weights: MutableList<Float> = ArrayList()
    override var name: String = ""
    var matrix: IMat4? = null
    var rotation: IVec4 = Vec4(0f, 0f, 0f, 1f)
    var scale: IVec3 = Vec3(1f, 1f, 1f)
    var translation: IVec3 = Vec3()

    override fun read(json: IJsonObject) {
        json.string("name") {
            name = it
            node.name = it
        }

        children.clear()
        json.ints("children") { childIndex ->
            children.add(childIndex)
            gltf.nodes.getOrWait(childIndex) { childNode ->
                node.addChildNode(childNode.node)
            }
        }

        if (json.contains("matrix")) {
            json.array("matrix") {
                var i = 0
                floats {
                    node.worldMatrix.values[i] = it
                    i++
                }
            }
            node.worldMatrix.getTranslation(node.position)
            node.worldMatrix.getRotation(node.rotation)
            node.worldMatrix.getScale(node.scale)
        } else {
            json.array("translation") { node.position.set(float(0), float(1), float(2)) }
            json.array("rotation") { node.rotation.set(float(0), float(1), float(2), float(3)) }
            json.array("scale") { node.scale.set(float(0), float(1), float(2)) }
        }

        weights.clear()
        json.floats("weights") { weights.add(it) }

        var obj: IObject3D? = null

        json.int("camera") { camera = it }
        json.int("mesh") { meshIndex ->
            mesh = meshIndex
            gltf.meshes.getOrWait(meshIndex) { mesh ->
                val newObj = Object3D()
                newObj.name = name
                mesh.primitives.forEach { newObj.meshes.add(it.mesh) }
                if (gltf.conf.setupVelocityShader) newObj.previousTransform = PreviousTRS()
                gltf.objects.add(newObj)
                obj = newObj
            }
        }

        if (json.contains("skin")) {
            json.int("skin") { skinIndex ->
                skin = skinIndex

                gltf.skins.getOrWait(skinIndex) { gltfSkin ->
                    val skin = gltfSkin.skin
                    obj?.armature = skin
                    obj?.meshes?.forEach { mesh ->
                        mesh.material.shader?.apply {
                            val vertexNode = nodes.first { it is VertexNode } as VertexNode
                            vertexNode.maxBones = skin.boneMatrices.size
                            vertexNode.worldTransformType = TransformNodeType.None
                        }

                        if (gltf.conf.setupVelocityShader) {
                            mesh.material.shaderChannels[ShaderChannel.Velocity]?.apply {
                                val vertexNode = nodes.first { it is VertexNode } as VertexNode
                                vertexNode.maxBones = skin.boneMatrices.size
                                vertexNode.worldTransformType = TransformNodeType.None

                                val velocityNode = nodes.first { it is VelocityNode } as VelocityNode
                                velocityNode.maxBones = skin.boneMatrices.size
                                velocityNode.worldTransformType = TransformNodeType.None
                            }
                        }

                        if (gltf.conf.setupDepthRendering) {
                            mesh.material.shaderChannels[ShaderChannel.Depth]?.apply {
                                val vertexNode = nodes.first { it is VertexNode } as VertexNode
                                vertexNode.maxBones = skin.boneMatrices.size
                                vertexNode.worldTransformType = TransformNodeType.None
                            }
                        }
                    }

                    gltf.nodes.ready(elementIndex)
                }
            }
        } else {
            gltf.nodes.ready(elementIndex)
        }
    }

    override fun write(json: IJsonObject) {
        if (name.isNotEmpty()) json["name"] = name
        if (children.isNotEmpty()) json.setInts("children", children.size) { children[it] }
        if (weights.isNotEmpty()) json.setFloats("weights", weights.size) { weights[it] }

        if (mesh != -1) json["mesh"] = mesh
        if (skin != -1) json["skin"] = skin
        if (camera != -1) json["camera"] = camera

        val matrix = matrix
        if (matrix != null) {
            json.setFloats("matrix", 16) { matrix.values[it] }
        } else {
            val t = translation
            if (t.x != 0f || t.y != 0f || t.z != 0f) json.setArray("translation") { add(t.x, t.y, t.z) }
            val r = rotation
            if (r.x != 0f || r.y != 0f || r.z != 0f || r.w != 1f) json.setArray("rotation") { add(r.x, r.y, r.z, r.w) }
            val s = scale
            if (s.x != 1f || s.y != 1f || s.z != 1f) json.setArray("scale") { add(s.x, s.y, s.z) }
        }
    }

    override fun destroy() {}
}