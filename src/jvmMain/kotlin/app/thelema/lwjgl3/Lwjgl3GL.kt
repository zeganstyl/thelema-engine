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

package app.thelema.lwjgl3

import app.thelema.data.IByteData
import app.thelema.data.IFloatData
import app.thelema.data.IIntData
import app.thelema.gl.*
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.*
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Lwjgl3GL: AbstractGL() {
    override var mainFrameBufferWidth: Int = 0
    override var mainFrameBufferHeight: Int = 0

    private var buffer: ByteBuffer? = null
    private var floatBuffer: FloatBuffer? = null
    private var intBuffer: IntBuffer? = null

    override val mainFrameBufferHandle: Int
        get() = 0
    override var majVer: Int = 0
    override var minVer: Int = 0
    override var relVer: Int = 0
    override var glslVer: Int = 0

    override val isGLES: Boolean
        get() = false

    override fun glActiveTextureBase(value: Int) {
        GL13.glActiveTexture(value)
    }

    override fun glUseProgramBase(value: Int) {
        GL20.glUseProgram(value)
    }

    override fun glBindBufferBase(target: Int, buffer: Int) {
        GL15.glBindBuffer(target, buffer)
    }

    override fun glBindVertexArrayBase(value: Int) {
        GL30.glBindVertexArray(value)
    }

    override fun glCullFaceBase(value: Int) {
        GL11.glCullFace(value)
    }

    override fun glBlendFuncBase(factorS: Int, factorD: Int) {
        GL11.glBlendFunc(factorS, factorD)
    }

    override fun glEnableBase(value: Int) {
        GL11.glEnable(value)
    }

    override fun glDisableBase(value: Int) {
        GL11.glDisable(value)
    }

    override fun glDepthFuncBase(value: Int) {
        GL11.glDepthFunc(value)
    }

    override fun glDepthMaskBase(value: Boolean) {
        GL11.glDepthMask(value)
    }

    override fun glBindTextureBase(target: Int, texture: Int) {
        GL11.glBindTexture(target, texture)
    }

    override fun glGenTextureBase(): Int = GL11.glGenTextures()
    override fun glGenBufferBase(): Int = GL15.glGenBuffers()
    override fun glGenFrameBufferBase(): Int = GL30.glGenFramebuffers()
    override fun glGenRenderBufferBase(): Int = GL30.glGenRenderbuffers()

    override fun glDeleteTextureBase(id: Int) { GL11.glDeleteTextures(id) }
    override fun glDeleteBufferBase(id: Int) { GL15.glDeleteBuffers(id) }
    override fun glDeleteFrameBufferBase(id: Int) { GL30.glDeleteFramebuffers(id) }
    override fun glDeleteRenderBufferBase(id: Int) { GL30.glDeleteRenderbuffers(id) }

    private fun ensureBufferCapacity(numBytes: Int) {
        var buffer = buffer
        if (buffer == null || buffer.capacity() < numBytes) {
            buffer = BufferUtils.createByteBuffer(numBytes)
            floatBuffer = buffer.asFloatBuffer()
            intBuffer = buffer.asIntBuffer()
            this.buffer = buffer
        }
    }

    private fun toFloatBuffer(v: FloatArray, offset: Int, count: Int): FloatBuffer {
        ensureBufferCapacity(count shl 2)
        floatBuffer!!.clear()
        floatBuffer!!.limit(count)
        floatBuffer!!.put(v, offset, count)
        floatBuffer!!.position(0)
        return floatBuffer!!
    }

    private fun toIntBuffer(v: IntArray, offset: Int, count: Int): IntBuffer {
        ensureBufferCapacity(count shl 2)
        intBuffer!!.clear()
        intBuffer!!.limit(count)
        intBuffer!!.put(v, offset, count)
        intBuffer!!.position(0)
        return intBuffer!!
    }

    override fun isExtensionSupported(extension: String) = GLFW.glfwExtensionSupported(extension)

    override fun enableExtension(extension: String): Boolean {
        // TODO
        return true
    }

    override fun glAttachShader(program: Int, shader: Int) {
        GL20.glAttachShader(program, shader)
    }

    override fun glBindAttribLocation(program: Int, index: Int, name: String) {
        GL20.glBindAttribLocation(program, index, name)
    }

    override fun glBlendColor(red: Float, green: Float, blue: Float, alpha: Float) {
        GL14.glBlendColor(red, green, blue, alpha)
    }

    override fun glBlendEquation(mode: Int) {
        GL14.glBlendEquation(mode)
    }

    override fun glBlendEquationSeparate(modeRGB: Int, modeAlpha: Int) {
        GL20.glBlendEquationSeparate(modeRGB, modeAlpha)
    }

    override fun glBlendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int) {
        GL14.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha)
    }

    override fun glBufferData(target: Int, size: Int, data: IByteData?, usage: Int) {
        if (data != null) {

            GL15.glBufferData(target, data.sourceObject as ByteBuffer, usage)
        }
    }

    override fun glBufferSubData(target: Int, offset: Int, size: Int, data: IByteData) {
        GL15.glBufferSubData(target, offset.toLong(), data.sourceObject as ByteBuffer)
    }

    override fun glClear(mask: Int) {
        GL11.glClear(mask)
    }

    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        GL11.glClearColor(red, green, blue, alpha)
    }

    override fun glClearDepthf(depth: Float) {
        GL11.glClearDepth(depth.toDouble())
    }

    override fun glClearStencil(s: Int) {
        GL11.glClearStencil(s)
    }

    override fun glColorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) {
        GL11.glColorMask(red, green, blue, alpha)
    }

    override fun glCompileShader(shader: Int) {
        GL20.glCompileShader(shader)
    }

    override fun glCompressedTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int,
                                        imageSize: Int, data: IByteData) {
        GL13.glCompressedTexImage2D(target, level, internalformat, width, height, border, data.sourceObject as ByteBuffer)
    }

    override fun glCopyTexImage2D(target: Int, level: Int, internalformat: Int, x: Int, y: Int, width: Int, height: Int, border: Int) {
        GL11.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border)
    }

    override fun glCopyTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, x: Int, y: Int, width: Int, height: Int) {
        GL11.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height)
    }

    override fun glCreateProgram(): Int {
        return GL20.glCreateProgram()
    }

    override fun glCreateShader(type: Int): Int {
        return GL20.glCreateShader(type)
    }

    override fun glDeleteBuffers(buffers: IntArray) {
        GL15.glDeleteBuffers(buffers)
    }

    override fun glDeleteProgram(program: Int) {
        GL20.glDeleteProgram(program)
    }

    override fun glDeleteShader(shader: Int) {
        GL20.glDeleteShader(shader)
    }

    override fun glDeleteTextures(textures: IntArray) {
        GL11.glDeleteTextures(textures)
    }

    override fun glDepthRangef(zNear: Float, zFar: Float) {
        GL11.glDepthRange(zNear.toDouble(), zFar.toDouble())
    }

    override fun glDetachShader(program: Int, shader: Int) {
        GL20.glDetachShader(program, shader)
    }

    override fun glDisableVertexAttribArray(index: Int) {
        GL20.glDisableVertexAttribArray(index)
    }

    override fun glDrawArrays(mode: Int, first: Int, count: Int) {
        GL11.glDrawArrays(mode, first, count)
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: IByteData) {
        when (type) {
            GL_UNSIGNED_BYTE -> GL11.glDrawElements(mode, indices.sourceObject as ByteBuffer)
            GL_UNSIGNED_SHORT -> GL11.glDrawElements(mode, (indices.sourceObject as ByteBuffer).asShortBuffer())
            GL_UNSIGNED_INT -> GL11.glDrawElements(mode, (indices.sourceObject as ByteBuffer).asIntBuffer())
            else -> throw IllegalArgumentException()
        }
    }

    override fun glEnableVertexAttribArray(index: Int) {
        GL20.glEnableVertexAttribArray(index)
    }

    override fun glFinish() {
        GL11.glFinish()
    }

    override fun glFlush() {
        GL11.glFlush()
    }

    override fun glFrontFace(mode: Int) {
        GL11.glFrontFace(mode)
    }

    override fun glGenBuffers(buffers: IntArray) {
        GL15.glGenBuffers(buffers)
    }

    override fun glGenTextures(n: Int, textures: IntArray) {
        GL11.glGenTextures(textures)
    }

    override fun glGetActiveAttrib(program: Int, index: Int, size: IntArray, type: IntArray): String {
        val typeTmp = BufferUtils.createIntBuffer(2)
        val sizeTmp = BufferUtils.createIntBuffer(1)
        sizeTmp.put(0, size[0])
        val name = GL20.glGetActiveAttrib(program, index, 256, sizeTmp, typeTmp)
        size[0] = typeTmp[0]
        type[0] = typeTmp[1]
        return name
    }

    override fun glGetActiveUniform(program: Int, index: Int, size: IntArray, type: IntArray): String {
        val typeTmp = BufferUtils.createIntBuffer(2)
        val sizeTmp = BufferUtils.createIntBuffer(1)
        sizeTmp.put(0, size[0])
        val name = GL20.glGetActiveUniform(program, index, 256, sizeTmp, typeTmp)
        size[0] = typeTmp[0]
        type[0] = typeTmp[1]
        return name
    }

    override fun glGetAttachedShaders(program: Int, maxcount: Int, count: IntArray, shaders: IntArray) {
        GL20.glGetAttachedShaders(program, count, shaders)
    }

    override fun glGetAttribLocation(program: Int, name: String): Int {
        return GL20.glGetAttribLocation(program, name)
    }

    override fun glGetBooleanv(pname: Int, params: IByteData) {
        GL11.glGetBooleanv(pname, params.sourceObject as ByteBuffer)
    }

    override fun glGetBufferParameteriv(target: Int, pname: Int, params: IntArray) {
        GL15.glGetBufferParameteriv(target, pname, params)
    }

    override fun glGetError(): Int {
        return GL11.glGetError()
    }

    override fun glGetFloatv(pname: Int, params: FloatArray) {
        GL11.glGetFloatv(pname, params)
    }

    override fun glGetFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int, params: IntArray) {
        EXTFramebufferObject.glGetFramebufferAttachmentParameterivEXT(target, attachment, pname, params)
    }

    override fun glGetIntegerv(pname: Int, params: IntArray) {
        GL11.glGetIntegerv(pname, params)
    }

    override fun glGetProgramInfoLog(program: Int): String {
        return GL20.glGetProgramInfoLog(program)
    }

    override fun glGetProgramiv(program: Int, pname: Int, params: IntArray) {
        GL20.glGetProgramiv(program, pname, params)
    }

    override fun glGetShaderInfoLog(shader: Int): String {
        return GL20.glGetShaderInfoLog(shader)
    }

    override fun glGetShaderPrecisionFormat(shadertype: Int, precisiontype: Int, range: IntArray, precision: IntArray) {
        throw UnsupportedOperationException("unsupported, won't implement")
    }

    override fun glGetShaderiv(shader: Int, pname: Int, params: IntArray) {
        GL20.glGetShaderiv(shader, pname, params)
    }

    override fun glGetString(name: Int): String {
        return GL11.glGetString(name)!!
    }

    override fun glGetTexParameterfv(target: Int, pname: Int, params: FloatArray) {
        GL11.glGetTexParameterfv(target, pname, params)
    }

    override fun glGetTexParameteriv(target: Int, pname: Int, params: IntArray) {
        GL11.glGetTexParameteriv(target, pname, params)
    }

    override fun glGetUniformLocation(program: Int, name: String): Int {
        return GL20.glGetUniformLocation(program, name)
    }

    override fun glGetUniformfv(program: Int, location: Int, params: FloatArray) {
        GL20.glGetUniformfv(program, location, params)
    }

    override fun glGetUniformiv(program: Int, location: Int, params: IntArray) {
        GL20.glGetUniformiv(program, location, params)
    }

    override fun glGetVertexAttribPointerv(index: Int, pname: Int, pointer: IntArray) {
        throw UnsupportedOperationException("unsupported, won't implement")
    }

    override fun glGetVertexAttribfv(index: Int, pname: Int, params: FloatArray) {
        GL20.glGetVertexAttribfv(index, pname, params)
    }

    override fun glGetVertexAttribiv(index: Int, pname: Int, params: IntArray) {
        GL20.glGetVertexAttribiv(index, pname, params)
    }

    override fun glHint(target: Int, mode: Int) {
        GL11.glHint(target, mode)
    }

    override fun glIsBuffer(buffer: Int): Boolean {
        return GL15.glIsBuffer(buffer)
    }

    override fun glIsEnabled(cap: Int): Boolean {
        return GL11.glIsEnabled(cap)
    }

    override fun glIsProgram(program: Int): Boolean {
        return GL20.glIsProgram(program)
    }

    override fun glIsShader(shader: Int): Boolean {
        return GL20.glIsShader(shader)
    }

    override fun glIsTexture(texture: Int): Boolean {
        return GL11.glIsTexture(texture)
    }

    override fun glLineWidth(width: Float) {
        GL11.glLineWidth(width)
    }

    override fun glLinkProgram(program: Int) {
        GL20.glLinkProgram(program)
    }

    override fun glPixelStorei(pname: Int, param: Int) {
        GL11.glPixelStorei(pname, param)
    }

    override fun glPolygonOffset(factor: Float, units: Float) {
        GL11.glPolygonOffset(factor, units)
    }

    override fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: IByteData) {
        GL11.glReadPixels(x, y, width, height, format, type, pixels.sourceObject as ByteBuffer)
    }

    override fun glReleaseShaderCompiler() { // nothing to do here
    }

    override fun glSampleCoverage(value: Float, invert: Boolean) {
        GL13.glSampleCoverage(value, invert)
    }

    override fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        GL11.glScissor(x, y, width, height)
    }

    override fun glShaderBinary(n: Int, shaders: IntArray, binaryformat: Int, binary: IByteData, length: Int) {
        throw UnsupportedOperationException("unsupported, won't implement")
    }

    override fun glShaderSource(shader: Int, string: String) {
        GL20.glShaderSource(shader, string)
    }

    override fun glStencilFunc(func: Int, ref: Int, mask: Int) {
        GL11.glStencilFunc(func, ref, mask)
    }

    override fun glStencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int) {
        GL20.glStencilFuncSeparate(face, func, ref, mask)
    }

    override fun glStencilMask(mask: Int) {
        GL11.glStencilMask(mask)
    }

    override fun glStencilMaskSeparate(face: Int, mask: Int) {
        GL20.glStencilMaskSeparate(face, mask)
    }

    override fun glStencilOp(fail: Int, zfail: Int, zpass: Int) {
        GL11.glStencilOp(fail, zfail, zpass)
    }

    override fun glStencilOpSeparate(face: Int, fail: Int, zfail: Int, zpass: Int) {
        GL20.glStencilOpSeparate(face, fail, zfail, zpass)
    }

    override fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int,
                              pixels: IByteData?) {
        GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels?.sourceObject as ByteBuffer?)
    }

    override fun glTexParameterf(target: Int, pname: Int, param: Float) {
        GL11.glTexParameterf(target, pname, param)
    }

    override fun glTexParameterfv(target: Int, pname: Int, params: FloatArray) {
        GL11.glTexParameterfv(target, pname, params)
    }

    override fun glTexParameteri(target: Int, pname: Int, param: Int) {
        GL11.glTexParameteri(target, pname, param)
    }

    override fun glTexParameteriv(target: Int, pname: Int, params: IntArray) {
        GL11.glTexParameteriv(target, pname, params)
    }

    override fun glTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, pixels: IByteData) {
        GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels.sourceObject as ByteBuffer)
    }

    override fun glUniform1f(location: Int, x: Float) {
        GL20.glUniform1f(location, x)
    }

    override fun glUniform1fv(location: Int, count: Int, v: FloatArray) {
        GL20.glUniform1fv(location, v)
    }

    override fun glUniform1fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        GL20.glUniform1fv(location, toFloatBuffer(v, offset, count))
    }

    override fun glUniform1i(location: Int, x: Int) {
        GL20.glUniform1i(location, x)
    }

    override fun glUniform1iv(location: Int, count: Int, v: IntArray) {
        GL20.glUniform1iv(location, v)
    }

    override fun glUniform1iv(location: Int, count: Int, v: IntArray, offset: Int) {
        GL20.glUniform1iv(location, toIntBuffer(v, offset, count))
    }

    override fun glUniform2f(location: Int, x: Float, y: Float) {
        GL20.glUniform2f(location, x, y)
    }

    override fun glUniform2fv(location: Int, count: Int, v: FloatArray) {
        GL20.glUniform2fv(location, v)
    }

    override fun glUniform2fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        GL20.glUniform2fv(location, toFloatBuffer(v, offset, count shl 1))
    }

    override fun glUniform2i(location: Int, x: Int, y: Int) {
        GL20.glUniform2i(location, x, y)
    }

    override fun glUniform2iv(location: Int, count: Int, v: IntArray) {
        GL20.glUniform2iv(location, v)
    }

    override fun glUniform2iv(location: Int, count: Int, v: IntArray, offset: Int) {
        GL20.glUniform2iv(location, toIntBuffer(v, offset, count shl 1))
    }

    override fun glUniform3f(location: Int, x: Float, y: Float, z: Float) {
        GL20.glUniform3f(location, x, y, z)
    }

    override fun glUniform3fv(location: Int, count: Int, v: FloatArray) {
        GL20.glUniform3fv(location, v)
    }

    override fun glUniform3fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        GL20.glUniform3fv(location, toFloatBuffer(v, offset, count * 3))
    }

    override fun glUniform3i(location: Int, x: Int, y: Int, z: Int) {
        GL20.glUniform3i(location, x, y, z)
    }

    override fun glUniform3iv(location: Int, count: Int, v: IntArray) {
        GL20.glUniform3iv(location, v)
    }

    override fun glUniform3iv(location: Int, count: Int, v: IntArray, offset: Int) {
        GL20.glUniform3iv(location, toIntBuffer(v, offset, count * 3))
    }

    override fun glUniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) {
        GL20.glUniform4f(location, x, y, z, w)
    }

    override fun glUniform4fv(location: Int, count: Int, v: FloatArray) {
        GL20.glUniform4fv(location, v)
    }

    override fun glUniform4fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        GL20.glUniform4fv(location, toFloatBuffer(v, offset, count shl 2))
    }

    override fun glUniform4i(location: Int, x: Int, y: Int, z: Int, w: Int) {
        GL20.glUniform4i(location, x, y, z, w)
    }

    override fun glUniform4iv(location: Int, count: Int, v: IntArray) {
        GL20.glUniform4iv(location, v)
    }

    override fun glUniform4iv(location: Int, count: Int, v: IntArray, offset: Int) {
        GL20.glUniform4iv(location, toIntBuffer(v, offset, count shl 2))
    }

    override fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        GL20.glUniformMatrix2fv(location, transpose, value.sourceObject as FloatBuffer)
    }

    override fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        GL20.glUniformMatrix2fv(location, transpose, toFloatBuffer(value, offset, count shl 2))
    }

    override fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        GL20.glUniformMatrix3fv(location, transpose, value.sourceObject as FloatBuffer)
    }

    override fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        GL20.glUniformMatrix3fv(location, transpose, toFloatBuffer(value, offset, count * 9))
    }

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        GL20.glUniformMatrix4fv(location, transpose, value.sourceObject as FloatBuffer)
    }

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        GL20.glUniformMatrix4fv(location, transpose, toFloatBuffer(value, offset, count shl 4))
    }

    override fun glValidateProgram(program: Int) {
        GL20.glValidateProgram(program)
    }

    override fun glVertexAttrib1f(indx: Int, x: Float) {
        GL20.glVertexAttrib1f(indx, x)
    }

    override fun glVertexAttrib1fv(indx: Int, values: FloatArray) {
        GL20.glVertexAttrib1f(indx, values[0])
    }

    override fun glVertexAttrib2f(indx: Int, x: Float, y: Float) {
        GL20.glVertexAttrib2f(indx, x, y)
    }

    override fun glVertexAttrib2fv(indx: Int, values: FloatArray) {
        GL20.glVertexAttrib2f(indx, values[0], values[1])
    }

    override fun glVertexAttrib3f(indx: Int, x: Float, y: Float, z: Float) {
        GL20.glVertexAttrib3f(indx, x, y, z)
    }

    override fun glVertexAttrib3fv(indx: Int, values: FloatArray) {
        GL20.glVertexAttrib3f(indx, values[0], values[1], values[2])
    }

    override fun glVertexAttrib4f(indx: Int, x: Float, y: Float, z: Float, w: Float) {
        GL20.glVertexAttrib4f(indx, x, y, z, w)
    }

    override fun glVertexAttrib4fv(indx: Int, values: FloatArray) {
        GL20.glVertexAttrib4f(indx, values[0], values[1], values[2], values[3])
    }

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: IByteData) {
        GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr.sourceObject as ByteBuffer)
    }

    override fun glViewport(x: Int, y: Int, width: Int, height: Int) {
        GL11.glViewport(x, y, width, height)
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) {
        GL11.glDrawElements(mode, count, type, indices.toLong())
    }

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: Int) {
        GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr.toLong())
    }

    override fun glReadBuffer(mode: Int) {
        GL11.glReadBuffer(mode)
    }

    override fun glDrawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, indices: IByteData) {
        GL12.glDrawRangeElements(mode, start, end, indices.sourceObject as ByteBuffer)
    }

    override fun glDrawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, offset: Int) {
        GL12.glDrawRangeElements(mode, start, end, count, type, offset.toLong())
    }

    override fun glTexImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, border: Int, format: Int,
                              type: Int, pixels: IByteData?) {
        GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels?.sourceObject as ByteBuffer?)
    }

    override fun glTexImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, border: Int, format: Int,
                              type: Int, offset: Int) {
        GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, offset.toLong())
    }

    override fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int,
                                 format: Int, type: Int, pixels: IByteData) {
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels.sourceObject as ByteBuffer)
    }

    override fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int,
                                 format: Int, type: Int, offset: Int) {
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset.toLong())
    }

    override fun glCopyTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, x: Int, y: Int, width: Int,
                                     height: Int) {
        GL12.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height)
    }

    override fun glGenQueries(ids: IntArray) {
        GL15.glGenQueries(ids)
    }

    override fun glGenQueries(): Int {
        return GL15.glGenQueries()
    }

    override fun glDeleteQueries(ids: IntArray) {
        GL15.glDeleteQueries(ids)
    }

    override fun glDeleteQueries(id: Int) {
        GL15.glDeleteQueries(id)
    }

    override fun glIsQuery(id: Int): Boolean {
        return GL15.glIsQuery(id)
    }

    override fun glBeginQuery(target: Int, id: Int) {
        GL15.glBeginQuery(target, id)
    }

    override fun glEndQuery(target: Int) {
        GL15.glEndQuery(target)
    }

    override fun glGetQueryiv(target: Int, pname: Int, params: IntArray) {
        GL15.glGetQueryiv(target, pname, params)
    }

    override fun glGetQueryObjectuiv(id: Int, pname: Int, params: IntArray) {
        GL15.glGetQueryObjectuiv(id, pname, params)
    }

    override fun glUnmapBuffer(target: Int): Boolean {
        return GL15.glUnmapBuffer(target)
    }

    override fun glGetBufferPointerv(target: Int, pname: Int): IntArray { // FIXME glGetBufferPointerv needs a proper translation
// return GL15.glGetBufferPointer(target, pname);
        throw UnsupportedOperationException("Not implemented")
    }

    override fun glDrawBuffers(n: Int, bufs: IntArray) {
        GL20.glDrawBuffers(bufs)
    }

    override fun glUniformMatrix2x3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        GL21.glUniformMatrix2x3fv(location, transpose, value.sourceObject as FloatBuffer)
    }

    override fun glUniformMatrix3x2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        GL21.glUniformMatrix3x2fv(location, transpose, value.sourceObject as FloatBuffer)
    }

    override fun glUniformMatrix2x4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        GL21.glUniformMatrix2x4fv(location, transpose, value.sourceObject as FloatBuffer)
    }

    override fun glUniformMatrix4x2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        GL21.glUniformMatrix4x2fv(location, transpose, value.sourceObject as FloatBuffer)
    }

    override fun glUniformMatrix3x4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        GL21.glUniformMatrix3x4fv(location, transpose, value.sourceObject as FloatBuffer)
    }

    override fun glUniformMatrix4x3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        GL21.glUniformMatrix4x3fv(location, transpose, value.sourceObject as FloatBuffer)
    }

    override fun glBlitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int,
                                   mask: Int, filter: Int) {
        GL30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter)
    }

    override fun glBindFramebuffer(target: Int, framebuffer: Int) {
        GL30.glBindFramebuffer(target, framebuffer)
    }

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) {
        GL30.glBindRenderbuffer(target, renderbuffer)
    }

    override fun glCheckFramebufferStatus(target: Int): Int {
        return GL30.glCheckFramebufferStatus(target)
    }

    override fun glDeleteFramebuffers(framebuffers: IntArray) {
        GL30.glDeleteFramebuffers(framebuffers)
    }

    override fun glDeleteRenderbuffers(renderbuffers: IntArray) {
        GL30.glDeleteRenderbuffers(renderbuffers)
    }

    override fun glGenerateMipmap(target: Int) {
        GL30.glGenerateMipmap(target)
    }

    override fun glGenFramebuffers(framebuffers: IntArray) {
        GL30.glGenFramebuffers(framebuffers)
    }

    override fun glGetRenderbufferParameteriv(target: Int, pname: Int, params: IntArray) {
        GL30.glGetRenderbufferParameteriv(target, pname, params)
    }

    override fun glIsFramebuffer(framebuffer: Int): Boolean {
        return GL30.glIsFramebuffer(framebuffer)
    }

    override fun glIsRenderbuffer(renderbuffer: Int): Boolean {
        return GL30.glIsRenderbuffer(renderbuffer)
    }

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        GL30.glRenderbufferStorage(target, internalformat, width, height)
    }

    override fun glRenderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) {
        GL30.glRenderbufferStorageMultisample(target, samples, internalformat, width, height)
    }

    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) {
        GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level)
    }

    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) {
        GL30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer)
    }

    override fun glFramebufferTextureLayer(target: Int, attachment: Int, texture: Int, level: Int, layer: Int) {
        GL30.glFramebufferTextureLayer(target, attachment, texture, level, layer)
    }

    override fun glFlushMappedBufferRange(target: Int, offset: Int, length: Int) {
        GL30.glFlushMappedBufferRange(target, offset.toLong(), length.toLong())
    }

    override fun glDeleteVertexArrays(arrays: IntArray) {
        GL30.glDeleteVertexArrays(arrays)
    }

    override fun glDeleteVertexArrays(id: Int) {
        GL30.glDeleteVertexArrays(id)
    }

    override fun glGenVertexArrays(arrays: IntArray) {
        GL30.glGenVertexArrays(arrays)
    }

    override fun glGenVertexArrays(): Int = GL30.glGenVertexArrays()

    override fun glIsVertexArray(array: Int): Boolean {
        return GL30.glIsVertexArray(array)
    }

    override fun glBeginTransformFeedback(primitiveMode: Int) {
        GL30.glBeginTransformFeedback(primitiveMode)
    }

    override fun glEndTransformFeedback() {
        GL30.glEndTransformFeedback()
    }

    override fun glBindBufferRange(target: Int, index: Int, buffer: Int, offset: Int, size: Int) {
        GL30.glBindBufferRange(target, index, buffer, offset.toLong(), size.toLong())
    }

    override fun glBindBufferBase(target: Int, index: Int, buffer: Int) {
        GL30.glBindBufferBase(target, index, buffer)
    }

    override fun glTransformFeedbackVaryings(program: Int, varyings: Array<String>, bufferMode: Int) {
        GL30.glTransformFeedbackVaryings(program, varyings, bufferMode)
    }

    override fun glVertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) {
        GL30.glVertexAttribIPointer(index, size, type, stride, offset.toLong())
    }

    override fun glGetVertexAttribIiv(index: Int, pname: Int, params: IntArray) {
        GL30.glGetVertexAttribIiv(index, pname, params)
    }

    override fun glGetVertexAttribIuiv(index: Int, pname: Int, params: IntArray) {
        GL30.glGetVertexAttribIuiv(index, pname, params)
    }

    override fun glVertexAttribI4i(index: Int, x: Int, y: Int, z: Int, w: Int) {
        GL30.glVertexAttribI4i(index, x, y, z, w)
    }

    override fun glVertexAttribI4ui(index: Int, x: Int, y: Int, z: Int, w: Int) {
        GL30.glVertexAttribI4ui(index, x, y, z, w)
    }

    override fun glGetUniformuiv(program: Int, location: Int, params: IntArray) {
        GL30.glGetUniformuiv(program, location, params)
    }

    override fun glGetFragDataLocation(program: Int, name: String): Int {
        return GL30.glGetFragDataLocation(program, name)
    }

    override fun glUniform1uiv(location: Int, count: Int, value: IIntData) {
        GL30.glUniform1uiv(location, value.sourceObject as IntBuffer)
    }

    override fun glUniform3uiv(location: Int, count: Int, value: IIntData) {
        GL30.glUniform3uiv(location, value.sourceObject as IntBuffer)
    }

    override fun glUniform4uiv(location: Int, count: Int, value: IIntData) {
        GL30.glUniform4uiv(location, value.sourceObject as IntBuffer)
    }

    override fun glClearBufferiv(buffer: Int, drawbuffer: Int, value: IIntData) {
        GL30.glClearBufferiv(buffer, drawbuffer, value.sourceObject as IntBuffer)
    }

    override fun glClearBufferuiv(buffer: Int, drawbuffer: Int, value: IIntData) {
        GL30.glClearBufferuiv(buffer, drawbuffer, value.sourceObject as IntBuffer)
    }

    override fun glClearBufferfv(buffer: Int, drawbuffer: Int, value: IFloatData) {
        GL30.glClearBufferfv(buffer, drawbuffer, value.sourceObject as FloatBuffer)
    }

    override fun glClearBufferfi(buffer: Int, drawbuffer: Int, depth: Float, stencil: Int) {
        GL30.glClearBufferfi(buffer, drawbuffer, depth, stencil)
    }

    override fun glGetStringi(name: Int, index: Int): String {
        return GL30.glGetStringi(name, index)!!
    }

    override fun glCopyBufferSubData(readTarget: Int, writeTarget: Int, readOffset: Int, writeOffset: Int, size: Int) {
        GL31.glCopyBufferSubData(readTarget, writeTarget, readOffset.toLong(), writeOffset.toLong(), size.toLong())
    }

    override fun glGetUniformIndices(program: Int, uniformNames: Array<String>, uniformIndices: IIntData) {
        GL31.glGetUniformIndices(program, uniformNames, uniformIndices.sourceObject as IntBuffer)
    }

    override fun glGetActiveUniformsiv(program: Int, uniformCount: Int, uniformIndices: IntArray, pname: Int, params: IntArray) {
        GL31.glGetActiveUniformsiv(program, uniformIndices, pname, params)
    }

    override fun glGetUniformBlockIndex(program: Int, uniformBlockName: String): Int {
        return GL31.glGetUniformBlockIndex(program, uniformBlockName)
    }

    override fun glGetActiveUniformBlockiv(program: Int, uniformBlockIndex: Int, pname: Int): Int {
        return GL31.glGetActiveUniformBlocki(program, uniformBlockIndex, pname)
    }

    override fun glGetActiveUniformBlockName(program: Int, uniformBlockIndex: Int, length: IntArray, uniformBlockName: IByteData) {
        GL31.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName.sourceObject as ByteBuffer)
    }

    override fun glGetActiveUniformBlockName(program: Int, uniformBlockIndex: Int): String {
        return GL31.glGetActiveUniformBlockName(program, uniformBlockIndex, 1024)
    }

    override fun glUniformBlockBinding(program: Int, uniformBlockIndex: Int, uniformBlockBinding: Int) {
        GL31.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding)
    }

    override fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) {
        GL31.glDrawArraysInstanced(mode, first, count, instanceCount)
    }

    override fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) {
        GL31.glDrawElementsInstanced(mode, count, type, indicesOffset.toLong(), instanceCount)
    }

    override fun glGetInteger64v(pname: Int, params: LongArray) {
        GL32.glGetInteger64v(pname, params)
    }

    override fun glGetBufferParameteri64v(target: Int, pname: Int, params: LongArray) {
        params[0] = GL32.glGetBufferParameteri64(target, pname)
    }

    override fun glGenSamplers(): Int = GL33.glGenSamplers()

    override fun glGenSamplers(samplers: IntArray) {
        GL33.glGenSamplers(samplers)
    }

    override fun glDeleteSamplers(samplers: IntArray) {
        GL33.glDeleteSamplers(samplers)
    }

    override fun glDeleteSamplers(sampler: Int) {
        GL33.glDeleteSamplers(sampler)
    }

    override fun glIsSampler(sampler: Int): Boolean {
        return GL33.glIsSampler(sampler)
    }

    override fun glBindSampler(unit: Int, sampler: Int) {
        GL33.glBindSampler(unit, sampler)
    }

    override fun glSamplerParameteri(sampler: Int, pname: Int, param: Int) {
        GL33.glSamplerParameteri(sampler, pname, param)
    }

    override fun glSamplerParameteriv(sampler: Int, pname: Int, param: IntArray) {
        GL33.glSamplerParameteriv(sampler, pname, param)
    }

    override fun glSamplerParameterf(sampler: Int, pname: Int, param: Float) {
        GL33.glSamplerParameterf(sampler, pname, param)
    }

    override fun glSamplerParameterfv(sampler: Int, pname: Int, param: FloatArray) {
        GL33.glSamplerParameterfv(sampler, pname, param)
    }

    override fun glGetSamplerParameteriv(sampler: Int, pname: Int, params: IntArray) {
        GL33.glGetSamplerParameterIiv(sampler, pname, params)
    }

    override fun glGetSamplerParameterfv(sampler: Int, pname: Int, params: FloatArray) {
        GL33.glGetSamplerParameterfv(sampler, pname, params)
    }

    override fun glVertexAttribDivisor(index: Int, divisor: Int) {
        GL33.glVertexAttribDivisor(index, divisor)
    }

    override fun glBindTransformFeedback(target: Int, id: Int) {
        GL40.glBindTransformFeedback(target, id)
    }

    override fun glDeleteTransformFeedbacks(ids: IntArray) {
        GL40.glDeleteTransformFeedbacks(ids)
    }

    override fun glDeleteTransformFeedbacks(id: Int) {
        GL40.glDeleteTransformFeedbacks(id)
    }

    override fun glGenTransformFeedbacks(ids: IntArray) {
        GL40.glGenTransformFeedbacks(ids)
    }

    override fun glGenTransformFeedbacks(): Int = GL40.glGenTransformFeedbacks()

    override fun glIsTransformFeedback(id: Int): Boolean {
        return GL40.glIsTransformFeedback(id)
    }

    override fun glPauseTransformFeedback() {
        GL40.glPauseTransformFeedback()
    }

    override fun glResumeTransformFeedback() {
        GL40.glResumeTransformFeedback()
    }

    override fun glProgramParameteri(program: Int, pname: Int, value: Int) {
        GL41.glProgramParameteri(program, pname, value)
    }

    override fun glInvalidateFramebuffer(target: Int, numAttachments: Int, attachments: IntArray) {
        GL43.glInvalidateFramebuffer(target, attachments)
    }

    override fun glInvalidateSubFramebuffer(target: Int, numAttachments: Int, attachments: IntArray, x: Int, y: Int, width: Int, height: Int) {
        GL43.glInvalidateSubFramebuffer(target, attachments, x, y, width, height)
    }
}