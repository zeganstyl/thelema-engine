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

package app.thelema.shader.node

import app.thelema.g3d.IScene
import app.thelema.math.IVec3
import app.thelema.gl.IMesh
import app.thelema.math.MATH

/** @author zeganstyl */
class TerrainTileNode: ShaderNode() {
    override val componentName: String
        get() = "TerrainTileNode"

    var inputPosition by input(GLSLNode.vertex.position)

    val tilePositionScale = output(GLSLVec3("tilePositionScale"))
    val outputPosition = output(GLSLVec3("outputPosition"))
    val uv = output(GLSLVec2("uv"))

    var tilePositionScaleName: String = "tilePositionScale"

    var uvScale = 10f

    override fun prepareShaderNode(mesh: IMesh, scene: IScene?) {
        shader[tilePositionScale.ref] = mesh.getMaterialValue<IVec3>(tilePositionScaleName) ?: MATH.Zero3
    }

    override fun declarationVert(out: StringBuilder) {
        super.declarationVert(out)
        out.append(tilePositionScale.uniformRefEnd)
        out.append(outputPosition.outRefEnd)
        out.append(uv.outRefEnd)
    }

    override fun declarationFrag(out: StringBuilder) {
        super.declarationFrag(out)
        out.append(tilePositionScale.uniformRefEnd)
        out.append(outputPosition.inRefEnd)
        out.append(uv.inRefEnd)
    }

    override fun executionVert(out: StringBuilder) {
        super.executionVert(out)
        val pos = inputPosition.ref
        val outputPos = outputPosition.ref
        val tilePositionScale = tilePositionScale.ref
        out.append("$outputPos = $pos;\n")
        out.append("$outputPos.x *= $tilePositionScale.z;\n")
        out.append("$outputPos.z *= $tilePositionScale.z;\n")
        out.append("$outputPos.x += $tilePositionScale.x;\n")
        out.append("$outputPos.z += $tilePositionScale.y;\n")
        if (uvScale == 1f) {
            out.append("${uv.ref} = $outputPos.xz;\n")
        } else if (uvScale != 0f) {
            out.append("${uv.ref} = $outputPos.xz * $uvScale;\n")
        }
    }
}