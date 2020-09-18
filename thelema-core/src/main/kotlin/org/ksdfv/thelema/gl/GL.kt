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

package org.ksdfv.thelema.gl

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.data.IFloatData
import org.ksdfv.thelema.data.IIntData
import org.ksdfv.thelema.img.IImageData
import org.ksdfv.thelema.kx.ThreadLocal

/** @author zeganstyl */
@ThreadLocal
object GL: IGL {
    lateinit var proxy: IGL

    /** Default frame buffer width */
    override val mainFrameBufferWidth: Int
        get() = proxy.mainFrameBufferWidth

    /** Default frame buffer height */
    override val mainFrameBufferHeight: Int
        get() = proxy.mainFrameBufferHeight

    override val mainFrameBufferHandle: Int
        get() = proxy.mainFrameBufferHandle

    override val majVer: Int
        get() = proxy.majVer
    override val minVer: Int
        get() = proxy.minVer
    override val relVer: Int
        get() = proxy.relVer
    override val glslVer: Int
        get() = proxy.glslVer

    override val glesMajVer: Int
        get() = proxy.glesMajVer
    override val glesMinVer: Int
        get() = proxy.glesMinVer
    override val isGLES: Boolean
        get() = proxy.isGLES

    override val maxAnisotropicFilterLevel: Float
        get() = proxy.maxAnisotropicFilterLevel

    override var program: Int
        get() = proxy.program
        set(value) { proxy.program = value }

    override var activeTexture: Int
        get() = proxy.activeTexture
        set(value) { proxy.activeTexture = value }

    override var arrayBuffer: Int
        get() = proxy.arrayBuffer
        set(value) { proxy.arrayBuffer = value }

    override var elementArrayBuffer: Int
        get() = proxy.elementArrayBuffer
        set(value) { proxy.elementArrayBuffer = value }

    override var vertexArray: Int
        get() = proxy.vertexArray
        set(value) { proxy.vertexArray = value }

    override val textureUnits: List<Int>
        get() = proxy.textureUnits

    override var isCullFaceEnabled: Boolean
        get() = proxy.isCullFaceEnabled
        set(value) { proxy.isCullFaceEnabled = value }

    override var cullFaceMode: Int
        get() = proxy.cullFaceMode
        set(value) { proxy.cullFaceMode = value }

    override var isBlendEnabled: Boolean
        get() = proxy.isBlendEnabled
        set(value) { proxy.isBlendEnabled = value }

    override var blendFactorS: Int
        get() = proxy.blendFactorS
        set(value) { proxy.blendFactorS = value }

    override var blendFactorD: Int
        get() = proxy.blendFactorD
        set(value) { proxy.blendFactorD = value }

    override var isDepthTestEnabled: Boolean
        get() = proxy.isDepthTestEnabled
        set(value) { proxy.isDepthTestEnabled = value }

    override var depthFunc: Int
        get() = proxy.depthFunc
        set(value) { proxy.depthFunc = value }

    override var isDepthMaskEnabled: Boolean
        get() = proxy.isDepthMaskEnabled
        set(value) { proxy.isDepthMaskEnabled = value }

    override fun initGL() = proxy.initGL()

    override fun getNextTextureUnit(): Int = proxy.getNextTextureUnit()

    override fun resetTextureUnitCounter() = proxy.resetTextureUnitCounter()

    override fun setSimpleAlphaBlending() = proxy.setSimpleAlphaBlending()

    override fun call(function: () -> Unit) = proxy.call(function)

    override fun render(function: () -> Unit) = proxy.render(function)

    override fun removeSingleCall(function: () -> Unit) = proxy.removeSingleCall(function)

    override fun removeRenderCall(function: () -> Unit) = proxy.removeRenderCall(function)

    override fun clearSingleCalls() = proxy.clearSingleCalls()

    override fun clearRenderCalls() = proxy.clearRenderCalls()

    override fun runSingleCalls() = proxy.runSingleCalls()

    override fun runRenderCalls() = proxy.runRenderCalls()

    override fun isExtensionSupported(extension: String) = proxy.isExtensionSupported(extension)

    override fun enableExtension(extension: String): Boolean =
        proxy.enableExtension(extension)

    override fun callExtensionFunction(extension: String, functionName: String, args: List<Any?>): Any? =
        proxy.callExtensionFunction(extension, functionName, args)

