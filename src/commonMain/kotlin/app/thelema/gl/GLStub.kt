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

package app.thelema.gl

import app.thelema.data.IByteData
import app.thelema.data.IFloatData

class GLStub: AbstractGL() {
    override val mainFrameBufferHeight: Int
        get() = 0

    override val mainFrameBufferWidth: Int
        get() = 0

    override val mainFrameBufferHandle: Int
        get() = 0

    override val majVer: Int
        get() = 0
    override val minVer: Int
        get() = 0
    override val relVer: Int
        get() = 0

    override val glslVer: Int
        get() = 110

    override fun enableExtension(extension: String): Boolean = true
    override fun glActiveTextureBase(value: Int) = Unit
    override fun glUseProgramBase(value: Int) = Unit
    override fun glBindBufferBase(target: Int, buffer: Int) {}
    override fun glBindVertexArrayBase(value: Int) {}
    override fun glCullFaceBase(value: Int) {}
    override fun glBlendFuncBase(factorS: Int, factorD: Int) {}
    override fun glEnableBase(value: Int) {}
    override fun glDisableBase(value: Int) {}
    override fun glDepthFuncBase(value: Int) {}
    override fun glDepthMaskBase(value: Boolean) {}
    override fun glBindTextureBase(target: Int, texture: Int) {}
    override fun glDeleteVertexArrays(id: Int) {}
    override fun glGenVertexArrays(): Int = 1
    override fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) {}
    override fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) {}
    override fun glVertexAttribDivisor(index: Int, divisor: Int) {}
    override fun glClear(mask: Int) {}
    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {}
    override fun glDeleteTexture(texture: Int) {}
    override fun glDrawArrays(mode: Int, first: Int, count: Int) {}
    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: IByteData) {}
    override fun glGenTexture(): Int = 1
    override fun glGetError(): Int = GL_NO_ERROR
    override fun glDisableVertexAttribArray(index: Int) {}
    override fun glScissor(x: Int, y: Int, width: Int, height: Int) {}

    override fun glTexImage2D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        border: Int,
        format: Int,
        type: Int,
        pixels: IByteData?
    ) = Unit

    override fun glViewport(x: Int, y: Int, width: Int, height: Int) = Unit

    override fun glAttachShader(program: Int, shader: Int) = Unit

    override fun glBindFramebuffer(target: Int, framebuffer: Int) = Unit

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) = Unit

    override fun glBufferData(target: Int, size: Int, data: IByteData?, usage: Int) = Unit

    override fun glCheckFramebufferStatus(target: Int): Int = GL_FRAMEBUFFER_COMPLETE

    override fun glCompileShader(shader: Int) = Unit

    override fun glCreateProgram(): Int = 1

    override fun glCreateShader(type: Int): Int = 1

    override fun glDeleteBuffer(buffer: Int) = Unit

    override fun glDeleteProgram(program: Int) = Unit

    override fun glDeleteRenderbuffer(renderbuffer: Int) = Unit

    override fun glDeleteShader(shader: Int) = Unit

    override fun glDetachShader(program: Int, shader: Int) = Unit

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) = Unit

    override fun glEnableVertexAttribArray(index: Int) = Unit

    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) = Unit

    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) = Unit

    override fun glGenBuffer(): Int = 1

    override fun glGenerateMipmap(target: Int) = Unit

    override fun glGenFramebuffer(): Int = 1

    override fun glGenRenderbuffer(): Int = 1

    override fun glGetActiveAttrib(program: Int, index: Int, size: IntArray, type: IntArray): String = ""

    override fun glGetActiveUniform(program: Int, index: Int, size: IntArray, type: IntArray): String = ""

    override fun glGetAttribLocation(program: Int, name: String): Int = 0

    override fun glGetProgramiv(program: Int, pname: Int, params: IntArray) = Unit

    override fun glGetProgramInfoLog(program: Int): String = ""

    override fun glGetShaderiv(shader: Int, pname: Int, params: IntArray) = Unit

    override fun glGetShaderInfoLog(shader: Int): String = ""

    override fun glGetUniformLocation(program: Int, name: String): Int = 0

    override fun glLinkProgram(program: Int) = Unit

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) = Unit

    override fun glShaderSource(shader: Int, string: String) = Unit

    override fun glTexParameteri(target: Int, pname: Int, param: Int) = Unit

    override fun glUniform1f(location: Int, x: Float) = Unit

    override fun glUniform1i(location: Int, x: Int) = Unit

    override fun glUniform2f(location: Int, x: Float, y: Float) = Unit

    override fun glUniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) = Unit

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) = Unit

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) = Unit

    override fun glVertexAttribPointer(
        indx: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        ptr: IByteData
    ) = Unit

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: Int) = Unit

    override fun glDrawBuffers(n: Int, bufs: IntArray) = Unit

    override fun glUniform3f(location: Int, x: Float, y: Float, z: Float) = Unit
}