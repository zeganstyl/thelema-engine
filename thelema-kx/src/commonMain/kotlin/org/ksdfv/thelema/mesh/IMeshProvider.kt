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

interface IMeshProvider {
    fun mesh(): IMesh
    fun vertexBuffer(bytes: IByteData, vertexInputs: IVertexInputs, initGpuObjects: Boolean = true): IVertexBuffer
    fun indexBuffer(bytes: IByteData, type: Int, initGpuObjects: Boolean = true): IIndexBufferObject
    fun vertexInputs(vararg inputs: VertexInput): IVertexInputs
    fun vertexInput(size: Int, name: String, type: Int, normalized: Boolean): IVertexInput
    fun screenQuad(): IScreenQuad
}