    override fun setExtensionParam(extension: String, paramName: String, value: Any?) =
        proxy.setExtensionParam(extension, paramName, value)

    override fun getExtensionParam(extension: String, paramName: String): Any? =
        proxy.getExtensionParam(extension, paramName)

    override fun glTexImage3D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        depth: Int,
        border: Int,
        format: Int,
        type: Int,
        pixels: IByteData?
    ) = proxy.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels)

    override fun glGenRenderbuffers(renderbuffers: IntArray) = proxy.glGenRenderbuffers(renderbuffers)

    override fun getErrorString(error: Int): String = proxy.getErrorString(error)

    override fun glReadBuffer(mode: Int) = proxy.glReadBuffer(mode)

    override fun glDrawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, indices: IByteData) =
        proxy.glDrawRangeElements(mode, start, end, count, type, indices)

    override fun glDrawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, offset: Int) =
        proxy.glDrawRangeElements(mode, start, end, count, type, offset)

    override fun glTexImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, border: Int, format: Int, type: Int, offset: Int) =
        proxy.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, offset)

    override fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, pixels: IByteData) =
        proxy.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels)

    override fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, offset: Int) =
        proxy.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset)

    override fun glCopyTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, x: Int, y: Int, width: Int, height: Int) =
        proxy.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height)

    override fun glGenQueries(ids: IntArray) = proxy.glGenQueries(ids)

    override fun glGenQueries(): Int = proxy.glGenQueries()

    override fun glDeleteQueries(ids: IntArray) = proxy.glDeleteQueries(ids)

    override fun glDeleteQueries(id: Int) = proxy.glDeleteQueries(id)

    override fun glIsQuery(id: Int): Boolean = proxy.glIsQuery(id)

    override fun glBeginQuery(target: Int, id: Int) = proxy.glBeginQuery(target, id)

    override fun glEndQuery(target: Int) = proxy.glEndQuery(target)

    override fun glGetQueryiv(target: Int, pname: Int, params: IntArray) = proxy.glGetQueryiv(target, pname, params)

    override fun glGetQueryObjectuiv(id: Int, pname: Int, params: IntArray) = proxy.glGetQueryObjectuiv(id, pname, params)

    override fun glUnmapBuffer(target: Int): Boolean = proxy.glUnmapBuffer(target)

    override fun glGetBufferPointerv(target: Int, pname: Int): IntArray = proxy.glGetBufferPointerv(target, pname)

    override fun glDrawBuffers(n: Int, bufs: IntArray) = proxy.glDrawBuffers(n, bufs)

    override fun glUniformMatrix2x3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
        proxy.glUniformMatrix2x3fv(location, count, transpose, value)

    override fun glUniformMatrix3x2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
        proxy.glUniformMatrix3x2fv(location, count, transpose, value)

    override fun glUniformMatrix2x4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
        proxy.glUniformMatrix2x4fv(location, count, transpose, value)

    override fun glUniformMatrix4x2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
        proxy.glUniformMatrix4x2fv(location, count, transpose, value)

    override fun glUniformMatrix3x4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
        proxy.glUniformMatrix3x4fv(location, count, transpose, value)

    override fun glUniformMatrix4x3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
        proxy.glUniformMatrix4x3fv(location, count, transpose, value)

    override fun glBlitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: Int, filter: Int) =
        proxy.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter)

    override fun glRenderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) =
        proxy.glRenderbufferStorageMultisample(target, samples, internalformat, width, height)

    override fun glFramebufferTextureLayer(target: Int, attachment: Int, texture: Int, level: Int, layer: Int) =
        proxy.glFramebufferTextureLayer(target, attachment, texture, level, layer)

    override fun glFlushMappedBufferRange(target: Int, offset: Int, length: Int) = proxy.glFlushMappedBufferRange(target, offset, length)

    override fun glBindVertexArray(array: Int) = proxy.glBindVertexArray(array)

    override fun glDeleteVertexArrays(id: Int) = proxy.glDeleteVertexArrays(id)

    override fun glDeleteVertexArrays(arrays: IntArray) = proxy.glDeleteVertexArrays(arrays)

    override fun glGenVertexArrays() = proxy.glGenVertexArrays()

    override fun glGenVertexArrays(arrays: IntArray) = proxy.glGenVertexArrays(arrays)

    override fun glIsVertexArray(array: Int): Boolean = proxy.glIsVertexArray(array)

    override fun glBeginTransformFeedback(primitiveMode: Int) = proxy.glBeginTransformFeedback(primitiveMode)

    override fun glEndTransformFeedback() = proxy.glEndTransformFeedback()

    override fun glBindBufferRange(target: Int, index: Int, buffer: Int, offset: Int, size: Int) =
        proxy.glBindBufferRange(target, index, buffer, offset, size)

    override fun glBindBufferBase(target: Int, index: Int, buffer: Int) =
        proxy.glBindBufferBase(target, index, buffer)

    override fun glTransformFeedbackVaryings(program: Int, varyings: Array<String>, bufferMode: Int) =
        proxy.glTransformFeedbackVaryings(program, varyings, bufferMode)

    override fun glVertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) =
        proxy.glVertexAttribIPointer(index, size, type, stride, offset)

    override fun glGetVertexAttribIiv(index: Int, pname: Int, params: IntArray) =
        proxy.glGetVertexAttribIiv(index, pname, params)

    override fun glGetVertexAttribIuiv(index: Int, pname: Int, params: IntArray) =
        proxy.glGetVertexAttribIuiv(index, pname, params)

    override fun glVertexAttribI4i(index: Int, x: Int, y: Int, z: Int, w: Int) =
        proxy.glVertexAttribI4i(index, x, y, z, w)

    override fun glVertexAttribI4ui(index: Int, x: Int, y: Int, z: Int, w: Int) =
        proxy.glVertexAttribI4ui(index, x, y, z, w)

    override fun glGetUniformuiv(program: Int, location: Int, params: IntArray) =
        proxy.glGetUniformuiv(program, location, params)

    override fun glGetFragDataLocation(program: Int, name: String): Int =
        proxy.glGetFragDataLocation(program, name)

    override fun glUniform1uiv(location: Int, count: Int, value: IIntData) =
        proxy.glUniform1uiv(location, count, value)

    override fun glUniform3uiv(location: Int, count: Int, value: IIntData) =
        proxy.glUniform3uiv(location, count, value)

    override fun glUniform4uiv(location: Int, count: Int, value: IIntData) =
        proxy.glUniform4uiv(location, count, value)

    override fun glClearBufferiv(buffer: Int, drawbuffer: Int, value: IIntData) =
        proxy.glClearBufferiv(buffer, drawbuffer, value)

    override fun glClearBufferuiv(buffer: Int, drawbuffer: Int, value: IIntData) =
        proxy.glClearBufferuiv(buffer, drawbuffer, value)

    override fun glClearBufferfv(buffer: Int, drawbuffer: Int, value: IFloatData) =
        proxy.glClearBufferfv(buffer, drawbuffer, value)

    override fun glClearBufferfi(buffer: Int, drawbuffer: Int, depth: Float, stencil: Int) =
        proxy.glClearBufferfi(buffer, drawbuffer, depth, stencil)

    override fun glGetStringi(name: Int, index: Int): String =
        proxy.glGetStringi(name, index)

    override fun glCopyBufferSubData(readTarget: Int, writeTarget: Int, readOffset: Int, writeOffset: Int, size: Int) =
        proxy.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size)

    override fun glGetUniformIndices(program: Int, uniformNames: Array<String>, uniformIndices: IIntData) =
        proxy.glGetUniformIndices(program, uniformNames, uniformIndices)

    override fun glGetActiveUniformsiv(program: Int, uniformCount: Int, uniformIndices: IntArray, pname: Int, params: IntArray) =
        proxy.glGetActiveUniformsiv(program, uniformCount, uniformIndices, pname, params)

    override fun glGetUniformBlockIndex(program: Int, uniformBlockName: String): Int =
        proxy.glGetUniformBlockIndex(program, uniformBlockName)

    override fun glGetActiveUniformBlockiv(program: Int, uniformBlockIndex: Int, pname: Int): Int =
        proxy.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname)

    override fun glGetActiveUniformBlockName(program: Int, uniformBlockIndex: Int, length: IntArray, uniformBlockName: IByteData) =
        proxy.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName)

    override fun glGetActiveUniformBlockName(program: Int, uniformBlockIndex: Int): String =
        proxy.glGetActiveUniformBlockName(program, uniformBlockIndex)

    override fun glUniformBlockBinding(program: Int, uniformBlockIndex: Int, uniformBlockBinding: Int) =
        proxy.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding)

    override fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) =
        proxy.glDrawArraysInstanced(mode, first, count, instanceCount)

    override fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) =
        proxy.glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount)

    override fun glGetInteger64v(pname: Int, params: LongArray) =
        proxy.glGetInteger64v(pname, params)

    override fun glGetBufferParameteri64v(target: Int, pname: Int, params: LongArray) =
        proxy.glGetBufferParameteri64v(target, pname, params)

    override fun glGenSamplers() = proxy.glGenSamplers()

    override fun glGenSamplers(samplers: IntArray) = proxy.glGenSamplers(samplers)

    override fun glDeleteSamplers(sampler: Int) = proxy.glDeleteSamplers(sampler)

    override fun glDeleteSamplers(samplers: IntArray) = proxy.glDeleteSamplers(samplers)

    override fun glIsSampler(sampler: Int): Boolean = proxy.glIsSampler(sampler)

    override fun glBindSampler(unit: Int, sampler: Int) = proxy.glBindSampler(unit, sampler)

    override fun glSamplerParameteri(sampler: Int, pname: Int, param: Int) =
        proxy.glSamplerParameteri(sampler, pname, param)

    override fun glSamplerParameteriv(sampler: Int, pname: Int, param: IntArray) =
        proxy.glSamplerParameteriv(sampler, pname, param)

    override fun glSamplerParameterf(sampler: Int, pname: Int, param: Float) =
        proxy.glSamplerParameterf(sampler, pname, param)

    override fun glSamplerParameterfv(sampler: Int, pname: Int, param: FloatArray) =
        proxy.glSamplerParameterfv(sampler, pname, param)

    override fun glGetSamplerParameteriv(sampler: Int, pname: Int, params: IntArray) =
        proxy.glGetSamplerParameteriv(sampler, pname, params)

    override fun glGetSamplerParameterfv(sampler: Int, pname: Int, params: FloatArray) =
        proxy.glGetSamplerParameterfv(sampler, pname, params)

    override fun glVertexAttribDivisor(index: Int, divisor: Int) = proxy.glVertexAttribDivisor(index, divisor)

    override fun glBindTransformFeedback(target: Int, id: Int) = proxy.glBindTransformFeedback(target, id)

    override fun glDeleteTransformFeedbacks(id: Int) = proxy.glDeleteTransformFeedbacks(id)

    override fun glDeleteTransformFeedbacks(ids: IntArray) = proxy.glDeleteTransformFeedbacks(ids)

    override fun glGenTransformFeedbacks() = proxy.glGenTransformFeedbacks()

    override fun glGenTransformFeedbacks(ids: IntArray) = proxy.glGenTransformFeedbacks(ids)

    override fun glIsTransformFeedback(id: Int): Boolean = proxy.glIsTransformFeedback(id)

    override fun glPauseTransformFeedback() = proxy.glPauseTransformFeedback()

    override fun glResumeTransformFeedback() = proxy.glResumeTransformFeedback()

    override fun glProgramParameteri(program: Int, pname: Int, value: Int) =
        proxy.glProgramParameteri(program, pname, value)

    override fun glInvalidateFramebuffer(target: Int, numAttachments: Int, attachments: IntArray) =
        proxy.glInvalidateFramebuffer(target, numAttachments, attachments)

    override fun glInvalidateSubFramebuffer(target: Int, numAttachments: Int, attachments: IntArray, x: Int, y: Int, width: Int, height: Int) =
        proxy.glInvalidateSubFramebuffer(target, numAttachments, attachments, x, y, width, height)

    override fun glActiveTexture(texture: Int) = proxy.glActiveTexture(texture)

    override fun glBindTexture(target: Int, texture: Int) = proxy.glBindTexture(target, texture)

    override fun glBlendFunc(sfactor: Int, dfactor: Int) = proxy.glBlendFunc(sfactor, dfactor)

    override fun glClear(mask: Int) = proxy.glClear(mask)

    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) =
        proxy.glClearColor(red, green, blue, alpha)

    override fun glClearDepthf(depth: Float) = proxy.glClearDepthf(depth)

    override fun glClearStencil(s: Int) = proxy.glClearStencil(s)

    override fun glColorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) =
        proxy.glColorMask(red, green, blue, alpha)

    override fun glCompressedTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, imageSize: Int, data: IByteData) =
        proxy.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data)

    override fun glCompressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, imageSize: Int, data: IByteData) =
        proxy.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data)

    override fun glCopyTexImage2D(target: Int, level: Int, internalformat: Int, x: Int, y: Int, width: Int, height: Int, border: Int) =
        proxy.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border)

    override fun glCopyTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, x: Int, y: Int, width: Int, height: Int) =
        proxy.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height)

    override fun glCullFace(mode: Int) = proxy.glCullFace(mode)

    override fun glDeleteTextures(textures: IntArray) = proxy.glDeleteTextures(textures)

    override fun glDeleteTexture(texture: Int) = proxy.glDeleteTexture(texture)

    override fun glDepthFunc(func: Int) = proxy.glDepthFunc(func)

    override fun glDepthMask(flag: Boolean) = proxy.glDepthMask(flag)

    override fun glDepthRangef(zNear: Float, zFar: Float) = proxy.glDepthRangef(zNear, zFar)

    override fun glDisable(cap: Int) = proxy.glDisable(cap)

    override fun glDrawArrays(mode: Int, first: Int, count: Int) = proxy.glDrawArrays(mode, first, count)

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: IByteData) = proxy.glDrawElements(mode, count, type, indices)

    override fun glEnable(cap: Int) = proxy.glEnable(cap)

    override fun glFinish() = proxy.glFinish()

    override fun glFlush() = proxy.glFlush()

    override fun glFrontFace(mode: Int) = proxy.glFrontFace(mode)

    override fun glGenTextures(n: Int, textures: IntArray) = proxy.glGenTextures(n, textures)

    override fun glGenTexture(): Int = proxy.glGenTexture()

    override fun glGetError(): Int = proxy.glGetError()

    override fun glGetIntegerv(pname: Int, params: IntArray) = proxy.glGetIntegerv(pname, params)

    override fun glGetString(name: Int): String? = proxy.glGetString(name)

    override fun glHint(target: Int, mode: Int) = proxy.glHint(target, mode)

    override fun glLineWidth(width: Float) = proxy.glLineWidth(width)

    override fun glPixelStorei(pname: Int, param: Int) = proxy.glPixelStorei(pname, param)

    override fun glPolygonOffset(factor: Float, units: Float) = proxy.glPolygonOffset(factor, units)

    override fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: IByteData) =
        proxy.glReadPixels(x, y, width, height, format, type, pixels)

    override fun glScissor(x: Int, y: Int, width: Int, height: Int) =
        proxy.glScissor(x, y, width, height)

    override fun glStencilFunc(func: Int, ref: Int, mask: Int) = proxy.glStencilFunc(func, ref, mask)

    override fun glStencilMask(mask: Int) = proxy.glStencilMask(mask)

    override fun glStencilOp(fail: Int, zfail: Int, zpass: Int) = proxy.glStencilOp(fail, zfail, zpass)

    override fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: IByteData?) =
        proxy.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels)

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
    ) = proxy.glTexImage2D(target, level, internalformat, width, height, border, format, type, image)

    override fun glTexParameterf(target: Int, pname: Int, param: Float) =
        proxy.glTexParameterf(target, pname, param)

    override fun glTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, pixels: IByteData) =
        proxy.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels)

    override fun glViewport(x: Int, y: Int, width: Int, height: Int) = proxy.glViewport(x, y, width, height)

    override fun glAttachShader(program: Int, shader: Int) = proxy.glAttachShader(program, shader)

    override fun glBindAttribLocation(program: Int, index: Int, name: String) = proxy.glBindAttribLocation(program, index, name)

    override fun glBindBuffer(target: Int, buffer: Int) = proxy.glBindBuffer(target, buffer)

    override fun glBindFramebuffer(target: Int, framebuffer: Int) = proxy.glBindFramebuffer(target, framebuffer)

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) = proxy.glBindRenderbuffer(target, renderbuffer)

    override fun glBlendColor(red: Float, green: Float, blue: Float, alpha: Float) =
        proxy.glBlendColor(red, green, blue, alpha)

    override fun glBlendEquation(mode: Int) = proxy.glBlendEquation(mode)

    override fun glBlendEquationSeparate(modeRGB: Int, modeAlpha: Int) = proxy.glBlendEquationSeparate(modeRGB, modeAlpha)

    override fun glBlendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int) =
        proxy.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha)

    override fun glBufferData(target: Int, size: Int, data: IByteData?, usage: Int) =
        proxy.glBufferData(target, size, data, usage)

    override fun glBufferSubData(target: Int, offset: Int, size: Int, data: IByteData) =
        proxy.glBufferSubData(target, offset, size, data)

    override fun glCheckFramebufferStatus(target: Int): Int = proxy.glCheckFramebufferStatus(target)

    override fun glCompileShader(shader: Int) = proxy.glCompileShader(shader)

    override fun glCreateProgram(): Int = proxy.glCreateProgram()

    override fun glCreateShader(type: Int): Int = proxy.glCreateShader(type)

    override fun glDeleteBuffer(buffer: Int) = proxy.glDeleteBuffer(buffer)

    override fun glDeleteBuffers(buffers: IntArray) = proxy.glDeleteBuffers(buffers)

    override fun glDeleteFramebuffer(framebuffer: Int) = proxy.glDeleteFramebuffer(framebuffer)

    override fun glDeleteFramebuffers(framebuffers: IntArray) = proxy.glDeleteFramebuffers(framebuffers)

    override fun glDeleteProgram(program: Int) = proxy.glDeleteProgram(program)

    override fun glDeleteRenderbuffer(renderbuffer: Int) = proxy.glDeleteRenderbuffer(renderbuffer)

    override fun glDeleteRenderbuffers(renderbuffers: IntArray) = proxy.glDeleteRenderbuffers(renderbuffers)

    override fun glDeleteShader(shader: Int) = proxy.glDeleteShader(shader)

    override fun glDetachShader(program: Int, shader: Int) = proxy.glDetachShader(program, shader)

    override fun glDisableVertexAttribArray(index: Int) = proxy.glDisableVertexAttribArray(index)

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) = proxy.glDrawElements(mode, count, type, indices)

    override fun glEnableVertexAttribArray(index: Int) = proxy.glEnableVertexAttribArray(index)

    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) =
        proxy.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer)

    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) =
        proxy.glFramebufferTexture2D(target, attachment, textarget, texture, level)

    override fun glGenBuffer(): Int = proxy.glGenBuffer()

    override fun glGenBuffers(buffers: IntArray) = proxy.glGenBuffers(buffers)

    override fun glGenerateMipmap(target: Int) = proxy.glGenerateMipmap(target)

    override fun glGenFramebuffer(): Int = proxy.glGenFramebuffer()

    override fun glGenFramebuffers(framebuffers: IntArray) = proxy.glGenFramebuffers(framebuffers)

    override fun glGenRenderbuffer(): Int = proxy.glGenRenderbuffer()

    override fun glGetActiveAttrib(program: Int, index: Int, size: IntArray, type: IntArray): String =
        proxy.glGetActiveAttrib(program, index, size, type)

    override fun glGetActiveUniform(program: Int, index: Int, size: IntArray, type: IntArray): String =
        proxy.glGetActiveUniform(program, index, size, type)

    override fun glGetAttachedShaders(program: Int, maxcount: Int, count: IntArray, shaders: IntArray) =
        proxy.glGetAttachedShaders(program, maxcount, count, shaders)

    override fun glGetAttribLocation(program: Int, name: String): Int =
        proxy.glGetAttribLocation(program, name)

    override fun glGetBooleanv(pname: Int, params: IByteData) = proxy.glGetBooleanv(pname, params)

    override fun glGetBufferParameteriv(target: Int, pname: Int, params: IntArray) =
        proxy.glGetBufferParameteriv(target, pname, params)

    override fun glGetFloatv(pname: Int, params: FloatArray) = proxy.glGetFloatv(pname, params)

    override fun glGetFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int, params: IntArray) =
        proxy.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params)

    override fun glGetProgramiv(program: Int, pname: Int, params: IntArray) =
        proxy.glGetProgramiv(program, pname, params)

    override fun glGetProgramInfoLog(program: Int): String = proxy.glGetProgramInfoLog(program)

    override fun glGetRenderbufferParameteriv(target: Int, pname: Int, params: IntArray) =
        proxy.glGetRenderbufferParameteriv(target, pname, params)

    override fun glGetShaderiv(shader: Int, pname: Int, params: IntArray) = proxy.glGetShaderiv(shader, pname, params)

    override fun glGetShaderInfoLog(shader: Int): String = proxy.glGetShaderInfoLog(shader)

    override fun glGetShaderPrecisionFormat(shadertype: Int, precisiontype: Int, range: IntArray, precision: IntArray) =
        proxy.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision)

    override fun glGetTexParameterfv(target: Int, pname: Int, params: FloatArray) =
        proxy.glGetTexParameterfv(target, pname, params)

    override fun glGetTexParameteriv(target: Int, pname: Int, params: IntArray) =
        proxy.glGetTexParameteriv(target, pname, params)

    override fun glGetUniformfv(program: Int, location: Int, params: FloatArray) =
        proxy.glGetUniformfv(program, location, params)

    override fun glGetUniformiv(program: Int, location: Int, params: IntArray) =
        proxy.glGetUniformiv(program, location, params)

    override fun glGetUniformLocation(program: Int, name: String): Int = proxy.glGetUniformLocation(program, name)

    override fun glGetVertexAttribfv(index: Int, pname: Int, params: FloatArray) =
        proxy.glGetVertexAttribfv(index, pname, params)

    override fun glGetVertexAttribiv(index: Int, pname: Int, params: IntArray) =
        proxy.glGetVertexAttribiv(index, pname, params)

    override fun glGetVertexAttribPointerv(index: Int, pname: Int, pointer: IntArray) =
        proxy.glGetVertexAttribPointerv(index, pname, pointer)

    override fun glIsBuffer(buffer: Int): Boolean = proxy.glIsBuffer(buffer)

    override fun glIsEnabled(cap: Int): Boolean = proxy.glIsEnabled(cap)

    override fun glIsFramebuffer(framebuffer: Int): Boolean = proxy.glIsFramebuffer(framebuffer)

    override fun glIsProgram(program: Int): Boolean = proxy.glIsProgram(program)

    override fun glIsRenderbuffer(renderbuffer: Int): Boolean = proxy.glIsRenderbuffer(renderbuffer)

    override fun glIsShader(shader: Int): Boolean = proxy.glIsShader(shader)

    override fun glIsTexture(texture: Int): Boolean = proxy.glIsTexture(texture)

    override fun glLinkProgram(program: Int) = proxy.glLinkProgram(program)

    override fun glReleaseShaderCompiler() = proxy.glReleaseShaderCompiler()

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) =
        proxy.glRenderbufferStorage(target, internalformat, width, height)

    override fun glSampleCoverage(value: Float, invert: Boolean) = proxy.glSampleCoverage(value, invert)

    override fun glShaderBinary(n: Int, shaders: IntArray, binaryformat: Int, binary: IByteData, length: Int) =
        proxy.glShaderBinary(n, shaders, binaryformat, binary, length)

    override fun glShaderSource(shader: Int, string: String) = proxy.glShaderSource(shader, string)

    override fun glStencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int) =
        proxy.glStencilFuncSeparate(face, func, ref, mask)

    override fun glStencilMaskSeparate(face: Int, mask: Int) = proxy.glStencilMaskSeparate(face, mask)

    override fun glStencilOpSeparate(face: Int, fail: Int, zfail: Int, zpass: Int) =
        proxy.glStencilOpSeparate(face, fail, zfail, zpass)

    override fun glTexParameterfv(target: Int, pname: Int, params: FloatArray) =
        proxy.glTexParameterfv(target, pname, params)

    override fun glTexParameteri(target: Int, pname: Int, param: Int) =
        proxy.glTexParameteri(target, pname, param)

    override fun glTexParameteriv(target: Int, pname: Int, params: IntArray) =
        proxy.glTexParameteriv(target, pname, params)

    override fun glUniform1f(location: Int, x: Float) = proxy.glUniform1f(location, x)

    override fun glUniform1fv(location: Int, count: Int, v: FloatArray) = proxy.glUniform1fv(location, count, v)

    override fun glUniform1fv(location: Int, count: Int, v: FloatArray, offset: Int) =
        proxy.glUniform1fv(location, count, v, offset)

    override fun glUniform1i(location: Int, x: Int) = proxy.glUniform1i(location, x)

    override fun glUniform1iv(location: Int, count: Int, v: IntArray) =
        proxy.glUniform1iv(location, count, v)

    override fun glUniform1iv(location: Int, count: Int, v: IntArray, offset: Int) =
        proxy.glUniform1iv(location, count, v, offset)

    override fun glUniform2f(location: Int, x: Float, y: Float) = proxy.glUniform2f(location, x, y)

    override fun glUniform2fv(location: Int, count: Int, v: FloatArray) = proxy.glUniform2fv(location, count, v)

    override fun glUniform2fv(location: Int, count: Int, v: FloatArray, offset: Int) =
        proxy.glUniform2fv(location, count, v, offset)

    override fun glUniform2i(location: Int, x: Int, y: Int) = proxy.glUniform2i(location, x, y)

    override fun glUniform2iv(location: Int, count: Int, v: IntArray) = proxy.glUniform2iv(location, count, v)

    override fun glUniform2iv(location: Int, count: Int, v: IntArray, offset: Int) =
        proxy.glUniform2iv(location, count, v, offset)

    override fun glUniform3f(location: Int, x: Float, y: Float, z: Float) = proxy.glUniform3f(location, x, y, z)

    override fun glUniform3fv(location: Int, count: Int, v: FloatArray) = proxy.glUniform3fv(location, count, v)

    override fun glUniform3fv(location: Int, count: Int, v: FloatArray, offset: Int) =
        proxy.glUniform3fv(location, count, v, offset)

    override fun glUniform3i(location: Int, x: Int, y: Int, z: Int) = proxy.glUniform3i(location, x, y, z)

    override fun glUniform3iv(location: Int, count: Int, v: IntArray) = proxy.glUniform3iv(location, count, v)

    override fun glUniform3iv(location: Int, count: Int, v: IntArray, offset: Int) =
        proxy.glUniform3iv(location, count, v, offset)

    override fun glUniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) =
        proxy.glUniform4f(location, x, y, z, w)

    override fun glUniform4fv(location: Int, count: Int, v: FloatArray) = proxy.glUniform4fv(location, count, v)

    override fun glUniform4fv(location: Int, count: Int, v: FloatArray, offset: Int) =
        proxy.glUniform4fv(location, count, v, offset)

    override fun glUniform4i(location: Int, x: Int, y: Int, z: Int, w: Int) =
        proxy.glUniform4i(location, x, y, z, w)

    override fun glUniform4iv(location: Int, count: Int, v: IntArray) =
        proxy.glUniform4iv(location, count, v)

    override fun glUniform4iv(location: Int, count: Int, v: IntArray, offset: Int) =
        proxy.glUniform4iv(location, count, v, offset)

    override fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
        proxy.glUniformMatrix2fv(location, count, transpose, value)

    override fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) =
        proxy.glUniformMatrix2fv(location, count, transpose, value, offset)

    override fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
        proxy.glUniformMatrix3fv(location, count, transpose, value)

    override fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) =
        proxy.glUniformMatrix3fv(location, count, transpose, value, offset)

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
        proxy.glUniformMatrix4fv(location, count, transpose, value)

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) =
        proxy.glUniformMatrix4fv(location, count, transpose, value, offset)

    /** GL will check if [program] is not equal to [program] and then do OpenGL call.
     * Also you can use [program] to set program */
    override fun glUseProgram(program: Int) = proxy.glUseProgram(program)

    override fun glValidateProgram(program: Int) = proxy.glValidateProgram(program)

    override fun glVertexAttrib1f(indx: Int, x: Float) = proxy.glVertexAttrib1f(indx, x)

    override fun glVertexAttrib1fv(indx: Int, values: FloatArray) = proxy.glVertexAttrib1fv(indx, values)

    override fun glVertexAttrib2f(indx: Int, x: Float, y: Float) = proxy.glVertexAttrib2f(indx, x, y)

    override fun glVertexAttrib2fv(indx: Int, values: FloatArray) = proxy.glVertexAttrib2fv(indx, values)

    override fun glVertexAttrib3f(indx: Int, x: Float, y: Float, z: Float) = proxy.glVertexAttrib3f(indx, x, y, z)

    override fun glVertexAttrib3fv(indx: Int, values: FloatArray) = proxy.glVertexAttrib3fv(indx, values)

    override fun glVertexAttrib4f(indx: Int, x: Float, y: Float, z: Float, w: Float) =
        proxy.glVertexAttrib4f(indx, x, y, z, w)

    override fun glVertexAttrib4fv(indx: Int, values: FloatArray) = proxy.glVertexAttrib4fv(indx, values)

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: IByteData) =
        proxy.glVertexAttribPointer(indx, size, type, normalized, stride, ptr)

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: Int) =
        proxy.glVertexAttribPointer(indx, size, type, normalized, stride, ptr)
}