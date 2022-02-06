/*
 * Copyright 2020-2021 Anton Trushkov
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

package app.thelema.gltf

import app.thelema.data.DATA
import app.thelema.ecs.component
import app.thelema.g3d.AABB
import app.thelema.g3d.SphereBoundings
import app.thelema.g3d.mesh.Mesh3DTool
import app.thelema.gl.*
import app.thelema.json.IJsonObject
import app.thelema.utils.iterate

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-primitive)
 *
 * @author zeganstyl */
class GLTFPrimitive(val gltfMesh: GLTFMesh): GLTFArrayElementAdapter(gltfMesh.primitives) {
    /** Map of attribute accessors, where key - attribute name and value - accessor index. */
    val attributesMap: MutableMap<String, Int> = HashMap()

    var indices = -1
    var material: Int = -1

    override val defaultName: String
        get() = "Primitive"

    val mesh: IMesh = gltfMesh.entity.entity("${defaultName}_$elementIndex").component()

    private var indexBufferUploadedToGPU = false

    override fun readJson() {
        super.readJson()

        val mesh = mesh
        if (name != defaultName) mesh.entity.name = gltfMesh.entity.makeChildName(name)

        mesh.primitiveType = when (json.int("mode", 4)) {
            0 -> GL_POINTS
            1 -> GL_LINES
            2 -> GL_LINE_LOOP
            3 -> GL_LINE_STRIP
            4 -> GL_TRIANGLES
            5 -> GL_TRIANGLE_STRIP
            6 -> GL_TRIANGLE_FAN
            else -> GL_TRIANGLES
        }

        json.int("material") {
            material = it
            mesh.material = (gltf.materials[it] as GLTFMaterial).material
        }

        // indices
        indices = json.int("indices", -1)

        if (indices >= 0) {
            val accessor = gltf.accessors[indices] as GLTFAccessor

            accessor.getOrWaitSetupBuffer { bytes ->
                val indices = IndexBuffer()
                indices.indexType = when (accessor.componentType) {
                    GLTF.Byte, GLTF.UByte -> GL_UNSIGNED_BYTE
                    GLTF.Short, GLTF.UShort -> GL_UNSIGNED_SHORT
                    GLTF.UInt -> GL_UNSIGNED_INT
                    else -> throw RuntimeException("GLTFPrimitive: unsupported index componentType " + accessor.componentType)
                }

                val newBytes = bytes.copy(bytes.position, accessor.count * indices.bytesPerIndex)

                newBytes.rewind()
                indices.bytes = newBytes

                mesh.indices = indices

                gltf.runGLCall { uploadIndices() }

                if (gltf.conf.mergeVertexAttributes) mergeBuffers(json) else addBuffersAsIs(json)
            }
        } else {
            if (gltf.conf.mergeVertexAttributes) mergeBuffers(json) else addBuffersAsIs(json)
        }
    }

    private fun setupBoundings() {
        mesh.boundings = if (mesh.containsAttribute("JOINTS_0")) SphereBoundings() else AABB()
        mesh.boundings?.also { it.setVertices(mesh, "POSITION") }
    }

