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

import app.thelema.g3d.terrain.Terrain
import app.thelema.img.ITexture

class TerrainVertexNode: ShaderNode() {
    override val name: String
        get() = "Terrain Vertex"

    override val classId: String
        get() = ClassId

    override val inputForm: Map<String, Int>
        get() = InputForm

    var heightMap: ITexture? = null

    var terrain: Terrain? = null

    var positionName: String = "POSITION"

    var terrainMaxHeight: Float = 1f
    var terrainY = 0f

    val sourceHeight = defOut(GLSLVec3("height"))
    val terrainUV = defOut(GLSLVec3("terrainUV"))

    companion object {
        const val ClassId = "terrainVertex"

        const val NormalizedViewVector = "normalizedViewVector"
        const val VertexPosition = "vertexPosition"
        const val UV = "uv"
        const val NormalScale = "normalScale"
        const val NormalColor = "normalColor"
        const val TBN = "tbn"

        val InputForm = LinkedHashMap<String, Int>().apply {
            put(NormalizedViewVector, GLSLType.Vec3)
            put(VertexPosition, GLSLType.Vec3)
            put(UV, GLSLType.Vec2)
            put(NormalScale, GLSLType.Float)
            put(NormalColor, GLSLType.Vec3)
            put(TBN, GLSLType.Mat3)
        }
    }
}