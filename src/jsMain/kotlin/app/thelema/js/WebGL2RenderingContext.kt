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

package app.thelema.js

import org.khronos.webgl.WebGLRenderingContext

/** [WebGL2RenderingContext](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext)
 *
 * @author zeganstyl */
abstract external class WebGL2RenderingContext : WebGLRenderingContext {
    // WebGLVertexArrayObject
    /** [createVertexArray](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/createVertexArray) */
    fun createVertexArray(): WebGLVertexArrayObject?
    /** [bindVertexArray](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/bindVertexArray) */
    fun bindVertexArray(vertexArray: WebGLVertexArrayObject?)
    /** [deleteVertexArray](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/deleteVertexArray) */
    fun deleteVertexArray(vertexArray: WebGLVertexArrayObject?)
    /** [isVertexArray](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/isVertexArray) */
    fun isVertexArray(vertexArray: WebGLVertexArrayObject?): Boolean

    // Drawing buffers
    /** [vertexAttribDivisor](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/vertexAttribDivisor) */
    fun vertexAttribDivisor(index: Int, divisor: Int)
    /** [drawArraysInstanced](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/drawArraysInstanced) */
    fun drawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int)
    /** [drawElementsInstanced](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/drawElementsInstanced) */
    fun drawElementsInstanced(mode: Int, count: Int, type: Int, offset: Int, instanceCount: Int)
    /** [drawBuffers](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/drawBuffers) */
    fun drawBuffers(buffers: IntArray)

    // ANGLE extension
    /** [drawArraysInstancedANGLE](https://developer.mozilla.org/en-US/docs/Web/API/ANGLE_instanced_arrays/drawArraysInstancedANGLE) */
    fun drawArraysInstancedANGLE(mode: Int, first: Int, count: Int, primcount: Int)
    /** [drawElementsInstancedANGLE](https://developer.mozilla.org/en-US/docs/Web/API/ANGLE_instanced_arrays/drawElementsInstancedANGLE) */
    fun drawElementsInstancedANGLE(mode: Int, count: Int, type: Int, offset: Int, primcount: Int)
    /** [vertexAttribDivisorANGLE](https://developer.mozilla.org/en-US/docs/Web/API/ANGLE_instanced_arrays/vertexAttribDivisorANGLE) */
    fun vertexAttribDivisorANGLE(index: Int, divisor: Int)

    /** [blitFramebuffer](https://developer.mozilla.org/en-US/docs/Web/API/WebGL2RenderingContext/blitFramebuffer) */
    fun blitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: Int, filter: Int)
}