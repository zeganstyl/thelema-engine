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

/** Vertex attribute input
 * [OpenGL documentation, vertex inputs](https://www.khronos.org/opengl/wiki/Vertex_Shader#Inputs).
 * [OpenGL documentation, glVertexAttribPointer](https://www.khronos.org/registry/OpenGL-Refpages/es3.0/html/glVertexAttribPointer.xhtml).
 * @author zeganstyl */
interface IVertexInput {
    /** Number of components of this attribute, must be between 1 and 4. */
    val size: Int

    /** Name of input for shaders */
    var name: String

    /** Type of each component, e.g. GL_FLOAT or GL_UNSIGNED_BYTE. */
    val type: Int

    /** Specifies whether fixed-point data values should be normalized (true) or
     * converted directly as fixed-point values (false) when they are accessed. */
    val normalized: Boolean

    /** The offset of this input in bytes. It must be set only by [IVertexInputs] */
    var byteOffset: Int

    /** The offset of this input. It may be in floats, bytes and etc. It must be set only by [IVertexInputs] */
    var componentOffset: Int

    /** Bytes count this input uses. */
    val sizeInBytes: Int
        get() = size * when (type) {
        GL_FLOAT, GL_FIXED -> 4
        GL_UNSIGNED_SHORT, GL_SHORT -> 2
        GL_UNSIGNED_BYTE, GL_BYTE -> 1
        else -> 0
    }

    companion object {
        const val PositionName = "aPosition"
        const val UVName = "aUV"
        const val NormalName = "aNormal"
        const val TangentName = "aTangent"
        const val ColorName = "aColor"
        const val BonesName = "aBones"
        const val BoneWeightsName = "aBoneWeights"
        const val ColorPackedName = "aColorPacked"
        const val Position2dName = "aPosition2d"

        /** Default position attribute */
        var Position: () -> IVertexInput = { VertexInput(3, PositionName, GL_FLOAT, true) }

        /** Default texture coordinates attribute */
        var UV: (index: Int) -> IVertexInput = {
            VertexInput(2, "aUV${if (it == 0) "" else "$it"}", GL_FLOAT, true)
        }

        /** Default normal attribute */
        var Normal: () -> IVertexInput = { VertexInput(3, NormalName, GL_FLOAT, true) }
        /** Default tangent attribute */
        var Tangent: () -> IVertexInput = { VertexInput(4, TangentName, GL_FLOAT, true) }
        /** Default color attribute */
        var Color: (index: Int) -> IVertexInput = {
            VertexInput(4, "$ColorName${if (it == 0) "" else "$it"}", GL_FLOAT, true)
        }
        /** Default 4 bones attribute. Each bone is index, that points to matrix in array */
        var Bones: (index: Int) -> IVertexInput = {
            VertexInput(4, "$BonesName${if (it == 0) "" else "$it"}", GL_FLOAT, false)
        }

        /** Default 4 bone weights attribute */
        var BoneWeights: (index: Int) -> IVertexInput = {
            VertexInput(4, "$BoneWeightsName${if (it == 0) "" else "$it"}", GL_FLOAT, true)
        }

        /** Used for 2d graphics. Less memory consumption and faster load to GPU */
        var ColorPacked: () -> IVertexInput = { VertexInput(4, ColorPackedName, GL_UNSIGNED_BYTE, true) }

        var Position2d: () -> IVertexInput = { VertexInput(2, Position2dName, GL_FLOAT, true) }
    }
}