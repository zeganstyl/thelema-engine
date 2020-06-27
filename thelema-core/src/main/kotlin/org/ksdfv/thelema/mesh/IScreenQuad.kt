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

import org.ksdfv.thelema.IFrameBuffer
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.shader.Shader

/** @author zeganstyl */
interface IScreenQuad {
    val mesh: IMesh

    private fun renderBase(
        shader: Shader,
        clearMask: Int? = GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT,
        set: IScreenQuad.() -> Unit = {}
    ) {
        if (clearMask != null) GL.glClear(clearMask)

        shader.bind()
        set(this@IScreenQuad)
        mesh.render(shader)
    }

    /**
     * Render screen quad mesh with shader
     * @param clearMask if null, glClear will not be called
     * @param set may be used to update shader uniforms. Called after shader bound, so you may not to bind it again.
     * */
    fun render(
        shader: Shader,
        out: IFrameBuffer? = null,
        clearMask: Int? = GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT,
        set: IScreenQuad.() -> Unit = {}
    ) {
        if (out != null) {
            out.render {
                renderBase(shader, clearMask, set)
            }
        } else {
            renderBase(shader, clearMask, set)
        }
    }

}