    fun mergeBuffers(json: IJsonObject) {
        val mesh = mesh

        val vertices = VertexBuffer()
        mesh.addVertexBuffer(vertices)

        var hasNormals = false
        var hasTangents = false

        json.get("attributes") {
            forEachInt { attributeName, accessorIndex ->
                val accessor = gltf.accessors[accessorIndex] as GLTFAccessor

                attributesMap[attributeName] = accessorIndex

                mesh.verticesCount = accessor.count
                vertices.verticesCount = accessor.count

                vertices.addAttribute(
                    size = accessor.typeSize(),
                    name = attributeName,
                    type = when (accessor.componentType) {
                        GLTF.Float -> GL_FLOAT
                        GLTF.Byte -> GL_BYTE
                        GLTF.UByte -> GL_UNSIGNED_BYTE
                        GLTF.Short -> GL_SHORT
                        GLTF.UShort -> GL_UNSIGNED_SHORT
                        else -> throw IllegalStateException("GLTF: Unknown accessor componentType: ${accessor.componentType}")
                    },
                    normalized = if (attributeName.startsWith("JOINTS_")) false else accessor.normalized
                )

                if (attributeName == "NORMAL") {
                    hasNormals = true
                } else if (attributeName == "TANGENT") {
                    hasTangents = true
                }
            }
        }

        if (!hasNormals && conf.calculateNormalsCpu) {
            vertices.addAttribute(3, "NORMAL", GL_FLOAT, false)
        }

        if (vertices.containsInput("TEXCOORD_0") && !hasTangents) {
            vertices.addAttribute(4, "TANGENT", GL_FLOAT, false)
        }

        val bytesPerVertex = vertices.bytesPerVertex

        val maxVertices = (gltf.accessors[attributesMap.values.first()] as GLTFAccessor).count

        val vertexBuffer = DATA.bytes(maxVertices * bytesPerVertex)
        vertices.bytes = vertexBuffer

        var loadedAttributes = 0
        vertices.vertexAttributes.forEach { attribute ->
            val attributeAccessor = gltf.accessors.getOrNull(attributesMap[attribute.name] ?: -1) as GLTFAccessor?

            val completeCall: () -> Unit = {
                // Implementation note: When normals are not specified, client implementations should calculate flat normals
                if (!hasNormals && conf.calculateNormalsCpu) calcNormals()
                if (!hasTangents && conf.calculateTangentsCpu) calcTangents()

                setupBoundings()

                ready()

                gltf.runGLCall {
                    vertices.uploadBufferToGpu()
                    uploadIndices()
                    if (!gltf.conf.saveMeshesInCPUMem) {
                        vertices.bytes.destroy()
                        // FIXME
                        //mesh.indices?.bytes?.destroy()
                    }
                }
            }

            if (attributeAccessor == null) {
                loadedAttributes++
                if (loadedAttributes == vertices.vertexAttributes.size) completeCall()
            } else {
                attributeAccessor.getOrWaitSetupBuffer { bytes ->
                    //val byteStride = bufferView.byteStride
                    val elementSize = attributeAccessor.elementSize()

                    var verticesOffset = attribute.byteOffset

                    for (j in 0 until attributeAccessor.count) {
                        vertexBuffer.position = verticesOffset
                        for (k in 0 until elementSize) {
                            vertexBuffer.put(bytes.get())
                        }
                        verticesOffset += bytesPerVertex
                    }

                    vertexBuffer.position = 0

                    loadedAttributes++

                    // set mesh data when all loaded
                    if (loadedAttributes == vertices.vertexAttributes.size) completeCall()
                }
            }
        }
    }

    private fun uploadIndices() {
        if (!indexBufferUploadedToGPU) {
            indexBufferUploadedToGPU = true
            mesh.indices?.uploadBufferToGpu()
        }
    }

    override fun initProgress() {
        super.initProgress()
        indexBufferUploadedToGPU = false

        val attributes = json.obj("attributes")

        //if (!attributes.contains("NORMAL")) maxProgress += mesh.indices?.size ?: mesh.verticesCount
        if (!attributes.contains("NORMAL") && conf.calculateNormalsCpu) maxProgress++
        if (attributes.contains("TEXCOORD_0") && !attributes.contains("TANGENT") && conf.calculateTangentsCpu) {
            maxProgress++
            maxProgress++
//            maxProgress += (mesh.indices?.size ?: mesh.verticesCount) / 3 // each triangle
//            maxProgress += mesh.verticesCount // orthogonalization
        }
    }

