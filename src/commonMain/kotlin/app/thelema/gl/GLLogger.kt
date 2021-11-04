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
import app.thelema.utils.LOG

class GLLogger(var proxy: IGL): AbstractGL() {
    override val mainFrameBufferHeight: Int
        get() = 0

    override val mainFrameBufferWidth: Int
        get() = 0

    override fun glActiveTextureBase(value: Int) {
        proxy.glActiveTexture(value)
        LOG.info("glActiveTexture($value)")
    }

    override fun glUseProgramBase(value: Int) {
        proxy.glUseProgram(value)
        LOG.info("glUseProgram($value)")
    }

    override fun glBindBufferBase(target: Int, buffer: Int) {
        proxy.glBindBuffer(target, buffer)
        LOG.info("glBindBuffer(${getBufferTarget(target)}, $buffer)")
    }

    private fun getBufferTarget(target: Int): String = when(target) {
        GL_ELEMENT_ARRAY_BUFFER -> "GL_ELEMENT_ARRAY_BUFFER"
        GL_ARRAY_BUFFER -> "GL_ARRAY_BUFFER"
        GL_UNIFORM_BUFFER -> "GL_UNIFORM_BUFFER"
        else -> "UNKNOWN"
    }

    override fun glBindVertexArrayBase(value: Int) {
        LOG.info("glBindVertexArray($value)")
    }

    override fun glCullFaceBase(value: Int) {
        LOG.info("glBindVertexArray($value)")
    }

    override fun glBlendFuncBase(factorS: Int, factorD: Int) {
        LOG.info("glBlendFunc($factorS, $factorD)")
    }

    private fun getGLState(type: Int) = when (type) {
        else -> "UNKNOWN"
    }

    override fun glEnableBase(value: Int) {
        LOG.info("glEnable(${getGLState(value)})")
    }

    override fun glDisableBase(value: Int) {
        LOG.info("glDisable(${getGLState(value)})")
    }

    override fun glDepthFuncBase(value: Int) {
        TODO("Not yet implemented")
    }

    override fun glDepthMaskBase(value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun glBindTextureBase(target: Int, texture: Int) {
        TODO("Not yet implemented")
    }

    override fun glGenTextureBase(): Int {
        TODO("Not yet implemented")
    }

    override fun glGenBufferBase(): Int {
        TODO("Not yet implemented")
    }

    override fun glGenFrameBufferBase(): Int {
        TODO("Not yet implemented")
    }

    override fun glGenRenderBufferBase(): Int {
        TODO("Not yet implemented")
    }

    override fun glDeleteTextureBase(id: Int) {
        TODO("Not yet implemented")
    }

    override fun glDeleteBufferBase(id: Int) {
        TODO("Not yet implemented")
    }

    override fun glDeleteFrameBufferBase(id: Int) {
        TODO("Not yet implemented")
    }

    override fun glDeleteRenderBufferBase(id: Int) {
        TODO("Not yet implemented")
    }

    override fun enableExtension(extension: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun glDrawBuffers(n: Int, bufs: IntArray) {
        TODO("Not yet implemented")
    }

    override fun glDeleteVertexArrays(id: Int) {
        TODO("Not yet implemented")
    }

    override fun glGenVertexArrays(): Int {
        TODO("Not yet implemented")
    }

    override fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) {
        TODO("Not yet implemented")
    }

    override fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) {
        TODO("Not yet implemented")
    }

    override fun glVertexAttribDivisor(index: Int, divisor: Int) {
        TODO("Not yet implemented")
    }

    override fun glClear(mask: Int) {
        TODO("Not yet implemented")
    }

    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        TODO("Not yet implemented")
    }

    override fun glDrawArrays(mode: Int, first: Int, count: Int) {
        TODO("Not yet implemented")
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: IByteData) {
        TODO("Not yet implemented")
    }

    override fun glGetError(): Int {
        TODO("Not yet implemented")
    }

    override fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

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
    ) {
        TODO("Not yet implemented")
    }

    override fun glViewport(x: Int, y: Int, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun glAttachShader(program: Int, shader: Int) {
        TODO("Not yet implemented")
    }

    override fun glBindFramebuffer(target: Int, framebuffer: Int) {
        TODO("Not yet implemented")
    }

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) {
        TODO("Not yet implemented")
    }

    override fun glBufferData(target: Int, size: Int, data: IByteData?, usage: Int) {
        TODO("Not yet implemented")
    }

    override fun glCheckFramebufferStatus(target: Int): Int {
        TODO("Not yet implemented")
    }

    override fun glCompileShader(shader: Int) {
        TODO("Not yet implemented")
    }

    override fun glCreateProgram(): Int {
        TODO("Not yet implemented")
    }

    override fun glCreateShader(type: Int): Int {
        TODO("Not yet implemented")
    }

    override fun glDeleteProgram(program: Int) {
        TODO("Not yet implemented")
    }

    override fun glDeleteShader(shader: Int) {
        TODO("Not yet implemented")
    }

    override fun glDetachShader(program: Int, shader: Int) {
        TODO("Not yet implemented")
    }

    override fun glDisableVertexAttribArray(index: Int) {
        TODO("Not yet implemented")
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) {
        TODO("Not yet implemented")
    }

    override fun glEnableVertexAttribArray(index: Int) {
        TODO("Not yet implemented")
    }

    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) {
        TODO("Not yet implemented")
    }

    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) {
        TODO("Not yet implemented")
    }

    override fun glGenerateMipmap(target: Int) {
        TODO("Not yet implemented")
    }

    override fun glGetActiveAttrib(program: Int, index: Int, size: IntArray, type: IntArray): String {
        TODO("Not yet implemented")
    }

    override fun glGetActiveUniform(program: Int, index: Int, size: IntArray, type: IntArray): String {
        TODO("Not yet implemented")
    }

    override fun glGetAttribLocation(program: Int, name: String): Int {
        TODO("Not yet implemented")
    }

    override fun glGetProgramiv(program: Int, pname: Int, params: IntArray) {
        TODO("Not yet implemented")
    }

    override fun glGetProgramInfoLog(program: Int): String {
        TODO("Not yet implemented")
    }

    override fun glGetShaderiv(shader: Int, pname: Int, params: IntArray) {
        TODO("Not yet implemented")
    }

    override fun glGetShaderInfoLog(shader: Int): String {
        TODO("Not yet implemented")
    }

    override fun glGetUniformLocation(program: Int, name: String): Int {
        TODO("Not yet implemented")
    }

    override fun glLinkProgram(program: Int) {
        TODO("Not yet implemented")
    }

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun glShaderSource(shader: Int, string: String) {
        TODO("Not yet implemented")
    }

    override fun glTexParameteri(target: Int, pname: Int, param: Int) {
        TODO("Not yet implemented")
    }

    override fun glUniform1f(location: Int, x: Float) {
        TODO("Not yet implemented")
    }

    override fun glUniform1i(location: Int, x: Int) {
        TODO("Not yet implemented")
    }

    override fun glUniform2f(location: Int, x: Float, y: Float) {
        TODO("Not yet implemented")
    }

    override fun glUniform3f(location: Int, x: Float, y: Float, z: Float) {
        TODO("Not yet implemented")
    }

    override fun glUniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) {
        TODO("Not yet implemented")
    }

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        TODO("Not yet implemented")
    }

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        TODO("Not yet implemented")
    }

    override fun glVertexAttribPointer(
        indx: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        ptr: IByteData
    ) {
        TODO("Not yet implemented")
    }

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: Int) {
        LOG.info("glVertexAttribPointer($indx, $size, $type, $normalized, $stride, $ptr)")
    }
}