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

package app.thelema.g3d.mesh

import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.data.IFloatData
import app.thelema.data.IShortData
import app.thelema.g3d.IMaterial
import app.thelema.g3d.Material
import app.thelema.gl.*

/** @author zeganstyl */
abstract class MeshBuilderOld {
    open var uv: Boolean = true
    open var normals: Boolean = true

    var material: IMaterial = Material()

    var positionName: String = "POSITION"
    var uvName: String = "UV"
    var normalName: String = "NORMAL"

    lateinit var currentFloatBuffer: IFloatData

    fun createVertices(verticesNum: Int, block: IByteData.() -> Unit): IVertexBuffer {
        var bytesNum = 12
        if (uv) bytesNum += 8
        if (normals) bytesNum += 12
        return VertexBuffer(DATA.bytes(verticesNum * bytesNum).apply(block)).apply {
            verticesCount = verticesNum
            addAttribute(3, positionName, GL_FLOAT, false)
            if (uv) addAttribute(2, uvName, GL_FLOAT, false)
            if (normals) addAttribute(3, normalName, GL_FLOAT, false)
        }
    }

    fun createVerticesFloat(verticesNum: Int, block: IFloatData.() -> Unit) =
        createVertices(verticesNum) {
            currentFloatBuffer = floatView()
            currentFloatBuffer.apply(block)
        }

    fun createIndices(indicesNum: Int, type: Int, block: IByteData.() -> Unit): IIndexBuffer {
        val bytesNum = indicesNum * when (type) {
            GL_UNSIGNED_BYTE -> 1
            GL_UNSIGNED_SHORT -> 2
            GL_UNSIGNED_INT -> 4
            else -> throw IllegalArgumentException()
        }

        return IndexBuffer(DATA.bytes(bytesNum).apply(block)).apply { indexType = type }
    }

    fun createIndicesShort(indicesNum: Int, block: IShortData.() -> Unit) =
        createIndices(indicesNum, GL_UNSIGNED_SHORT) { shortView().apply(block) }

    abstract fun build(out: IMesh = Mesh()): IMesh
}