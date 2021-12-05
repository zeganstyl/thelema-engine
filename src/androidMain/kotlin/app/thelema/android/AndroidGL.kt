package app.thelema.android

import android.opengl.GLES30
import app.thelema.data.IByteData
import app.thelema.data.IFloatData
import app.thelema.gl.AbstractGL
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class AndroidGL(val app: AndroidApp): AbstractGL() {
    override val mainFrameBufferWidth: Int
        get() = app.view.holder.surfaceFrame.width()

    override val mainFrameBufferHeight: Int
        get() = app.view.holder.surfaceFrame.height()

    override fun initVersions() {
        majVer = app.glesVersion
        minVer = 0
        relVer = 0

        glslVer = if (app.glesVersion == 3) 200 else 100
    }

    override fun isExtensionSupported(extension: String): Boolean {
        return true
    }

    override fun glGenTextureBase(): Int = arrayToInt { GLES30.glGenTextures(1, it, 0) }

    override fun glGenBufferBase(): Int = arrayToInt { GLES30.glGenBuffers(1, it, 0) }

    override fun glGenFrameBufferBase(): Int = arrayToInt { GLES30.glGenFramebuffers(1, it, 0) }

    override fun glGenRenderBufferBase(): Int = arrayToInt { GLES30.glGenRenderbuffers(1, it, 0) }

    override fun glDeleteTextureBase(id: Int) {
        GLES30.glDeleteTextures(1, intToArray(id), 0)
    }

    override fun glDeleteBufferBase(id: Int) {
        GLES30.glDeleteBuffers(1, intToArray(id), 0)
    }

    override fun glDeleteFrameBufferBase(id: Int) {
        GLES30.glDeleteFramebuffers(1, intToArray(id), 0)
    }

    override fun glDeleteRenderBufferBase(id: Int) {
        GLES30.glDeleteRenderbuffers(1, intToArray(id), 0)
    }

    override fun enableExtension(extension: String): Boolean {
        return GLES30.glGetString(GLES30.GL_EXTENSIONS).contains(extension)
    }

    override fun glGetString(name: Int): String? {
        return GLES30.glGetString(name)
    }

    override fun glGetIntegerv(pname: Int, params: IntArray) {
        GLES30.glGetIntegerv(pname, params, 0)
    }

    override fun glGetFloatv(pname: Int, params: FloatArray) {
        GLES30.glGetFloatv(pname, params, 0)
    }

    override fun glActiveTextureBase(value: Int) {
        GLES30.glActiveTexture(value)
    }

    override fun glBindBufferBase(target: Int, buffer: Int) {
        GLES30.glBindBuffer(target, buffer)
    }

    override fun glBindTextureBase(target: Int, texture: Int) {
        GLES30.glBindTexture(target, texture)
    }

    override fun glBindVertexArrayBase(value: Int) {
        GLES30.glBindVertexArray(value)
    }

    override fun glDisableVertexAttribArray(index: Int) {
        GLES30.glDisableVertexAttribArray(index)
    }

    override fun glBlendFuncBase(factorS: Int, factorD: Int) {
        GLES30.glBlendFunc(factorS, factorD)
    }

    override fun glCullFaceBase(value: Int) {
        GLES30.glCullFace(value)
    }

    override fun glDepthFuncBase(value: Int) {
        GLES30.glDepthFunc(value)
    }

    override fun glDepthMaskBase(value: Boolean) {
        GLES30.glDepthMask(value)
    }

    override fun glDisableBase(value: Int) {
        GLES30.glDisable(value)
    }

    override fun glEnableBase(value: Int) {
        GLES30.glEnable(value)
    }

    override fun glUseProgramBase(value: Int) {
        GLES30.glUseProgram(value)
    }

    override fun glAttachShader(program: Int, shader: Int) {
        GLES30.glAttachShader(program, shader)
    }

    override fun glBindFramebuffer(target: Int, framebuffer: Int) {
        GLES30.glBindFramebuffer(target, framebuffer)
    }

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) {
        GLES30.glBindRenderbuffer(target, renderbuffer)
    }

    override fun glBufferData(target: Int, size: Int, data: IByteData?, usage: Int) {
        GLES30.glBufferData(target, size, data?.sourceObject as Buffer?, usage)
    }

    override fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        GLES30.glScissor(x, y, width, height)
    }

    override fun glCheckFramebufferStatus(target: Int): Int = GLES30.glCheckFramebufferStatus(target)

    override fun glClear(mask: Int) {
        GLES30.glClear(mask)
    }

    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        GLES30.glClearColor(red, green, blue, alpha)
    }

    override fun glCompileShader(shader: Int) {
        GLES30.glCompileShader(shader)
    }

    override fun glCreateProgram(): Int {
        return GLES30.glCreateProgram()
    }

    override fun glCreateShader(type: Int): Int {
        return GLES30.glCreateShader(type)
    }

    private fun intToArray(value: Int): IntArray {
        val ints = IntArray(1)
        ints[0] = value
        return ints
    }

    override fun glDeleteProgram(program: Int) {
        GLES30.glDeleteProgram(program)
    }

    override fun glDeleteShader(shader: Int) {
        GLES30.glDeleteShader(shader)
    }

    override fun glDeleteVertexArrays(id: Int) {
        GLES30.glDeleteVertexArrays(1, intToArray(id), 0)
    }

    override fun glDetachShader(program: Int, shader: Int) {
        GLES30.glDetachShader(program, shader)
    }

    override fun glDrawArrays(mode: Int, first: Int, count: Int) {
        GLES30.glDrawArrays(mode, first, count)
    }

    override fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) {
        GLES30.glDrawArraysInstanced(mode, first, count, instanceCount)
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) {
        GLES30.glDrawElements(mode, count, type, indices)
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: IByteData) {
        GLES30.glDrawElements(mode, count, type, indices.sourceObject as Buffer)
    }

    override fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) {
        GLES30.glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount)
    }

    fun arrayToInt(block: (array: IntArray) -> Unit): Int {
        val ints = IntArray(1)
        block(ints)
        return ints[0]
    }

    override fun glGenVertexArrays(): Int = arrayToInt { GLES30.glGenVertexArrays(1, it, 0) }

    override fun glGenerateMipmap(target: Int) {
        GLES30.glGenerateMipmap(target)
    }

    override fun glGetError(): Int = GLES30.glGetError()

    override fun glGetProgramInfoLog(program: Int): String = GLES30.glGetProgramInfoLog(program)

    override fun glGetShaderInfoLog(shader: Int): String = GLES30.glGetShaderInfoLog(shader)

    override fun glShaderSource(shader: Int, string: String) {
        GLES30.glShaderSource(shader, string)
    }

    override fun glGetShaderiv(shader: Int, pname: Int, params: IntArray) {
        GLES30.glGetShaderiv(shader, pname, params, 0)
    }

    override fun glLinkProgram(program: Int) {
        GLES30.glLinkProgram(program)
    }

    override fun glGetProgramiv(program: Int, pname: Int, params: IntArray) {
        GLES30.glGetProgramiv(program, pname, params, 0)
    }

    override fun glGetActiveAttrib(program: Int, index: Int, size: IntArray, type: IntArray): String {
        return GLES30.glGetActiveAttrib(program, index, size, 0, type, 0)
    }

    override fun glGetAttribLocation(program: Int, name: String): Int {
        return GLES30.glGetAttribLocation(program, name)
    }

    override fun glEnableVertexAttribArray(index: Int) {
        GLES30.glEnableVertexAttribArray(index)
    }

    override fun glVertexAttribPointer(
        indx: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        ptr: IByteData
    ) {
        GLES30.glVertexAttribPointer(indx, size, type, normalized, stride, ptr.sourceObject as ByteBuffer)
    }

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: Int) {
        GLES30.glVertexAttribPointer(indx, size, type, normalized, stride, ptr)
    }

    override fun glVertexAttribDivisor(index: Int, divisor: Int) {
        GLES30.glVertexAttribDivisor(index, divisor)
    }

    override fun glGetActiveUniform(program: Int, index: Int, size: IntArray, type: IntArray): String {
        return GLES30.glGetActiveUniform(program, index, size, 0, type, 0)
    }

    override fun glGetUniformLocation(program: Int, name: String): Int {
        return GLES30.glGetUniformLocation(program, name)
    }

    override fun glTexParameteri(target: Int, pname: Int, param: Int) {
        GLES30.glTexParameteri(target, pname, param)
    }

    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) {
        GLES30.glFramebufferTexture2D(target, attachment, textarget, texture, level)
    }

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        GLES30.glRenderbufferStorage(target, internalformat, width, height)
    }

    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) {
        GLES30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer)
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
        GLES30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels?.sourceObject as Buffer?)
    }

    override fun glUniform1f(location: Int, x: Float) {
        GLES30.glUniform1f(location, x)
    }

    override fun glUniform1i(location: Int, x: Int) {
        GLES30.glUniform1i(location, x)
    }

    override fun glUniform2f(location: Int, x: Float, y: Float) {
        GLES30.glUniform2f(location, x, y)
    }

    override fun glUniform3f(location: Int, x: Float, y: Float, z: Float) {
        GLES30.glUniform3f(location, x, y, z)
    }

    override fun glUniform3fv(location: Int, count: Int, v: FloatArray) {
        GLES30.glUniform3fv(location, count, v, 0)
    }

    override fun glUniform3fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        GLES30.glUniform3fv(location, count, v, offset)
    }

    override fun glUniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) {
        GLES30.glUniform4f(location, x, y, z, w)
    }

    override fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        GLES30.glUniformMatrix3fv(location, count, transpose, value, offset)
    }

    override fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        GLES30.glUniformMatrix3fv(location, count, transpose, value.sourceObject as FloatBuffer)
    }

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        GLES30.glUniformMatrix4fv(location, count, transpose, value, offset)
    }

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        GLES30.glUniformMatrix4fv(location, count, transpose, value.sourceObject as FloatBuffer)
    }

    override fun glViewport(x: Int, y: Int, width: Int, height: Int) {
        GLES30.glViewport(x, y, width, height)
    }

    override fun glDrawBuffers(n: Int, bufs: IntArray) {
        GLES30.glDrawBuffers(n, bufs, 0)
    }

    override fun glTexParameterf(target: Int, pname: Int, param: Float) {
        GLES30.glTexParameterf(target, pname, param)
    }

    override fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: IByteData) {
        GLES30.glReadPixels(x, y, width, height, format, type, pixels.sourceObject as Buffer)
    }
}
