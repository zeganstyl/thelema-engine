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

package org.ksdfv.thelema.teavm

import org.teavm.jso.JSByRef
import org.teavm.jso.webgl.WebGLRenderingContext

/** [WebGL documentation](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext)
 *
 * @author zeganstyl
 * */
interface WebGL2RenderingContext : WebGLRenderingContext {
    // WebGLVertexArrayObject
    /** [WebGL documentation](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/createVertexArray) */
    fun createVertexArray(): WebGLVertexArrayObject?
    /** [WebGL documentation](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/bindVertexArray) */
    fun bindVertexArray(vertexArray: WebGLVertexArrayObject?)
    /** [WebGL documentation](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/deleteVertexArray) */
    fun deleteVertexArray(vertexArray: WebGLVertexArrayObject?)
    /** [WebGL documentation](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/isVertexArray) */
    fun isVertexArray(vertexArray: WebGLVertexArrayObject?): Boolean

    // Drawing buffers
    /** [WebGL documentation](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/vertexAttribDivisor) */
    fun vertexAttribDivisor(index: Int, divisor: Int)
    /** [WebGL documentation](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/drawArraysInstanced) */
    fun drawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int)
    /** [WebGL documentation](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/drawElementsInstanced) */
    fun drawElementsInstanced(mode: Int, count: Int, type: Int, offset: Int, instanceCount: Int)
    /** [WebGL documentation](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/drawBuffers) */
    fun drawBuffers(@JSByRef buffers: IntArray)
}