    fun addBuffersAsIs(json: IJsonObject) {
        val mesh = mesh

        val completeCall: () -> Unit = {
            //println("mesh $mesh completed")
            // Implementation note: When normals are not specified, client implementations should calculate flat normals
            if (conf.calculateNormalsCpu && !json.obj("attributes").contains("NORMAL")) {
                mesh.addVertexBuffer {
                    addAttribute(3, "NORMAL", GL_FLOAT, false)
                    initVertexBuffer(mesh.verticesCount)
                }

                calcNormals()
            }

            if (conf.calculateTangentsCpu && mesh.containsAttribute("TEXCOORD_0") && !json.obj("attributes").contains("TANGENT")) {
                val positionAccessor = json.obj("attributes").int("POSITION")
                val buffer = gltfMesh.tangentsMap[positionAccessor]
                if (buffer != null) {
                    mesh.addVertexBuffer(buffer)
                } else {
                    gltfMesh.tangentsMap[positionAccessor] = mesh.addVertexBuffer {
                        addAttribute(4, "TANGENT", GL_FLOAT, false)
                        initVertexBuffer(mesh.verticesCount)
                    }
                }
                calcTangents()
            }

            setupBoundings()

            ready()

            gltf.runGLCall {
                mesh.vertexBuffers.iterate { it.uploadBufferToGpu() }
                uploadIndices()
                if (!gltf.conf.saveMeshesInCPUMem) {
                    mesh.vertexBuffers.iterate { it.bytes.destroy() }
                    // FIXME
                    //mesh.indices?.bytes?.destroy()
                }
            }
        }

        json.get("attributes") {
            var loadedAttributes = 0

            val attributesCount = size

            forEachInt { attributeName, accessorIndex ->
                val accessor = gltf.accessors[accessorIndex] as GLTFAccessor

                mesh.verticesCount = accessor.count

                accessor.getOrWaitSetupBuffer { bytes ->
                    val buffer = gltfMesh.buffersMap[accessorIndex]
                    if (buffer != null) {
                        buffer.vertexAttributes[0].addAlias(attributeName)
                        mesh.addVertexBuffer(buffer)
                    } else {
                        gltfMesh.buffersMap[accessorIndex] = mesh.addVertexBuffer {
                            this.bytes = bytes.byteView().copy()
                            verticesCount = accessor.count
                            addAttribute(
                                size = accessor.typeSize(),
                                name = attributeName,
                                type = when (accessor.componentType) {
                                    GLTF.Float -> GL_FLOAT
                                    GLTF.Byte -> GL_BYTE
                                    GLTF.UByte -> GL_UNSIGNED_BYTE
                                    GLTF.Short -> GL_SHORT
                                    GLTF.UShort -> GL_UNSIGNED_SHORT
                                    else -> throw IllegalStateException("GLTF: Unknown accessor componentType: ${accessor.componentType}")
                                },
                                normalized = if (attributeName.startsWith("JOINTS_")) false else accessor.normalized
                            )
                        }
                    }

                    loadedAttributes++

                    if (loadedAttributes == attributesCount) completeCall()
                }
            }
        }
    }

    fun calcNormals() {
        val mesh = mesh

        if (mesh.primitiveType == GL_TRIANGLES) {
            Mesh3DTool.calculateFlatNormals(
                mesh,
                mesh.getAttribute("POSITION"),
                mesh.getAttribute("NORMAL")
            )
            currentProgress++
        }
    }

    fun calcTangents() {
        val mesh = mesh

        if (mesh.containsAttribute("TEXCOORD_0") && mesh.primitiveType == GL_TRIANGLES) {
            Mesh3DTool.calculateTangents(
                mesh,
                mesh.getAttribute("POSITION"),
                mesh.getAttribute("TEXCOORD_0"),
                mesh.getAttribute("TANGENT")
            )
            currentProgress++
            Mesh3DTool.orthogonalizeTangents(
                mesh.getAttribute("TANGENT"),
                mesh.getAttribute("NORMAL")
            )
            currentProgress++
        }
    }

    override fun writeJson() {
        super.writeJson()

        val mesh = mesh

        val primitive = mesh

        if (material != -1) json["material"] = material
        if (indices != -1) json["indices"] = indices

        if (primitive.primitiveType != GL_TRIANGLES) {
            json["mode"] = when (primitive.primitiveType) {
                0 -> GL_POINTS
                1 -> GL_LINES
                2 -> GL_LINE_LOOP
                3 -> GL_LINE_STRIP
                4 -> GL_TRIANGLES
                5 -> GL_TRIANGLE_STRIP
                6 -> GL_TRIANGLE_FAN
                else -> throw IllegalStateException("Primitive type can't be ${primitive.primitiveType}")
            }
        }

        json.setObj("attributes") {
            attributesMap.entries.forEach {
                set(it.key, it.value)
            }
        }
    }

    override fun destroy() {
        mesh.destroy()
        attributesMap.clear()
    }
}
