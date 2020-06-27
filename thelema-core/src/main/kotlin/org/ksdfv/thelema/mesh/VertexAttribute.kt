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

package org.ksdfv.thelema.mesh

import org.ksdfv.thelema.gl.*

/**
 * https://www.khronos.org/opengl/wiki/Vertex_Shader#Inputs
 *
 * @param size the number of components of this attribute, must be between 1 and 4.
 * @param name name for attribute in shader.
 * @param type the OpenGL type of each component, e.g. GL_FLOAT or GL_UNSIGNED_BYTE. Since mesh
 * stores vertex data in 32bit floats, the total size of this attribute (type size times number of components) must be a
 * multiple of four bytes.
 * @param normalized specifies whether fixed-point data values should be normalized (true) or
 * converted directly as fixed-point values (false) when they are accessed.
 * See https://www.khronos.org/registry/OpenGL-Refpages/es3.0/html/glVertexAttribPointer.xhtml
 */
class VertexAttribute (
        val id: Int,
        val size: Int,
        var name: String,
        val type: Int = GL_FLOAT,
        val normalized: Boolean = false) {
    /** the offset of this attribute in bytes, don't change this!  */
    var byteOffset: Int = 0
    var componentOffset: Int = 0

    /** @return How many bytes this attribute uses. */
    val sizeInBytes = size * when (type) {
        GL_FLOAT, GL_FIXED -> 4
        GL_UNSIGNED_SHORT, GL_SHORT -> 2
        GL_UNSIGNED_BYTE, GL_BYTE -> 1
        else -> 0
    }

    init {
        if (size < 1 || size > 4) {
            throw RuntimeException("numComponents specified for VertexAttribute is incorrect. It must be >= 1 and <= 4")
        }
    }

    /** @return A copy of this VertexAttribute with the same parameters. The [.offset] is not copied and must
     * be recalculated, as is typically done by the [VertexAttributes] that owns the VertexAttribute.
     */
    fun copy() = VertexAttribute(id, size, name, type, normalized)

    override fun toString() = "$name: id=$id, size=$size, byteOffset=$byteOffset"

    companion object {
        const val PositionName = "a_position"
        const val Position2dName = "a_position2d"
        const val NormalName = "a_normal"
        const val TangentName = "a_tangent"
        const val ColorName = "a_color"
        const val ColorPackedName = "a_colorPacked"
        const val UVName = "a_texCoord"

        /** Global attribute list. Key - id of attribute, value - attribute */
        val names = HashMap<String, Int>()

        val builders = HashMap<Int, Builder>()

        var idCounter = 0
        fun newAttribute(size: Int, name: String, type: Int = GL_FLOAT, normalized: Boolean = true): Int {
            val id = idCounter++
            names[name] = id
            builders[id] =
                Builder(
                    id,
                    name
                ) { VertexAttribute(id, size, name, type, normalized) }
            return id
        }

        fun newAttributeArray(
                attributesNum: Int,
                attributeSize: Int,
                name: String, type: Int = GL_FLOAT,
                normalized: Boolean = true) =
                Array(attributesNum) {
                    newAttribute(
                        attributeSize,
                        name + it,
                        type,
                        normalized
                    )
                }

        val Position: Int = newAttribute(
            3,
            PositionName
        )
        val Color: Int = newAttribute(
            4,
            ColorName
        )
        val Transparency: Int =
            newAttribute(1, "a_transparency")

        val Normal: Int = newAttribute(
            3,
            NormalName
        )
        val UV: Array<Int> = newAttributeArray(
            2,
            2,
            UVName
        )

        val Bones = newAttributeArray(2, 4, "aBones", normalized = false)
        val BoneWeights =
            newAttributeArray(2, 4, "aBoneWeights")

        val Tangent: Int = newAttribute(4, TangentName)

        /** Used for 2d graphics. Less memory consumption and faster load to GPU */
        val ColorPacked: Int = newAttribute(
            4,
            ColorPackedName,
            GL_UNSIGNED_BYTE
        )

        /** Used for 2d graphics. Less memory consumption and faster load to GPU */
        val Position2d: Int = newAttribute(
            2,
            Position2dName
        )

        val ParticlePositionScale: Int =
            newAttribute(4, "a_particlePositionScale")

        /** Matrix 4x4 columns. Used in instanced rendering. If this attribute is used, then all of this array must be added to the attributes. */
        val ParticleWorldTransform: Array<Int> =
            newAttributeArray(4, 4, "a_worldTransform")

        /** Sprite animation */
        val TextureFrame: Int =
            newAttribute(4, "a_textureFrame")

        val ParticleColor: Int =
            newAttribute(4, "a_instanceColor")
    }

    class Builder(val id: Int, val name: String, val build: () -> VertexAttribute)
}