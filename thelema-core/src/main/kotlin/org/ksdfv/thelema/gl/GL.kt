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

import org.ksdfv.thelema.img.IImage
import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.data.IFloatData
import org.ksdfv.thelema.data.IIntData
import org.ksdfv.thelema.ext.traverseSafe

/** @author zeganstyl */
object GL: IGL {
    var api: IGL = object: IGL {}

    /** Default frame buffer width */
    var mainFrameBufferWidth: Int = 0

    /** Default frame buffer height */
    var mainFrameBufferHeight: Int = 0

    /** See [call] */
    val singleCallRequests = ArrayList<Request>()

    /** See [render] */
    val renderCallRequests = ArrayList<Request>()

    /** See [call] */
    val destroyCallRequests = ArrayList<Request>()

    /** Will be Called when screen resize is performed. No removing after execution. */
    val resizeCallRequests = ArrayList<Request>()

    private var enableGlCall = false

    /** Maximum supported anisotropic filtering level supported by the device. */
    var maxAnisotropicFilterLevel = 1f
        private set

    /** Current shader program. See [glUseProgram] */
    var currentProgram: Int = 0
        set(value) {
            if (field != value) {
                field = value
                api.glUseProgram(value)
            }
        }

    /** Current texture unit. Starts from 0 */
    var activeTextureUnit: Int = 0
        set(value) {
            if (field != value) {
                field = value
                api.glActiveTexture(GL_TEXTURE0 + value)
            }
        }

    private val textureUnitsInternal = ArrayList<Int>(16).apply { for (i in 0 until 16) { add(0) } }

    /** Contains texture handles of all texture image units.
     * Initial size is 16, you can update and check max texture units number of your video card with [initTextureUnits] */
    val textureUnits: List<Int>
        get() = textureUnitsInternal

