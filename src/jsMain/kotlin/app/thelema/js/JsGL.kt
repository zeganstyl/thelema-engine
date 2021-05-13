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

import org.khronos.webgl.*
import app.thelema.data.IByteData
import app.thelema.data.IFloatData
import app.thelema.gl.AbstractGL
import app.thelema.gl.GL
import app.thelema.img.IImageData
import org.w3c.dom.HTMLImageElement

/**
 * @author zeganstyl
 * */
class JsGL(
    val gl: WebGL2RenderingContext,
    override val majVer: Int = 1,
    override val minVer: Int = 0,
    override val glslVer: Int = 100
): AbstractGL() {
    val textures = GLObjectArray<WebGLTexture, TextureWrap> { TextureWrap(it) }
    val programs = GLObjectArray<WebGLProgram, ProgramWrap> { ProgramWrap(it) }
    val buffers = GLObjectArray<WebGLBuffer, BufferWrap> { BufferWrap(it) }
    val vertexArrays = GLObjectArray<WebGLVertexArrayObject, VertexArrayWrap> { VertexArrayWrap(it) }
    val frameBuffers = GLObjectArray<WebGLFramebuffer, FrameBufferWrap> { FrameBufferWrap(it) }
    val renderBuffers = GLObjectArray<WebGLRenderbuffer, RenderBufferWrap> { RenderBufferWrap(it) }
    val shaders = GLObjectArray<WebGLShader, ShaderWrap> { ShaderWrap(it) }

    val floatArraysCache = HashMap<Int, Array<Float>>()

    override val mainFrameBufferWidth: Int
        get() = gl.drawingBufferWidth

    override val mainFrameBufferHeight: Int
        get() = gl.drawingBufferHeight

    override val mainFrameBufferHandle: Int
        get() = 0

    override val relVer: Int = 0

    override val glesMajVer: Int
        get() = majVer + 1

    override val glesMinVer: Int
        get() = 0

    override val isGLES: Boolean
        get() = true

    override val maxAnisotropicFilterLevel: Float
        get() = 1f

    override fun glBlendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int) {
        gl.blendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha)
    }

    override fun glBlendEquationSeparate(modeRGB: Int, modeAlpha: Int) {
        gl.blendEquationSeparate(modeRGB, modeAlpha)
    }

    override fun glActiveTextureBase(value: Int) {
        gl.activeTexture(value)
    }

    override fun glUseProgramBase(value: Int) {
        gl.useProgram(programs.gl(value))
    }

    override fun glBindBufferBase(target: Int, buffer: Int) {
        gl.bindBuffer(target, buffers.gl(buffer))
    }

    override fun glBindVertexArrayBase(value: Int) {
        gl.bindVertexArray(vertexArrays.gl(value))
    }

    override fun glCullFaceBase(value: Int) {
        gl.cullFace(value)
    }

    override fun glBlendFuncBase(factorS: Int, factorD: Int) {
        gl.blendFunc(factorS, factorD)
    }

    override fun glEnableBase(value: Int) {
        gl.enable(value)
    }

    override fun glDisableBase(value: Int) {
        gl.disable(value)
    }

    override fun glDepthFuncBase(value: Int) {
        gl.depthFunc(value)
    }

    override fun glDepthMaskBase(value: Boolean) {
        gl.depthMask(value)
    }

    override fun glBindTextureBase(target: Int, texture: Int) {
        gl.bindTexture(target, textures.gl(texture))
    }

    override fun isExtensionSupported(extension: String): Boolean =
        gl.getSupportedExtensions()?.contains(extension) ?: false

    override fun enableExtension(extension: String): Boolean =
        gl.getExtension(extension) != null

    override fun glGetVertexAttribIiv(index: Int, pname: Int, params: IntArray) {
        params[0] = gl.getVertexAttrib(index, pname) as Int
    }

    override fun glCreateProgram(): Int {
        return programs.new(gl.createProgram())
    }

    override fun glClear(mask: Int) {
        gl.clear(mask)
    }

    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        gl.clearColor(red, green, blue, alpha)
    }

    override fun glDeleteTexture(texture: Int) {
        gl.deleteTexture(textures.gl(texture))
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: IByteData) {
        gl.drawElements(mode, count, type, indices.sourceObject.unsafeCast<ArrayBufferView>().byteOffset)
    }

    override fun glDrawArrays(mode: Int, first: Int, count: Int) {
        gl.drawArrays(mode, first, count)
    }

    override fun glLinkProgram(program: Int) {
        gl.linkProgram(programs.gl(program))
    }

    override fun glGenTexture(): Int {
        return textures.new(gl.createTexture())
    }

    override fun glGetUniformLocation(program: Int, name: String): Int {
        val programDesc = programs.wrap(program)
        return if (programDesc != null) {
            var loc = programDesc.uniformLocationsMap[name]
            if (loc == null) {
                val glLoc = gl.getUniformLocation(programDesc.gl, name)
                if (glLoc != null) {
                    val newId = programDesc.uniformLocations.size
                    loc = UniformLocationWrap(newId, glLoc)
                    programDesc.uniformLocations.add(loc)
                    programDesc.uniformLocationsMap[name] = loc
                    newId
                } else -1
            } else loc.id
        } else -1
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
        gl.texImage2D(target, level, internalformat, width, height, border, format, type, pixels?.sourceObject?.unsafeCast<ArrayBufferView>())
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
        image: IImageData
    ) {
        gl.texImage2D(target, level, internalformat, format, type, image.sourceObject as HTMLImageElement)
    }

    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) {
        gl.framebufferTexture2D(target, attachment, textarget, textures.gl(texture), level)
    }

    override fun glGenRenderbuffer(): Int {
        return renderBuffers.new(gl.createRenderbuffer())
    }

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) {
        gl.bindRenderbuffer(target, renderBuffers.gl(renderbuffer))
    }

    override fun glDeleteRenderbuffer(renderbuffer: Int) {
        gl.deleteRenderbuffer(renderBuffers.gl(renderbuffer))
    }

    override fun glIsRenderbuffer(renderbuffer: Int): Boolean {
        return gl.isRenderbuffer(renderBuffers.gl(renderbuffer))
    }

    override fun glGetRenderbufferParameteriv(target: Int, pname: Int, params: IntArray) {
        val param = gl.getRenderbufferParameter(target, pname)
        params[0] = when (param) {
            is Int -> param
            is Boolean -> if (param) 1 else 0
            else -> throw IllegalArgumentException("Can't get render buffer parameter $pname")
        }
    }

    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) {
        gl.framebufferRenderbuffer(target, attachment, renderbuffertarget, renderBuffers.gl(renderbuffer))
    }

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        gl.renderbufferStorage(target, internalformat, width, height)
    }

    override fun glRenderbufferStorageMultisample(
        target: Int,
        samples: Int,
        internalformat: Int,
        width: Int,
        height: Int
    ) {
        gl.renderbufferStorage(target, internalformat, width, height)
    }

    override fun glCheckFramebufferStatus(target: Int): Int {
        return gl.checkFramebufferStatus(target)
    }

    override fun glGenerateMipmap(target: Int) {
        gl.generateMipmap(target)
    }

    override fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        gl.scissor(x, y, width, height)
    }

    override fun glTexParameterf(target: Int, pname: Int, param: Float) {
        gl.texParameterf(target, pname, param)
    }

    override fun glTexParameteri(target: Int, pname: Int, param: Int) {
        gl.texParameteri(target, pname, param)
    }

    override fun glGetProgramInfoLog(program: Int): String {
        return gl.getProgramInfoLog(programs.gl(program)) ?: ""
    }

    override fun glGenBuffer(): Int {
        return buffers.new(gl.createBuffer())
    }

    override fun glViewport(x: Int, y: Int, width: Int, height: Int) {
        gl.viewport(x, y, width, height)
    }

    override fun glBlendEquation(mode: Int) {
        gl.blendEquation(mode)
    }

    override fun glBufferData(target: Int, size: Int, data: IByteData?, usage: Int) {
        gl.bufferData(target, data?.sourceObject?.unsafeCast<ArrayBufferView>(), usage)
    }

    override fun glGetIntegerv(pname: Int, params: IntArray) {
        val param = gl.getParameter(pname)
        params[0] = when (param) {
            is Int -> param
            is Boolean -> if (param) 1 else 0
            else -> throw IllegalArgumentException("Can't get parameter $pname")
        }
    }

    override fun glBindFramebuffer(target: Int, framebuffer: Int) {
        gl.bindFramebuffer(target, frameBuffers.gl(framebuffer))
    }

    override fun glCompileShader(shader: Int) {
        gl.compileShader(shaders.gl(shader))
    }

    override fun glBindAttribLocation(program: Int, index: Int, name: String) {
        gl.bindAttribLocation(programs.gl(program), index, name)
    }

    override fun glCreateShader(type: Int): Int {
        return shaders.new(gl.createShader(type))
    }

    override fun glDeleteBuffer(buffer: Int) {
        gl.deleteBuffer(buffers.delete(buffer))
    }

    override fun glDeleteFramebuffer(framebuffer: Int) {
        gl.deleteFramebuffer(frameBuffers.delete(framebuffer))
    }

    override fun glDeleteProgram(program: Int) {
        gl.deleteProgram(programs.delete(program))
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) {
        gl.drawElements(mode, count, type, indices)
    }

    override fun glGenFramebuffer(): Int {
        return frameBuffers.new(gl.createFramebuffer())
    }

    override fun glDeleteShader(shader: Int) {
        gl.deleteShader(shaders.delete(shader))
    }

    override fun glDetachShader(program: Int, shader: Int) {
        gl.detachShader(programs.gl(program), shaders.gl(shader))
    }

    override fun glEnableVertexAttribArray(index: Int) {
        gl.enableVertexAttribArray(index)
    }

    override fun glDisableVertexAttribArray(index: Int) {
        gl.disableVertexAttribArray(index)
    }

    override fun glGetFloatv(pname: Int, params: FloatArray) {
        params[0] = gl.getParameter(pname) as Float
    }

    private fun uLoc(location: Int) =
        programs.wrap(GL.program)?.uniformLocations?.getOrNull(location)?.gl

    override fun glUniform1i(location: Int, x: Int) { gl.uniform1i(uLoc(location), x) }
    override fun glUniform2i(location: Int, x: Int, y: Int) { gl.uniform2i(uLoc(location), x, y) }
    override fun glUniform3i(location: Int, x: Int, y: Int, z: Int) { gl.uniform3i(uLoc(location), x, y, z) }
    override fun glUniform4i(location: Int, x: Int, y: Int, z: Int, w: Int) { gl.uniform4i(uLoc(location), x, y, z, w) }
    override fun glUniform1f(location: Int, x: Float) { gl.uniform1f(uLoc(location), x) }
    override fun glUniform2f(location: Int, x: Float, y: Float) { gl.uniform2f(uLoc(location), x, y) }
    override fun glUniform3f(location: Int, x: Float, y: Float, z: Float) { gl.uniform3f(uLoc(location), x, y, z) }
    override fun glUniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) { gl.uniform4f(uLoc(location), x, y, z, w) }

    private fun floatArrayConv(input: FloatArray): Array<Float> {
        val size = input.size
        var out = floatArraysCache[size]
        if (out == null) {
            out = Array(size) { input[it] }
            floatArraysCache[size] = out
        } else {
            for (i in 0 until size) {
                out[i] = input[i]
            }
        }
        return out
    }

    override fun glUniform1fv(location: Int, count: Int, v: FloatArray) { gl.uniform1fv(uLoc(location), floatArrayConv(v)) }
    override fun glUniform1fv(location: Int, count: Int, v: FloatArray, offset: Int) { gl.uniform1fv(uLoc(location), floatArrayConv(v)) }
    override fun glUniform2fv(location: Int, count: Int, v: FloatArray) { gl.uniform2fv(uLoc(location), floatArrayConv(v)) }
    override fun glUniform2fv(location: Int, count: Int, v: FloatArray, offset: Int) { gl.uniform2fv(uLoc(location), floatArrayConv(v)) }
    override fun glUniform3fv(location: Int, count: Int, v: FloatArray) { gl.uniform3fv(uLoc(location), floatArrayConv(v)) }
    override fun glUniform3fv(location: Int, count: Int, v: FloatArray, offset: Int) { gl.uniform3fv(uLoc(location), floatArrayConv(v)) }
    override fun glUniform4fv(location: Int, count: Int, v: FloatArray) { gl.uniform4fv(uLoc(location), floatArrayConv(v)) }
    override fun glUniform4fv(location: Int, count: Int, v: FloatArray, offset: Int) { gl.uniform4fv(uLoc(location), floatArrayConv(v)) }

    override fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        gl.uniformMatrix3fv(uLoc(location), transpose, floatArrayConv(value))
    }

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        gl.uniformMatrix4fv(uLoc(location), transpose, floatArrayConv(value))
    }

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        gl.uniformMatrix4fv(uLoc(location), transpose, value.sourceObject as Float32Array)
    }

    override fun glIsEnabled(cap: Int): Boolean = gl.isEnabled(cap)

    override fun glShaderSource(shader: Int, string: String) {
        gl.shaderSource(shaders.gl(shader), string)
    }

    override fun glGetShaderiv(shader: Int, pname: Int, params: IntArray) {
        val param = gl.getShaderParameter(shaders.gl(shader), pname)
        params[0] = when (param) {
            is Int -> param
            is Boolean -> if (param) 1 else 0
            else -> throw IllegalArgumentException("Can't get shader parameter $pname")
        }
    }

    override fun glGetProgramiv(program: Int, pname: Int, params: IntArray) {
        val param = gl.getProgramParameter(programs.gl(program), pname)
        params[0] = when (param) {
            is Int -> param
            is Boolean -> if (param) 1 else 0
            else -> throw IllegalArgumentException("Can't get program parameter $pname")
        }
    }

    override fun glGetActiveUniform(program: Int, index: Int, size: IntArray, type: IntArray): String {
        val info = gl.getActiveUniform(programs.gl(program), index)
        if (info != null) {
            size[0] = info.size
            type[0] = info.type
        }
        return info?.name ?: ""
    }

    override fun glGetShaderInfoLog(shader: Int): String {
        return gl.getShaderInfoLog(shaders.gl(shader)) ?: ""
    }

    override fun glAttachShader(program: Int, shader: Int) {
        gl.attachShader(programs.gl(program), shaders.gl(shader))
    }

    override fun glGetActiveAttrib(program: Int, index: Int, size: IntArray, type: IntArray): String {
        val info = gl.getActiveAttrib(programs.gl(program), index)
        if (info != null) {
            size[0] = info.size
            type[0] = info.type
        }
        return info?.name ?: ""
    }

    override fun glGetAttribLocation(program: Int, name: String): Int {
        return gl.getAttribLocation(programs.gl(program), name)
    }

    override fun glVertexAttribPointer(
        indx: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        ptr: IByteData
    ) {
        gl.vertexAttribPointer(indx, size, type, normalized, stride, ptr.sourceObject.unsafeCast<ArrayBufferView>().byteOffset)
    }

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: Int) {
        gl.vertexAttribPointer(indx, size, type, normalized, stride, ptr)
    }

    override fun glGenVertexArrays(): Int {
        return vertexArrays.new(gl.createVertexArray())
    }

    override fun glIsVertexArray(array: Int): Boolean {
        return gl.isVertexArray(vertexArrays.gl(array))
    }

    override fun glDeleteVertexArrays(id: Int) {
        gl.deleteVertexArray(vertexArrays.delete(id))
    }

    override fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) {
        gl.drawArraysInstanced(mode, first, count, instanceCount)
    }

    override fun glDrawBuffers(n: Int, bufs: IntArray) {
        gl.drawBuffers(bufs)
    }

    override fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) {
        gl.drawElementsInstanced(mode, count, type, indicesOffset, instanceCount)
    }

    override fun glVertexAttribDivisor(index: Int, divisor: Int) {
        gl.vertexAttribDivisor(index, divisor)
    }

    override fun glGetError(): Int = gl.getError()


    class ProgramWrap(
        override val gl: WebGLProgram,
        val uniformLocations: ArrayList<UniformLocationWrap> = ArrayList(),
        val uniformLocationsMap: HashMap<String, UniformLocationWrap> = HashMap()
    ): GLObject<WebGLProgram>()

    class UniformLocationWrap(
        val id: Int,
        override val gl: WebGLUniformLocation
    ): GLObject<WebGLUniformLocation>()

    class BufferWrap(
        override val gl: WebGLBuffer
    ): GLObject<WebGLBuffer>()

    class FrameBufferWrap(
        override val gl: WebGLFramebuffer
    ): GLObject<WebGLFramebuffer>()

    class RenderBufferWrap(
        override val gl: WebGLRenderbuffer
    ): GLObject<WebGLRenderbuffer>()

    class ShaderWrap(
        override val gl: WebGLShader
    ): GLObject<WebGLShader>()

    class TextureWrap(
        override val gl: WebGLTexture
    ): GLObject<WebGLTexture>()

    class VertexArrayWrap(
        override val gl: WebGLVertexArrayObject
    ): GLObject<WebGLVertexArrayObject>()

    abstract class GLObject<T> {
        abstract val gl: T
    }

    class GLObjectArray<GLT, WrapT: GLObject<GLT>>(val buildWrap: (gl: GLT) -> WrapT): ArrayList<WrapT?>() {
        fun gl(id: Int): GLT? = getOrNull(id - 1)?.gl

        fun wrap(id: Int): WrapT? = getOrNull(id - 1)

        fun new(gl: GLT?): Int {
            return if (gl != null) {
                add(buildWrap(gl))
                size
            } else -1
        }

        fun delete(id: Int): GLT? {
            val index = id - 1
            val program = gl(index)
            set(index, null)
            return program
        }
    }
}