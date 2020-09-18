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
import org.ksdfv.thelema.data.IFloatData
import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO
import org.ksdfv.thelema.math.MATH
import org.ksdfv.thelema.mesh.*

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-primitive)
 *
 * @author zeganstyl */
class GLTFPrimitive(
    override val gltf: IGLTF,
    override var elementIndex: Int,
    var meshIndex: Int,
    var mesh: IMesh = Mesh()
): IJsonObjectIO, IGLTFArrayElement {
    /** Map of attribute accessors, where key - attribute name and value - accessor index. */
    val attributesMap: MutableMap<String, Int> = HashMap()

    override var name: String = ""

    var indices = -1
    var material: Int = -1

    override fun read(json: IJsonObject) {
        name = json.string("name", "")
        mesh.name = name

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
            mesh.material = gltf.materials[it].material
        }

        // indices
        indices = json.int("indices", -1)
        val indices = if (indices != -1) loadIndices(indices) else null
        mesh.indices = indices

        // vertices
        val vertexInputs = VertexInputs()

        var hasNormals = false
        var hasTangents = false

        json.get("attributes") {
            ints { attributeName, accessorIndex ->
                val accessor = gltf.accessors[accessorIndex]

                attributesMap[attributeName] = accessorIndex

                //vertexInputs.add(VertexInput(accessor.typeSize(), attributeName, accessor.glComponentType(), accessor.normalized))
                vertexInputs.add(VertexInput(accessor.typeSize(), attributeName, GL_FLOAT, accessor.normalized))

                if (attributeName == "NORMAL") {
                    hasNormals = true
                } else if (attributeName == "TANGENT") {
                    hasTangents = true
                }
            }
        }

        // TODO morph targets
        // morph targets
//                if (primitive.targets != null) {
//                    val morphTargetCount = primitive.targets.size
//                    node.weights = WeightVector(morphTargetCount)
//
//                    for (t in 0 until primitive.targets.size) {
//                        for (attribute in primitive.targets.get(t)) {
//                            val attributeName = attribute.key
//                            val accessorId = (attribute.value as Float).toInt() // XXX Json issue !?
//                            val accessor = accessors.get(accessorId)
//                            attributeBufferAccessors.add(accessor)
//
//                            when (attributeName) {
//                                "POSITION" -> vertexAttributes.add(VertexAttribute(VertexAttribute.PositionTarget, 3, ShaderProgram.POSITION_ATTRIBUTE + t, t))
//                                "NORMAL" -> vertexAttributes.add(VertexAttribute(VertexAttribute.NormalTarget, 3, ShaderProgram.NORMAL_ATTRIBUTE + t, t))
//                                "TANGENT" -> vertexAttributes.add(VertexAttribute(VertexAttribute.TangentTarget, 3, ShaderProgram.TANGENT_ATTRIBUTE + t, t))
//                                else -> throw RuntimeException("unsupported target attribute type $attributeName")
//                            }
//                        }
//                    }
//                }

        if (!hasNormals) {
            vertexInputs.add(VertexInput(3, "NORMAL", GL_FLOAT, false))
        }

        if (vertexInputs.contains("TEXCOORD_0") && !hasTangents) {
            vertexInputs.add(VertexInput(4, "TANGENT", GL_FLOAT, false))
        }

        val floatsPerVertex = vertexInputs.floatsPerVertex

        val maxVertices = gltf.accessors[attributesMap.values.first()].count

        val vertexBuffer = DATA.bytes(maxVertices * floatsPerVertex * 4)
        val vertices = vertexBuffer.floatView()

        var loadedAttributes = 0
        vertexInputs.forEach { attribute ->
            val attributeAccessor = gltf.accessors.getOrNull(attributesMap[attribute.name] ?: -1)

            val completeCall: () -> Unit = {
                // Implementation note: When normals are not specified, client implementations should calculate flat normals
                if (!hasNormals) {
                    val posOffset = vertexInputs.floatOffsetOf("POSITION")
                    val normalOffset = vertexInputs.floatOffsetOf("NORMAL")

                    var index = 0
                    if (indices != null) {
                        val indicesView = when (indices.type) {
                            GL_UNSIGNED_BYTE -> indices.bytes
                            GL_UNSIGNED_SHORT -> indices.bytes.shortView()
                            GL_UNSIGNED_INT -> indices.bytes.intView()
                            else -> throw IllegalStateException("Index type is unknown")
                        }

                        val maxIndices = indicesView.size
                        while (index < maxIndices) {
                            val indx = indicesView.toUInt(index++) * floatsPerVertex
                            val indy = indicesView.toUInt(index++) * floatsPerVertex
                            val indz = indicesView.toUInt(index++) * floatsPerVertex
                            calculateFlatNormal(indx, indy, indz, posOffset, normalOffset, vertices)
                        }
                    } else {
                        while (index < maxVertices) {
                            val indx = (index++) * floatsPerVertex
                            val indy = (index++) * floatsPerVertex
                            val indz = (index++) * floatsPerVertex
                            calculateFlatNormal(indx, indy, indz, posOffset, normalOffset, vertices)
                        }
                    }
                }

                if (!hasTangents && vertexInputs.contains("TEXCOORD_0")) {
                    // https://learnopengl.com/Advanced-Lighting/Normal-Mapping

                    val posOffset = vertexInputs.floatOffsetOf("POSITION")
                    val uvOffset = vertexInputs.floatOffsetOf("TEXCOORD_0")
                    val tangentOffset = vertexInputs.floatOffsetOf("TANGENT")

                    var index = 0
                    if (indices != null) {
                        val indicesView = when (indices.type) {
                            GL_UNSIGNED_BYTE -> indices.bytes
                            GL_UNSIGNED_SHORT -> indices.bytes.shortView()
                            GL_UNSIGNED_INT -> indices.bytes.intView()
                            else -> throw IllegalStateException("Index type is unknown")
                        }

                        val maxIndices = indicesView.size
                        while (index < maxIndices) {
                            val indx = indicesView.toUInt(index++) * floatsPerVertex
                            val indy = indicesView.toUInt(index++) * floatsPerVertex
                            val indz = indicesView.toUInt(index++) * floatsPerVertex
                            calculateTangent(indx, indy, indz, vertices, posOffset, uvOffset, tangentOffset)
                        }
                    } else {
                        while (index < maxVertices) {
                            val indx = (index++) * floatsPerVertex
                            val indy = (index++) * floatsPerVertex
                            val indz = (index++) * floatsPerVertex
                            calculateTangent(indx, indy, indz, vertices, posOffset, uvOffset, tangentOffset)
                        }
                    }
                }

                mesh.vertices = MESH.vertexBuffer(vertexBuffer, vertexInputs, initGpuObjects = false)

                gltf.meshes[meshIndex].primitives.ready(elementIndex)

                if (gltf.conf.separateThread) {
                    GL.call {
                        mesh.vertices?.initGpuObjects()
                    }
                } else {
                    mesh.vertices?.initGpuObjects()
                }
            }

            if (attributeAccessor == null) {
                loadedAttributes++
                if (loadedAttributes == vertexInputs.size) completeCall()
            } else {
                val bufferView = gltf.bufferViews[attributeAccessor.bufferView]

                gltf.buffers.getOrWait(bufferView.buffer) { buffer ->
                    buffer.bytes.position = bufferView.byteOffset + attributeAccessor.byteOffset

                    // buffer can be interleaved, so we
                    // in some cases we have to compute vertex stride
                    val byteStride = if (bufferView.byteStride == -1) attributeAccessor.strideSize() else bufferView.byteStride

                    var verticesOffset = attribute.byteOffset / 4

                    when (attributeAccessor.componentType) {
                        GLTF.Float -> {
                            val stride = byteStride / 4
                            val attributeBuffer = buffer.bytes.floatView()
                            for (j in 0 until attributeAccessor.count) {
                                for (k in 0 until stride) {
                                    vertices[verticesOffset + k] = attributeBuffer.get()
                                }

                                verticesOffset += floatsPerVertex
                            }

//                            if (attribute.name == "NORMAL" && gltf.conf.normalsConvert != GLTFConf.NormalsConvert.None) {
//                                val attributeBuffer = buffer.bytes.floatView()
//                                when (gltf.conf.normalsConvert) {
//                                    GLTFConf.NormalsConvert.SwapYZNegateZ -> {
//                                        for (j in 0 until attributeBufferAccessor.count) {
//                                            vertices[verticesOffset + 0] = attributeBuffer.get()
//                                            vertices[verticesOffset + 2] = attributeBuffer.get()
//                                            vertices[verticesOffset + 1] = attributeBuffer.get()
//
//                                            verticesOffset += floatsPerVertex
//                                        }
//                                    }
//                                    else -> {}
//                                }
//                            } else {
//                                val stride = byteStride / 4
//                                val attributeBuffer = buffer.bytes.floatView()
//                                for (j in 0 until attributeBufferAccessor.count) {
//                                    for (k in 0 until stride) {
//                                        vertices[verticesOffset + k] = attributeBuffer.get()
//                                    }
//
//                                    verticesOffset += floatsPerVertex
//                                }
//                            }
                        }

                        GLTF.UByte -> {
                            val attributeBuffer = buffer.bytes
                            for (j in 0 until attributeAccessor.count) {
                                for (k in 0 until byteStride) {
                                    vertices[verticesOffset + k] = (attributeBuffer.get().toInt() and 0xFF).toFloat()
                                }

                                verticesOffset += floatsPerVertex
                            }
                        }

                        GLTF.UShort -> {
                            val stride = byteStride / 2
                            val attributeBuffer = buffer.bytes.shortView()
                            for (j in 0 until attributeAccessor.count) {
                                for (k in 0 until stride) {
                                    vertices[verticesOffset + k] = (attributeBuffer.get().toInt() and 0xFFFF).toFloat()
                                }

                                verticesOffset += floatsPerVertex
                            }
                        }
                    }

                    loadedAttributes++

                    // set mesh data when all loaded
                    if (loadedAttributes == vertexInputs.size) completeCall()
                }
            }
        }
    }

    override fun write(json: IJsonObject) {
        val primitive = mesh

        if (name.isNotEmpty()) json["name"] = name
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

        json.set("attributes") {
            attributesMap.entries.forEach {
                set(it.key, it.value)
            }
        }
    }

    private fun loadIndices(
        accessorIndex: Int,
        out: IndexBufferObject = IndexBufferObject(DATA.nullBuffer, initGpuObjects = false)
    ): IndexBufferObject {
        val accessor = gltf.accessors[accessorIndex]
        val bufferView = gltf.bufferViews[accessor.bufferView]

        gltf.buffers.getOrWait(bufferView.buffer) { buffer ->
            if (accessor.type == "SCALAR") {
                buffer.bytes.position = bufferView.byteOffset + accessor.byteOffset

                val indexBytes = DATA.bytes(accessor.count * when (accessor.componentType) {
                    GLTF.Byte, GLTF.UByte -> 1
                    GLTF.Short, GLTF.UShort -> 2
                    GLTF.UInt -> 4
                    else -> throw RuntimeException("unsupported componentType " + accessor.componentType)
                })

                for (i in 0 until indexBytes.size) {
                    indexBytes[i] = buffer.bytes.get()
                }
                indexBytes.position = 0

                out.bytes = indexBytes
                out.type = when (accessor.componentType) {
                    GLTF.Byte, GLTF.UByte -> GL_UNSIGNED_BYTE
                    GLTF.Short, GLTF.UShort -> GL_UNSIGNED_SHORT
                    GLTF.UInt -> GL_UNSIGNED_INT
                    else -> throw RuntimeException("GltfLoader: unsupported componentType " + accessor.componentType)
                }

                buffer.bytes.position = 0

                if (gltf.conf.separateThread) {
                    GL.call { out.initGpuObjects() }
                } else {
                    out.initGpuObjects()
                }
            } else {
                throw RuntimeException("indices accessor must be SCALAR but was " + accessor.type)
            }
        }

        return out
    }

    private fun calculateFlatNormal(v1: Int, v2: Int, v3: Int, positionOffset: Int, normalOffset: Int, vertices: IFloatData) {
        var offset = v1 + positionOffset
        val ax = vertices[offset]
        val ay = vertices[offset + 1]
        val az = vertices[offset + 2]

        offset = v2 + positionOffset
        val bx = vertices[offset]
        val by = vertices[offset + 1]
        val bz = vertices[offset + 2]

        offset = v3 + positionOffset
        val cx = vertices[offset]
        val cy = vertices[offset + 1]
        val cz = vertices[offset + 2]

        val bax = bx - ax
        val bay = by - ay
        val baz = bz - az
        val cax = cx - ax
        val cay = cy - ay
        val caz = cz - az
        var x = bay * caz - baz * cay
        var y = baz * cax - bax * caz
        var z = bax * cay - bay * cax

        // normalize
        val len2 = MATH.len2(x, y, z)
        if (len2 != 0f && len2 != 1f) {
            val invSqrt = MATH.invSqrt(len2)
            x *= invSqrt
            y *= invSqrt
            z *= invSqrt
        }

        setVec3InputToTriangle(x, y, z, vertices, normalOffset, v1, v2, v3)
    }

    private fun setVec3InputToTriangle(x: Float, y: Float, z: Float, vertices: IFloatData, dataOffset: Int, v1: Int, v2: Int, v3: Int) {
        var offset = v1 + dataOffset
        vertices[offset] = x
        vertices[offset + 1] = y
        vertices[offset + 2] = z

        offset = v2 + dataOffset
        vertices[offset] = x
        vertices[offset + 1] = y
        vertices[offset + 2] = z

        offset = v3 + dataOffset
        vertices[offset] = x
        vertices[offset + 1] = y
        vertices[offset + 2] = z
    }

    private fun setVec4InputToTriangle(x: Float, y: Float, z: Float, w: Float, vertices: IFloatData, dataOffset: Int, v1: Int, v2: Int, v3: Int) {
        var offset = v1 + dataOffset
        vertices[offset] = x
        vertices[offset + 1] = y
        vertices[offset + 2] = z
        vertices[offset + 3] = w

        offset = v2 + dataOffset
        vertices[offset] = x
        vertices[offset + 1] = y
        vertices[offset + 2] = z
        vertices[offset + 3] = w

        offset = v3 + dataOffset
        vertices[offset] = x
        vertices[offset + 1] = y
        vertices[offset + 2] = z
        vertices[offset + 3] = w
    }

    private fun calculateTangent(
        v1: Int,
        v2: Int,
        v3: Int,
        vertices: IFloatData,
        positionOffset: Int,
        uvOffset: Int,
        tangentOffset: Int
    ) {
        var offset = v1 + positionOffset
        val x1 = vertices[offset]
        val y1 = vertices[offset + 1]
        val z1 = vertices[offset + 2]

        offset = v2 + positionOffset
        val edge1x = vertices[offset] - x1
        val edge1y = vertices[offset + 1] - y1
        val edge1z = vertices[offset + 2] - z1

        offset = v3 + positionOffset
        val edge2x = vertices[offset] - x1
        val edge2y = vertices[offset + 1] - y1
        val edge2z = vertices[offset + 2] - z1

        offset = v1 + uvOffset
        val u1 = vertices[offset]
        val v11 = vertices[offset + 1]

        offset = v2 + uvOffset
        val deltaUV1x = vertices[offset] - u1
        val deltaUV1y = vertices[offset + 1] - v11

        offset = v3 + uvOffset
        val deltaUV2x = vertices[offset] - u1
        val deltaUV2y = vertices[offset + 1] - v11

        val f = 1.0f / (deltaUV1x * deltaUV2y - deltaUV2x * deltaUV1y)

        var x = f * (deltaUV2y * edge1x - deltaUV1y * edge2x)
        var y = f * (deltaUV2y * edge1y - deltaUV1y * edge2y)
        var z = f * (deltaUV2y * edge1z - deltaUV1y * edge2z)

        // normalize
        val len2 = MATH.len2(x, y, z)
        if (len2 != 0f && len2 != 1f) {
            val invSqrt = MATH.invSqrt(len2)
            x *= invSqrt
            y *= invSqrt
            z *= invSqrt
        }

        setVec4InputToTriangle(x, y, z, 1f, vertices, tangentOffset, v1, v2, v3)
    }

    override fun destroy() {
        mesh.destroy()
    }
}