    var isCullFaceEnabled: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value) api.glEnable(GL_CULL_FACE) else api.glDisable(GL_CULL_FACE)
            }
        }

    /** [OpenGL API documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glCullFace.xml) */
    var cullFaceMode: Int = GL_BACK
        set(value) {
            if (field != value) {
                field = value
                api.glCullFace(value)
            }
        }

    /** Use [blendFactorS] and [blendFactorD] to set parameters. */
    var isBlendEnabled: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value) api.glEnable(GL_BLEND) else api.glDisable(GL_BLEND)
            }
        }

    /** Use [isBlendEnabled] to enable this parameter.
     * [OpenGL API documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glBlendFunc.xml) */
    var blendFactorS: Int = GL_ONE
        set(value) {
            if (field != value) {
                field = value
                if (enableGlCall) api.glBlendFunc(value, blendFactorD)
            }
        }

    /** Use [isBlendEnabled] to enable this parameter.
     * [OpenGL API documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glBlendFunc.xml) */
    var blendFactorD: Int = GL_ZERO
        set(value) {
            if (field != value) {
                field = value
                if (enableGlCall) api.glBlendFunc(blendFactorS, value)
            }
        }

    /** Use [depthFunc] to set parameters. */
    var isDepthTestEnabled: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value) api.glEnable(GL_DEPTH_TEST) else api.glDisable(GL_DEPTH_TEST)
            }
        }

    /** Use [isDepthTestEnabled] to enable this parameter.
     * [OpenGL API documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glDepthFunc.xml) */
    var depthFunc: Int = GL_LESS
        set(value) {
            if (field != value) {
                field = value
                api.glDepthFunc(value)
            }
        }

    /** [OpenGL API documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glDepthMask.xml) */
    var isDepthMaskEnabled: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                api.glDepthMask(value)
            }
        }

    private var samplerCounter: Int = 0

    /** Get free texture unit. If all texture units are grabbed, it will give units from 0 again */
    fun grabTextureUnit(): Int {
        val unit = samplerCounter
        samplerCounter++
        if (samplerCounter >= textureUnits.size) samplerCounter = 0
        return unit
    }

    /** This function will check maximum number of texture units and update [textureUnits] */
    fun initTextureUnits() {
        val buffer = IntArray(1)
        glGetIntegerv(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, buffer)
        val units = buffer[0]

        if (textureUnitsInternal.size < units) {
            val num = units - textureUnitsInternal.size
            for (i in 0 until num) {
                textureUnitsInternal.add(0)
            }
        } else if (textureUnitsInternal.size > units) {
            val num = textureUnitsInternal.size - units
            for (i in 0 until num) {
                textureUnitsInternal.removeAt(textureUnitsInternal.lastIndex)
            }
        }
    }

    fun initGL() {
        initTextureUnits()

        if (isExtensionSupported("GL_EXT_texture_filter_anisotropic")) {
            val buffer = FloatArray(16)
            glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, buffer)
            maxAnisotropicFilterLevel = buffer[0]
        }
    }

    /** Simple blending function in most cases.
     * sfactor = GL_SRC_ALPHA; dfactor = GL_ONE_MINUS_SRC_ALPHA */
    fun setSimpleAlphaBlending() {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    /** Will be called once and removed */
    fun call(name: String? = null, call: () -> Unit) {
        singleCallRequests.add(Request(name, call))
    }

    /** Will be called every thread loop */
    fun render(name: String? = null, call: () -> Unit) {
        renderCallRequests.add(Request(name, call))
    }

    /** Will be called every thread loop */
    fun destroy(name: String? = null, call: () -> Unit) {
        destroyCallRequests.add(Request(name, call))
    }

    /** Must be called on OpenGL thread by platform backend */
    fun doSingleCalls() {
        singleCallRequests.traverseSafe {
            it.call()
            if (it.name != null) println("\"${it.name}\" called on GL thread")
        }
        singleCallRequests.clear()
    }

    /** Must be called on OpenGL thread by platform backend */
    fun doRenderCalls() {
        renderCallRequests.traverseSafe { it.call() }
    }

    /** Must be called on OpenGL thread by platform backend */
    fun doDestroyCalls() {
        destroyCallRequests.traverseSafe {
            it.call()
            if (it.name != null) println("\"${it.name}\" called on GL thread")
        }
        destroyCallRequests.clear()
    }

    override fun isExtensionSupported(extension: String) = api.isExtensionSupported(extension)

    override fun glReadBuffer(mode: Int) = api.glReadBuffer(mode)

    override fun glDrawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, indices: IByteData) =
            api.glDrawRangeElements(mode, start, end, count, type, indices)

    override fun glDrawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, offset: Int) =
            api.glDrawRangeElements(mode, start, end, count, type, offset)

    override fun glTexImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, border: Int, format: Int, type: Int, offset: Int) =
            api.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, offset)

    override fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, pixels: IByteData) =
            api.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels)

    override fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, offset: Int) =
            api.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset)

    override fun glCopyTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, x: Int, y: Int, width: Int, height: Int) =
            api.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height)

    override fun glGenQueries(ids: IntArray) = api.glGenQueries(ids)

    override fun glGenQueries(): Int = api.glGenQueries()

    override fun glDeleteQueries(ids: IntArray) = api.glDeleteQueries(ids)

    override fun glDeleteQueries(id: Int) = api.glDeleteQueries(id)

    override fun glIsQuery(id: Int): Boolean = api.glIsQuery(id)

    override fun glBeginQuery(target: Int, id: Int) = api.glBeginQuery(target, id)

    override fun glEndQuery(target: Int) = api.glEndQuery(target)

    override fun glGetQueryiv(target: Int, pname: Int, params: IntArray) = api.glGetQueryiv(target, pname, params)

    override fun glGetQueryObjectuiv(id: Int, pname: Int, params: IntArray) = api.glGetQueryObjectuiv(id, pname, params)

    override fun glUnmapBuffer(target: Int): Boolean = api.glUnmapBuffer(target)

    override fun glGetBufferPointerv(target: Int, pname: Int): IntArray = api.glGetBufferPointerv(target, pname)

    override fun glDrawBuffers(n: Int, bufs: IntArray) = api.glDrawBuffers(n, bufs)

    override fun glUniformMatrix2x3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
            api.glUniformMatrix2x3fv(location, count, transpose, value)

    override fun glUniformMatrix3x2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
            api.glUniformMatrix3x2fv(location, count, transpose, value)

    override fun glUniformMatrix2x4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
            api.glUniformMatrix2x4fv(location, count, transpose, value)

    override fun glUniformMatrix4x2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
            api.glUniformMatrix4x2fv(location, count, transpose, value)

    override fun glUniformMatrix3x4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
            api.glUniformMatrix3x4fv(location, count, transpose, value)

    override fun glUniformMatrix4x3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
            api.glUniformMatrix4x3fv(location, count, transpose, value)

    override fun glBlitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: Int, filter: Int) =
            api.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter)

    override fun glRenderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) =
            api.glRenderbufferStorageMultisample(target, samples, internalformat, width, height)

    override fun glFramebufferTextureLayer(target: Int, attachment: Int, texture: Int, level: Int, layer: Int) =
            api.glFramebufferTextureLayer(target, attachment, texture, level, layer)

    override fun glFlushMappedBufferRange(target: Int, offset: Int, length: Int) = api.glFlushMappedBufferRange(target, offset, length)

    override fun glBindVertexArray(array: Int) = api.glBindVertexArray(array)

    override fun glDeleteVertexArrays(id: Int) = api.glDeleteVertexArrays(id)

    override fun glDeleteVertexArrays(arrays: IntArray) = api.glDeleteVertexArrays(arrays)

    override fun glGenVertexArrays() = api.glGenVertexArrays()

    override fun glGenVertexArrays(arrays: IntArray) = api.glGenVertexArrays(arrays)

    override fun glIsVertexArray(array: Int): Boolean = api.glIsVertexArray(array)

    override fun glBeginTransformFeedback(primitiveMode: Int) = api.glBeginTransformFeedback(primitiveMode)

    override fun glEndTransformFeedback() = api.glEndTransformFeedback()

    override fun glBindBufferRange(target: Int, index: Int, buffer: Int, offset: Int, size: Int) =
            api.glBindBufferRange(target, index, buffer, offset, size)

    override fun glBindBufferBase(target: Int, index: Int, buffer: Int) =
            api.glBindBufferBase(target, index, buffer)

    override fun glTransformFeedbackVaryings(program: Int, varyings: Array<String>, bufferMode: Int) =
            api.glTransformFeedbackVaryings(program, varyings, bufferMode)

    override fun glVertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) =
            api.glVertexAttribIPointer(index, size, type, stride, offset)

    override fun glGetVertexAttribIiv(index: Int, pname: Int, params: IntArray) =
            api.glGetVertexAttribIiv(index, pname, params)

    override fun glGetVertexAttribIuiv(index: Int, pname: Int, params: IntArray) =
            api.glGetVertexAttribIuiv(index, pname, params)

    override fun glVertexAttribI4i(index: Int, x: Int, y: Int, z: Int, w: Int) =
            api.glVertexAttribI4i(index, x, y, z, w)

    override fun glVertexAttribI4ui(index: Int, x: Int, y: Int, z: Int, w: Int) =
            api.glVertexAttribI4ui(index, x, y, z, w)

    override fun glGetUniformuiv(program: Int, location: Int, params: IntArray) =
            api.glGetUniformuiv(program, location, params)

    override fun glGetFragDataLocation(program: Int, name: String): Int =
            api.glGetFragDataLocation(program, name)

    override fun glUniform1uiv(location: Int, count: Int, value: IIntData) =
            api.glUniform1uiv(location, count, value)

    override fun glUniform3uiv(location: Int, count: Int, value: IIntData) =
            api.glUniform3uiv(location, count, value)

    override fun glUniform4uiv(location: Int, count: Int, value: IIntData) =
            api.glUniform4uiv(location, count, value)

    override fun glClearBufferiv(buffer: Int, drawbuffer: Int, value: IIntData) =
            api.glClearBufferiv(buffer, drawbuffer, value)

    override fun glClearBufferuiv(buffer: Int, drawbuffer: Int, value: IIntData) =
            api.glClearBufferuiv(buffer, drawbuffer, value)

    override fun glClearBufferfv(buffer: Int, drawbuffer: Int, value: IFloatData) =
            api.glClearBufferfv(buffer, drawbuffer, value)

    override fun glClearBufferfi(buffer: Int, drawbuffer: Int, depth: Float, stencil: Int) =
            api.glClearBufferfi(buffer, drawbuffer, depth, stencil)

    override fun glGetStringi(name: Int, index: Int): String =
            api.glGetStringi(name, index)

    override fun glCopyBufferSubData(readTarget: Int, writeTarget: Int, readOffset: Int, writeOffset: Int, size: Int) =
            api.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size)

    override fun glGetUniformIndices(program: Int, uniformNames: Array<String>, uniformIndices: IIntData) =
            api.glGetUniformIndices(program, uniformNames, uniformIndices)

    override fun glGetActiveUniformsiv(program: Int, uniformCount: Int, uniformIndices: IntArray, pname: Int, params: IntArray) =
            api.glGetActiveUniformsiv(program, uniformCount, uniformIndices, pname, params)

    override fun glGetUniformBlockIndex(program: Int, uniformBlockName: String): Int =
            api.glGetUniformBlockIndex(program, uniformBlockName)

    override fun glGetActiveUniformBlockiv(program: Int, uniformBlockIndex: Int, pname: Int): Int =
        api.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname)

    override fun glGetActiveUniformBlockName(program: Int, uniformBlockIndex: Int, length: IntArray, uniformBlockName: IByteData) =
            api.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName)

    override fun glGetActiveUniformBlockName(program: Int, uniformBlockIndex: Int): String =
            api.glGetActiveUniformBlockName(program, uniformBlockIndex)

    override fun glUniformBlockBinding(program: Int, uniformBlockIndex: Int, uniformBlockBinding: Int) =
            api.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding)

    override fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) =
            api.glDrawArraysInstanced(mode, first, count, instanceCount)

    override fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) =
            api.glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount)

    override fun glGetInteger64v(pname: Int, params: LongArray) =
            api.glGetInteger64v(pname, params)

    override fun glGetBufferParameteri64v(target: Int, pname: Int, params: LongArray) =
            api.glGetBufferParameteri64v(target, pname, params)

    override fun glGenSamplers() = api.glGenSamplers()

    override fun glGenSamplers(samplers: IntArray) = api.glGenSamplers(samplers)

    override fun glDeleteSamplers(sampler: Int) = api.glDeleteSamplers(sampler)

    override fun glDeleteSamplers(samplers: IntArray) = api.glDeleteSamplers(samplers)

    override fun glIsSampler(sampler: Int): Boolean = api.glIsSampler(sampler)

    override fun glBindSampler(unit: Int, sampler: Int) = api.glBindSampler(unit, sampler)

    override fun glSamplerParameteri(sampler: Int, pname: Int, param: Int) =
            api.glSamplerParameteri(sampler, pname, param)

    override fun glSamplerParameteriv(sampler: Int, pname: Int, param: IntArray) =
            api.glSamplerParameteriv(sampler, pname, param)

    override fun glSamplerParameterf(sampler: Int, pname: Int, param: Float) =
            api.glSamplerParameterf(sampler, pname, param)

    override fun glSamplerParameterfv(sampler: Int, pname: Int, param: FloatArray) =
            api.glSamplerParameterfv(sampler, pname, param)

    override fun glGetSamplerParameteriv(sampler: Int, pname: Int, params: IntArray) =
            api.glGetSamplerParameteriv(sampler, pname, params)

    override fun glGetSamplerParameterfv(sampler: Int, pname: Int, params: FloatArray) =
            api.glGetSamplerParameterfv(sampler, pname, params)

    override fun glVertexAttribDivisor(index: Int, divisor: Int) = api.glVertexAttribDivisor(index, divisor)

    override fun glBindTransformFeedback(target: Int, id: Int) = api.glBindTransformFeedback(target, id)

    override fun glDeleteTransformFeedbacks(id: Int) = api.glDeleteTransformFeedbacks(id)

    override fun glDeleteTransformFeedbacks(ids: IntArray) = api.glDeleteTransformFeedbacks(ids)

    override fun glGenTransformFeedbacks() = api.glGenTransformFeedbacks()

    override fun glGenTransformFeedbacks(ids: IntArray) = api.glGenTransformFeedbacks(ids)

    override fun glIsTransformFeedback(id: Int): Boolean = api.glIsTransformFeedback(id)

    override fun glPauseTransformFeedback() = api.glPauseTransformFeedback()

    override fun glResumeTransformFeedback() = api.glResumeTransformFeedback()

    override fun glProgramParameteri(program: Int, pname: Int, value: Int) =
            api.glProgramParameteri(program, pname, value)

    override fun glInvalidateFramebuffer(target: Int, numAttachments: Int, attachments: IntArray) =
            api.glInvalidateFramebuffer(target, numAttachments, attachments)

    override fun glInvalidateSubFramebuffer(target: Int, numAttachments: Int, attachments: IntArray, x: Int, y: Int, width: Int, height: Int) =
            api.glInvalidateSubFramebuffer(target, numAttachments, attachments, x, y, width, height)

    override fun glActiveTexture(texture: Int) {
        activeTextureUnit = texture - GL_TEXTURE0
    }

    override fun glBindTexture(target: Int, texture: Int) {
        if (textureUnitsInternal[activeTextureUnit] != texture) {
            textureUnitsInternal[activeTextureUnit] = texture
            api.glBindTexture(target, texture)
        }
    }

    override fun glBlendFunc(sfactor: Int, dfactor: Int) {
        enableGlCall = false
        api.glBlendFunc(sfactor, dfactor)
        blendFactorS = sfactor
        blendFactorD = sfactor
        enableGlCall = true
    }

    override fun glClear(mask: Int) = api.glClear(mask)

    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) =
            api.glClearColor(red, green, blue, alpha)

    override fun glClearDepthf(depth: Float) = api.glClearDepthf(depth)

    override fun glClearStencil(s: Int) = api.glClearStencil(s)

    override fun glColorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) =
            api.glColorMask(red, green, blue, alpha)

    override fun glCompressedTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, imageSize: Int, data: IByteData) =
            api.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data)

    override fun glCompressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, imageSize: Int, data: IByteData) =
            api.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data)

    override fun glCopyTexImage2D(target: Int, level: Int, internalformat: Int, x: Int, y: Int, width: Int, height: Int, border: Int) =
            api.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border)

    override fun glCopyTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, x: Int, y: Int, width: Int, height: Int) =
            api.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height)

    override fun glCullFace(mode: Int) {
        cullFaceMode = mode
    }

    override fun glDeleteTextures(textures: IntArray) = api.glDeleteTextures(textures)

    override fun glDeleteTexture(texture: Int) = api.glDeleteTexture(texture)

    override fun glDepthFunc(func: Int) {
        depthFunc = func
    }

    override fun glDepthMask(flag: Boolean) {
        isDepthMaskEnabled = flag
    }

    override fun glDepthRangef(zNear: Float, zFar: Float) = api.glDepthRangef(zNear, zFar)

    override fun glDisable(cap: Int) {
        when (cap) {
            GL_BLEND -> isBlendEnabled = false
            GL_CULL_FACE -> isCullFaceEnabled = false
            else -> api.glDisable(cap)
        }
    }

    override fun glDrawArrays(mode: Int, first: Int, count: Int) = api.glDrawArrays(mode, first, count)

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: IByteData) = api.glDrawElements(mode, count, type, indices)

    override fun glEnable(cap: Int) {
        when (cap) {
            GL_BLEND -> isBlendEnabled = true
            GL_CULL_FACE -> isCullFaceEnabled = true
            else -> api.glEnable(cap)
        }
    }

    override fun glFinish() = api.glFinish()

    override fun glFlush() = api.glFlush()

    override fun glFrontFace(mode: Int) = api.glFrontFace(mode)

    override fun glGenTextures(n: Int, textures: IntArray) = api.glGenTextures(n, textures)

    override fun glGenTexture(): Int = api.glGenTexture()

    override fun glGetError(): Int = api.glGetError()

    override fun glGetIntegerv(pname: Int, params: IntArray) = api.glGetIntegerv(pname, params)

    override fun glGetString(name: Int): String? = api.glGetString(name)

    override fun glHint(target: Int, mode: Int) = api.glHint(target, mode)

    override fun glLineWidth(width: Float) = api.glLineWidth(width)

    override fun glPixelStorei(pname: Int, param: Int) = api.glPixelStorei(pname, param)

    override fun glPolygonOffset(factor: Float, units: Float) = api.glPolygonOffset(factor, units)

    override fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: IByteData) =
            api.glReadPixels(x, y, width, height, format, type, pixels)

    override fun glScissor(x: Int, y: Int, width: Int, height: Int) =
            api.glScissor(x, y, width, height)

    override fun glStencilFunc(func: Int, ref: Int, mask: Int) = api.glStencilFunc(func, ref, mask)

    override fun glStencilMask(mask: Int) = api.glStencilMask(mask)

    override fun glStencilOp(fail: Int, zfail: Int, zpass: Int) = api.glStencilOp(fail, zfail, zpass)

    override fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: IByteData?) =
            api.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels)

    override fun glTexImage2D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        border: Int,
        format: Int,
        type: Int,
        image: IImage?
    ) {
        api.glTexImage2D(target, level, internalformat, width, height, border, format, type, image)
    }

    override fun glTexParameterf(target: Int, pname: Int, param: Float) =
            api.glTexParameterf(target, pname, param)

    override fun glTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, pixels: IByteData) =
            api.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels)

    override fun glViewport(x: Int, y: Int, width: Int, height: Int) = api.glViewport(x, y, width, height)

    override fun glAttachShader(program: Int, shader: Int) = api.glAttachShader(program, shader)

    override fun glBindAttribLocation(program: Int, index: Int, name: String) = api.glBindAttribLocation(program, index, name)

    override fun glBindBuffer(target: Int, buffer: Int) = api.glBindBuffer(target, buffer)

    override fun glBindFramebuffer(target: Int, framebuffer: Int) = api.glBindFramebuffer(target, framebuffer)

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) = api.glBindRenderbuffer(target, renderbuffer)

    override fun glBlendColor(red: Float, green: Float, blue: Float, alpha: Float) =
            api.glBlendColor(red, green, blue, alpha)

    override fun glBlendEquation(mode: Int) = api.glBlendEquation(mode)

    override fun glBlendEquationSeparate(modeRGB: Int, modeAlpha: Int) = api.glBlendEquationSeparate(modeRGB, modeAlpha)

    override fun glBlendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int) =
            api.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha)

    override fun glBufferData(target: Int, size: Int, data: IByteData?, usage: Int) =
            api.glBufferData(target, size, data, usage)

    override fun glBufferSubData(target: Int, offset: Int, size: Int, data: IByteData) =
            api.glBufferSubData(target, offset, size, data)

    override fun glCheckFramebufferStatus(target: Int): Int = api.glCheckFramebufferStatus(target)

    override fun glCompileShader(shader: Int) = api.glCompileShader(shader)

    override fun glCreateProgram(): Int = api.glCreateProgram()

    override fun glCreateShader(type: Int): Int = api.glCreateShader(type)

    override fun glDeleteBuffer(buffer: Int) = api.glDeleteBuffer(buffer)

    override fun glDeleteBuffers(buffers: IntArray) = api.glDeleteBuffers(buffers)

    override fun glDeleteFramebuffer(framebuffer: Int) = api.glDeleteFramebuffer(framebuffer)

    override fun glDeleteFramebuffers(framebuffers: IntArray) = api.glDeleteFramebuffers(framebuffers)

    override fun glDeleteProgram(program: Int) = api.glDeleteProgram(program)

    override fun glDeleteRenderbuffer(renderbuffer: Int) = api.glDeleteRenderbuffer(renderbuffer)

    override fun glDeleteRenderbuffers(renderbuffers: IntArray) = api.glDeleteRenderbuffers(renderbuffers)

    override fun glDeleteShader(shader: Int) = api.glDeleteShader(shader)

    override fun glDetachShader(program: Int, shader: Int) = api.glDetachShader(program, shader)

    override fun glDisableVertexAttribArray(index: Int) = api.glDisableVertexAttribArray(index)

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) = api.glDrawElements(mode, count, type, indices)

    override fun glEnableVertexAttribArray(index: Int) = api.glEnableVertexAttribArray(index)

    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) =
            api.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer)

    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) =
            api.glFramebufferTexture2D(target, attachment, textarget, texture, level)

    override fun glGenBuffer(): Int = api.glGenBuffer()

    override fun glGenBuffers(buffers: IntArray) = api.glGenBuffers(buffers)

    override fun glGenerateMipmap(target: Int) = api.glGenerateMipmap(target)

    override fun glGenFramebuffer(): Int = api.glGenFramebuffer()

    override fun glGenFramebuffers(framebuffers: IntArray) = api.glGenFramebuffers(framebuffers)

    override fun glGenRenderbuffer(): Int = api.glGenRenderbuffer()

    override fun glGetActiveAttrib(program: Int, index: Int, size: IntArray, type: IntArray): String =
            api.glGetActiveAttrib(program, index, size, type)

    override fun glGetActiveUniform(program: Int, index: Int, size: IntArray, type: IntArray): String =
            api.glGetActiveUniform(program, index, size, type)

    override fun glGetAttachedShaders(program: Int, maxcount: Int, count: IntArray, shaders: IntArray) =
            api.glGetAttachedShaders(program, maxcount, count, shaders)

    override fun glGetAttribLocation(program: Int, name: String): Int =
            api.glGetAttribLocation(program, name)

    override fun glGetBooleanv(pname: Int, params: IByteData) = api.glGetBooleanv(pname, params)

    override fun glGetBufferParameteriv(target: Int, pname: Int, params: IntArray) =
            api.glGetBufferParameteriv(target, pname, params)

    override fun glGetFloatv(pname: Int, params: FloatArray) = api.glGetFloatv(pname, params)

    override fun glGetFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int, params: IntArray) =
            api.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params)

    override fun glGetProgramiv(program: Int, pname: Int, params: IntArray) =
            api.glGetProgramiv(program, pname, params)

    override fun glGetProgramInfoLog(program: Int): String = api.glGetProgramInfoLog(program)

    override fun glGetRenderbufferParameteriv(target: Int, pname: Int, params: IntArray) =
            api.glGetRenderbufferParameteriv(target, pname, params)

    override fun glGetShaderiv(shader: Int, pname: Int, params: IntArray) = api.glGetShaderiv(shader, pname, params)

    override fun glGetShaderInfoLog(shader: Int): String = api.glGetShaderInfoLog(shader)

    override fun glGetShaderPrecisionFormat(shadertype: Int, precisiontype: Int, range: IntArray, precision: IntArray) =
            api.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision)

    override fun glGetTexParameterfv(target: Int, pname: Int, params: FloatArray) =
            api.glGetTexParameterfv(target, pname, params)

    override fun glGetTexParameteriv(target: Int, pname: Int, params: IntArray) =
            api.glGetTexParameteriv(target, pname, params)

    override fun glGetUniformfv(program: Int, location: Int, params: FloatArray) =
            api.glGetUniformfv(program, location, params)

    override fun glGetUniformiv(program: Int, location: Int, params: IntArray) =
            api.glGetUniformiv(program, location, params)

    override fun glGetUniformLocation(program: Int, name: String): Int = api.glGetUniformLocation(program, name)

    override fun glGetVertexAttribfv(index: Int, pname: Int, params: FloatArray) =
            api.glGetVertexAttribfv(index, pname, params)

    override fun glGetVertexAttribiv(index: Int, pname: Int, params: IntArray) =
            api.glGetVertexAttribiv(index, pname, params)

    override fun glGetVertexAttribPointerv(index: Int, pname: Int, pointer: IntArray) =
            api.glGetVertexAttribPointerv(index, pname, pointer)

    override fun glIsBuffer(buffer: Int): Boolean = api.glIsBuffer(buffer)

    override fun glIsEnabled(cap: Int): Boolean = api.glIsEnabled(cap)

    override fun glIsFramebuffer(framebuffer: Int): Boolean = api.glIsFramebuffer(framebuffer)

    override fun glIsProgram(program: Int): Boolean = api.glIsProgram(program)

    override fun glIsRenderbuffer(renderbuffer: Int): Boolean = api.glIsRenderbuffer(renderbuffer)

    override fun glIsShader(shader: Int): Boolean = api.glIsShader(shader)

    override fun glIsTexture(texture: Int): Boolean = api.glIsTexture(texture)

    override fun glLinkProgram(program: Int) = api.glLinkProgram(program)

    override fun glReleaseShaderCompiler() = api.glReleaseShaderCompiler()

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) =
            api.glRenderbufferStorage(target, internalformat, width, height)

    override fun glSampleCoverage(value: Float, invert: Boolean) = api.glSampleCoverage(value, invert)

    override fun glShaderBinary(n: Int, shaders: IntArray, binaryformat: Int, binary: IByteData, length: Int) =
            api.glShaderBinary(n, shaders, binaryformat, binary, length)

    override fun glShaderSource(shader: Int, string: String) = api.glShaderSource(shader, string)

    override fun glStencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int) =
            api.glStencilFuncSeparate(face, func, ref, mask)

    override fun glStencilMaskSeparate(face: Int, mask: Int) = api.glStencilMaskSeparate(face, mask)

    override fun glStencilOpSeparate(face: Int, fail: Int, zfail: Int, zpass: Int) =
            api.glStencilOpSeparate(face, fail, zfail, zpass)

    override fun glTexParameterfv(target: Int, pname: Int, params: FloatArray) =
            api.glTexParameterfv(target, pname, params)

    override fun glTexParameteri(target: Int, pname: Int, param: Int) =
            api.glTexParameteri(target, pname, param)

    override fun glTexParameteriv(target: Int, pname: Int, params: IntArray) =
            api.glTexParameteriv(target, pname, params)

    override fun glUniform1f(location: Int, x: Float) = api.glUniform1f(location, x)

    override fun glUniform1fv(location: Int, count: Int, v: FloatArray) = api.glUniform1fv(location, count, v)

    override fun glUniform1fv(location: Int, count: Int, v: FloatArray, offset: Int) =
            api.glUniform1fv(location, count, v, offset)

    override fun glUniform1i(location: Int, x: Int) = api.glUniform1i(location, x)

    override fun glUniform1iv(location: Int, count: Int, v: IntArray) =
            api.glUniform1iv(location, count, v)

    override fun glUniform1iv(location: Int, count: Int, v: IntArray, offset: Int) =
            api.glUniform1iv(location, count, v, offset)

    override fun glUniform2f(location: Int, x: Float, y: Float) = api.glUniform2f(location, x, y)

    override fun glUniform2fv(location: Int, count: Int, v: FloatArray) = api.glUniform2fv(location, count, v)

    override fun glUniform2fv(location: Int, count: Int, v: FloatArray, offset: Int) =
            api.glUniform2fv(location, count, v, offset)

    override fun glUniform2i(location: Int, x: Int, y: Int) = api.glUniform2i(location, x, y)

    override fun glUniform2iv(location: Int, count: Int, v: IntArray) = api.glUniform2iv(location, count, v)

    override fun glUniform2iv(location: Int, count: Int, v: IntArray, offset: Int) =
            api.glUniform2iv(location, count, v, offset)

    override fun glUniform3f(location: Int, x: Float, y: Float, z: Float) = api.glUniform3f(location, x, y, z)

    override fun glUniform3fv(location: Int, count: Int, v: FloatArray) = api.glUniform3fv(location, count, v)

    override fun glUniform3fv(location: Int, count: Int, v: FloatArray, offset: Int) =
            api.glUniform3fv(location, count, v, offset)

    override fun glUniform3i(location: Int, x: Int, y: Int, z: Int) = api.glUniform3i(location, x, y, z)

    override fun glUniform3iv(location: Int, count: Int, v: IntArray) = api.glUniform3iv(location, count, v)

    override fun glUniform3iv(location: Int, count: Int, v: IntArray, offset: Int) =
            api.glUniform3iv(location, count, v, offset)

    override fun glUniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) =
            api.glUniform4f(location, x, y, z, w)

    override fun glUniform4fv(location: Int, count: Int, v: FloatArray) = api.glUniform4fv(location, count, v)

    override fun glUniform4fv(location: Int, count: Int, v: FloatArray, offset: Int) =
            api.glUniform4fv(location, count, v, offset)

    override fun glUniform4i(location: Int, x: Int, y: Int, z: Int, w: Int) =
            api.glUniform4i(location, x, y, z, w)

    override fun glUniform4iv(location: Int, count: Int, v: IntArray) =
            api.glUniform4iv(location, count, v)

    override fun glUniform4iv(location: Int, count: Int, v: IntArray, offset: Int) =
            api.glUniform4iv(location, count, v, offset)

    override fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
            api.glUniformMatrix2fv(location, count, transpose, value)

    override fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) =
            api.glUniformMatrix2fv(location, count, transpose, value, offset)

    override fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
            api.glUniformMatrix3fv(location, count, transpose, value)

    override fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) =
            api.glUniformMatrix3fv(location, count, transpose, value, offset)

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) =
            api.glUniformMatrix4fv(location, count, transpose, value)

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) =
            api.glUniformMatrix4fv(location, count, transpose, value, offset)

    /** GL will check if [currentProgram] is not equal to [program] and then do OpenGL call.
     * Also you can use [currentProgram] to set program */
    override fun glUseProgram(program: Int) {
        currentProgram = program
    }

    override fun glValidateProgram(program: Int) = api.glValidateProgram(program)

    override fun glVertexAttrib1f(indx: Int, x: Float) = api.glVertexAttrib1f(indx, x)

    override fun glVertexAttrib1fv(indx: Int, values: FloatArray) = api.glVertexAttrib1fv(indx, values)

    override fun glVertexAttrib2f(indx: Int, x: Float, y: Float) = api.glVertexAttrib2f(indx, x, y)

    override fun glVertexAttrib2fv(indx: Int, values: FloatArray) = api.glVertexAttrib2fv(indx, values)

    override fun glVertexAttrib3f(indx: Int, x: Float, y: Float, z: Float) = api.glVertexAttrib3f(indx, x, y, z)

    override fun glVertexAttrib3fv(indx: Int, values: FloatArray) = api.glVertexAttrib3fv(indx, values)

    override fun glVertexAttrib4f(indx: Int, x: Float, y: Float, z: Float, w: Float) =
            api.glVertexAttrib4f(indx, x, y, z, w)

    override fun glVertexAttrib4fv(indx: Int, values: FloatArray) = api.glVertexAttrib4fv(indx, values)

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: IByteData) =
            api.glVertexAttribPointer(indx, size, type, normalized, stride, ptr)

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: Int) =
            api.glVertexAttribPointer(indx, size, type, normalized, stride, ptr)


    /** @param name can be used in debug */
    class Request(var name: String? = null, val call: () -> Unit)
}