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

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.kx.ThreadLocal

@ThreadLocal
object MSH: IMeshProvider {
    var proxy: IMeshProvider = object : IMeshProvider{
        override fun mesh(): IMesh = Mesh()
        override fun vertexBuffer(bytes: IByteData, vertexInputs: IVertexInputs, initGpuObjects: Boolean): IVertexBuffer =
            VertexBufferObject(bytes, vertexInputs, initGpuObjects = initGpuObjects)

        override fun indexBuffer(bytes: IByteData, type: Int, initGpuObjects: Boolean): IIndexBufferObject =
            IndexBufferObject(bytes, type, initGpuObjects = initGpuObjects)

        override fun vertexInputs(vararg inputs: VertexInput): IVertexInputs =
            VertexInputs(*inputs)

        override fun vertexInput(size: Int, name: String, type: Int, normalized: Boolean): IVertexInput =
            VertexInput(size, name, type, normalized)

        override fun screenQuad(): IScreenQuad = ScreenQuad()
    }

    override fun mesh(): IMesh = proxy.mesh()
    override fun vertexBuffer(bytes: IByteData, vertexInputs: IVertexInputs, initGpuObjects: Boolean): IVertexBuffer =
        proxy.vertexBuffer(bytes, vertexInputs, initGpuObjects)

    override fun indexBuffer(bytes: IByteData, type: Int, initGpuObjects: Boolean): IIndexBufferObject =
        proxy.indexBuffer(bytes, type, initGpuObjects)

    override fun vertexInputs(vararg inputs: VertexInput): IVertexInputs =
        proxy.vertexInputs(*inputs)

    override fun vertexInput(size: Int, name: String, type: Int, normalized: Boolean): IVertexInput =
        proxy.vertexInput(size, name, type, normalized)

    override fun screenQuad(): IScreenQuad = proxy.screenQuad()
}