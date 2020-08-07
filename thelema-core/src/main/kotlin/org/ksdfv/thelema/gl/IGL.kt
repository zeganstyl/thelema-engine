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

import org.ksdfv.thelema.APP
import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.data.IFloatData
import org.ksdfv.thelema.data.IIntData
import org.ksdfv.thelema.img.IImage

/**
 * OpenGL API
 * [OpenGL API documentation](https://www.khronos.org/registry/OpenGL-Refpages/es3.0/)
 * @author zeganstyl */
interface IGL {
    val mainFrameBufferHandle: Int
        get() = 0

    /** Default frame buffer width */
    val mainFrameBufferWidth: Int
        get() = throw NotImplementedError()

    /** Default frame buffer height */
    val mainFrameBufferHeight: Int
        get() = throw NotImplementedError()

    /** Major version of OpenGL/ES/WebGL */
    val majVer: Int
        get() = throw NotImplementedError()
    /** Minor version of OpenGL/ES/WebGL */
    val minVer: Int
        get() = throw NotImplementedError()
    /** Release version of OpenGL/ES/WebGL */
    val relVer: Int
        get() = throw NotImplementedError()

    /** Max supported GLSL version
     * For example 100, 330, 410 and etc */
    val glslVer: Int
        get() = throw NotImplementedError()

    val glesMajVer: Int
        get() = majVer

    val glesMinVer: Int
        get() = minVer

    val isGLES: Boolean
        get() = APP.platformType == APP.WebGL || APP.platformType == APP.Android

    fun initGL() {
        throw NotImplementedError()
    }

    /** Reset state without destroying this object */
    fun reset() {}

    fun isExtensionSupported(extension: String): Boolean {
        throw NotImplementedError()
    }

    /** @return true if extension successfully enabled */
    fun enableExtension(extension: String): Boolean {
        throw NotImplementedError()
    }

    /**
     * @param extension extension name
     * @param args arguments that will be passed into function
     * */
    fun callExtensionFunction(extension: String, functionName: String, args: List<Any?>): Any? {
        throw NotImplementedError()
    }

    fun setExtensionParam(extension: String, paramName: String, value: Any?) {
        throw NotImplementedError()
    }

    fun getExtensionParam(extension: String, paramName: String): Any? {
        throw NotImplementedError()
    }
    
    fun glReadBuffer(mode: Int) {
        throw NotImplementedError()
    }

    fun glDrawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, indices: IByteData) {
        throw NotImplementedError()
    }

    fun glDrawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, offset: Int) {
        throw NotImplementedError()
    }

    fun glTexImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, border: Int, format: Int, type: Int, pixels: IByteData?) {
        throw NotImplementedError()
    }

    fun glTexImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, border: Int, format: Int, type: Int, offset: Int) {
        throw NotImplementedError()
    }

    fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, pixels: IByteData) {
        throw NotImplementedError()
    }

    fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, offset: Int) {
        throw NotImplementedError()
    }

    fun glCopyTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, x: Int, y: Int, width: Int, height: Int) {
        throw NotImplementedError()
    }

    fun glGenQueries(ids: IntArray) { throw NotImplementedError() }

    fun glGenQueries(): Int { throw NotImplementedError() }

    fun glDeleteQueries(id: Int) { throw NotImplementedError() }

    fun glDeleteQueries(ids: IntArray) { throw NotImplementedError() }

    fun glIsQuery(id: Int): Boolean {
        throw NotImplementedError()
    }

    fun glBeginQuery(target: Int, id: Int) {
        throw NotImplementedError()
    }

    fun glEndQuery(target: Int) {
        throw NotImplementedError()
    }

    fun glGetQueryiv(target: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetQueryObjectuiv(id: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glUnmapBuffer(target: Int): Boolean {
        throw NotImplementedError()
    }

    fun glGetBufferPointerv(target: Int, pname: Int): IntArray {
        throw NotImplementedError()
    }

    fun glDrawBuffers(n: Int, bufs: IntArray) {
        throw NotImplementedError()
    }

    fun glUniformMatrix2x3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        throw NotImplementedError()
    }

    fun glUniformMatrix3x2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        throw NotImplementedError()
    }

    fun glUniformMatrix2x4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        throw NotImplementedError()
    }

    fun glUniformMatrix4x2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        throw NotImplementedError()
    }

    fun glUniformMatrix3x4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        throw NotImplementedError()
    }

    fun glUniformMatrix4x3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        throw NotImplementedError()
    }

    fun glBlitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: Int, filter: Int) {
        throw NotImplementedError()
    }

    fun glRenderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) {
        throw NotImplementedError()
    }

    fun glFramebufferTextureLayer(target: Int, attachment: Int, texture: Int, level: Int, layer: Int) {
        throw NotImplementedError()
    }

    fun glFlushMappedBufferRange(target: Int, offset: Int, length: Int) {
        throw NotImplementedError()
    }

    fun glBindVertexArray(array: Int) {
        throw NotImplementedError()
    }

    fun glDeleteVertexArrays(arrays: IntArray) {
        throw NotImplementedError()
    }

    fun glDeleteVertexArrays(id: Int) {
        throw NotImplementedError()
    }

    fun glGenVertexArrays(arrays: IntArray) {
        throw NotImplementedError()
    }

    fun glGenVertexArrays(): Int {
        throw NotImplementedError()
    }

    fun glIsVertexArray(array: Int): Boolean {
        throw NotImplementedError()
    }

    fun glBeginTransformFeedback(primitiveMode: Int) {
        throw NotImplementedError()
    }

    fun glEndTransformFeedback() {
        throw NotImplementedError()
    }

    fun glBindBufferRange(target: Int, index: Int, buffer: Int, offset: Int, size: Int) {
        throw NotImplementedError()
    }

    fun glBindBufferBase(target: Int, index: Int, buffer: Int) {
        throw NotImplementedError()
    }

    fun glTransformFeedbackVaryings(program: Int, varyings: Array<String>, bufferMode: Int) {
        throw NotImplementedError()
    }

    fun glVertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) {
        throw NotImplementedError()
    }

    fun glGetVertexAttribIiv(index: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetVertexAttribIuiv(index: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glVertexAttribI4i(index: Int, x: Int, y: Int, z: Int, w: Int) {
        throw NotImplementedError()
    }

    fun glVertexAttribI4ui(index: Int, x: Int, y: Int, z: Int, w: Int) {
        throw NotImplementedError()
    }

    fun glGetUniformuiv(program: Int, location: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetFragDataLocation(program: Int, name: String): Int {
        throw NotImplementedError()
    }

    fun glUniform1uiv(location: Int, count: Int, value: IIntData) {
        throw NotImplementedError()
    }

    fun glUniform3uiv(location: Int, count: Int, value: IIntData) {
        throw NotImplementedError()
    }

    fun glUniform4uiv(location: Int, count: Int, value: IIntData) {
        throw NotImplementedError()
    }

    fun glClearBufferiv(buffer: Int, drawbuffer: Int, value: IIntData) {
        throw NotImplementedError()
    }

    fun glClearBufferuiv(buffer: Int, drawbuffer: Int, value: IIntData) {
        throw NotImplementedError()
    }

    fun glClearBufferfv(buffer: Int, drawbuffer: Int, value: IFloatData) {
        throw NotImplementedError()
    }

    fun glClearBufferfi(buffer: Int, drawbuffer: Int, depth: Float, stencil: Int) {
        throw NotImplementedError()
    }

    fun glGetStringi(name: Int, index: Int): String {
        throw NotImplementedError()
    }

    fun glCopyBufferSubData(readTarget: Int, writeTarget: Int, readOffset: Int, writeOffset: Int, size: Int) {
        throw NotImplementedError()
    }

    fun glGetUniformIndices(program: Int, uniformNames: Array<String>, uniformIndices: IIntData) {
        throw NotImplementedError()
    }

    fun glGetActiveUniformsiv(program: Int, uniformCount: Int, uniformIndices: IntArray, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetUniformBlockIndex(program: Int, uniformBlockName: String): Int {
        throw NotImplementedError()
    }

    fun glGetActiveUniformBlockiv(program: Int, uniformBlockIndex: Int, pname: Int): Int {
        throw NotImplementedError()
    }

    fun glGetActiveUniformBlockName(program: Int, uniformBlockIndex: Int, length: IntArray, uniformBlockName: IByteData) {
        throw NotImplementedError()
    }

    fun glGetActiveUniformBlockName(program: Int, uniformBlockIndex: Int): String {
        throw NotImplementedError()
    }

    fun glUniformBlockBinding(program: Int, uniformBlockIndex: Int, uniformBlockBinding: Int) {
        throw NotImplementedError()
    }

    fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) {
        throw NotImplementedError()
    }

    fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) {
        throw NotImplementedError()
    }

    fun glGetInteger64v(pname: Int, params: LongArray) {
        throw NotImplementedError()
    }

    fun glGetBufferParameteri64v(target: Int, pname: Int, params: LongArray) {
        throw NotImplementedError()
    }

    fun glGenSamplers(samplers: IntArray) {
        throw NotImplementedError()
    }

    fun glGenSamplers(): Int {
        throw NotImplementedError()
    }

    fun glDeleteSamplers(samplers: IntArray) {
        throw NotImplementedError()
    }

    fun glDeleteSamplers(sampler: Int) {
        throw NotImplementedError()
    }

    fun glIsSampler(sampler: Int): Boolean {
        throw NotImplementedError()
    }

    fun glBindSampler(unit: Int, sampler: Int) {
        throw NotImplementedError()
    }

    fun glSamplerParameteri(sampler: Int, pname: Int, param: Int) {
        throw NotImplementedError()
    }

    fun glSamplerParameteriv(sampler: Int, pname: Int, param: IntArray) {
        throw NotImplementedError()
    }

    fun glSamplerParameterf(sampler: Int, pname: Int, param: Float) {
        throw NotImplementedError()
    }

    fun glSamplerParameterfv(sampler: Int, pname: Int, param: FloatArray) {
        throw NotImplementedError()
    }

    fun glGetSamplerParameteriv(sampler: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetSamplerParameterfv(sampler: Int, pname: Int, params: FloatArray) {
        throw NotImplementedError()
    }

    fun glVertexAttribDivisor(index: Int, divisor: Int) {
        throw NotImplementedError()
    }

    fun glBindTransformFeedback(target: Int, id: Int) { throw NotImplementedError() }

    fun glDeleteTransformFeedbacks(ids: IntArray) { throw NotImplementedError() }

    fun glDeleteTransformFeedbacks(id: Int) { throw NotImplementedError() }

    fun glGenTransformFeedbacks(ids: IntArray) { throw NotImplementedError() }

    fun glGenTransformFeedbacks(): Int { throw NotImplementedError() }

    fun glIsTransformFeedback(id: Int): Boolean { throw NotImplementedError() }

    fun glPauseTransformFeedback() { throw NotImplementedError() }

    fun glResumeTransformFeedback() { throw NotImplementedError() }

    fun glProgramParameteri(program: Int, pname: Int, value: Int) { throw NotImplementedError() }

    fun glInvalidateFramebuffer(target: Int, numAttachments: Int, attachments: IntArray) { throw NotImplementedError() }

    fun glInvalidateSubFramebuffer(target: Int, numAttachments: Int, attachments: IntArray, x: Int, y: Int, width: Int, height: Int) {
        throw NotImplementedError()
    }

    fun glActiveTexture(texture: Int) { throw NotImplementedError() }

    fun glBindTexture(target: Int, texture: Int) { throw NotImplementedError() }

    /** [OpenGL API documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glBlendFunc.xml) */
    fun glBlendFunc(sfactor: Int, dfactor: Int) { throw NotImplementedError() }

    fun glClear(mask: Int) { throw NotImplementedError() }

    fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) { throw NotImplementedError() }

    fun glClearDepthf(depth: Float) { throw NotImplementedError() }

    fun glClearStencil(s: Int) { throw NotImplementedError() }

    fun glColorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) {
        throw NotImplementedError()
    }

    fun glCompressedTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, imageSize: Int, data: IByteData) {
        throw NotImplementedError()
    }

    fun glCompressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, imageSize: Int, data: IByteData) {
        throw NotImplementedError()
    }

    fun glCopyTexImage2D(target: Int, level: Int, internalformat: Int, x: Int, y: Int, width: Int, height: Int, border: Int) {
        throw NotImplementedError()
    }

    fun glCopyTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, x: Int, y: Int, width: Int, height: Int) {
        throw NotImplementedError()
    }

    fun glCullFace(mode: Int) {
        throw NotImplementedError()
    }

    fun glDeleteTextures(textures: IntArray) {
        throw NotImplementedError()
    }

    fun glDeleteTexture(texture: Int) {
        throw NotImplementedError()
    }

    /** [OpenGL API documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glDepthFunc.xml) */
    fun glDepthFunc(func: Int) {
        throw NotImplementedError()
    }

    fun glDepthMask(flag: Boolean) {
        throw NotImplementedError()
    }

    fun glDepthRangef(zNear: Float, zFar: Float) {
        throw NotImplementedError()
    }

    fun glDisable(cap: Int) {
        throw NotImplementedError()
    }

    fun glDrawArrays(mode: Int, first: Int, count: Int) {
        throw NotImplementedError()
    }

    fun glDrawElements(mode: Int, count: Int, type: Int, indices: IByteData) {
        throw NotImplementedError()
    }

    /** [OpenGL API documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glEnable.xml) */
    fun glEnable(cap: Int) {
        throw NotImplementedError()
    }

    fun glFinish() {
        throw NotImplementedError()
    }

    fun glFlush() {
        throw NotImplementedError()
    }

    fun glFrontFace(mode: Int) {
        throw NotImplementedError()
    }

    fun glGenTextures(n: Int, textures: IntArray) {
        throw NotImplementedError()
    }

    fun glGenTexture(): Int {
        throw NotImplementedError()
    }

    fun glGetError(): Int {
        throw NotImplementedError()
    }

    fun glGetIntegerv(pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetString(name: Int): String? {
        throw NotImplementedError()
    }

    fun glHint(target: Int, mode: Int) {
        throw NotImplementedError()
    }

    fun glLineWidth(width: Float) {
        throw NotImplementedError()
    }

    fun glPixelStorei(pname: Int, param: Int) {
        throw NotImplementedError()
    }

    fun glPolygonOffset(factor: Float, units: Float) {
        throw NotImplementedError()
    }

    fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: IByteData) {
        throw NotImplementedError()
    }

    fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        throw NotImplementedError()
    }

    fun glStencilFunc(func: Int, ref: Int, mask: Int) {
        throw NotImplementedError()
    }

    fun glStencilMask(mask: Int) {
        throw NotImplementedError()
    }

    fun glStencilOp(fail: Int, zfail: Int, zpass: Int) {
        throw NotImplementedError()
    }

    fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: IByteData?) {
        throw NotImplementedError()
    }

    /** May be used in WebGL */
    fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, image: IImage?) {
        glTexImage2D(target, level, internalformat, width, height, border, format, type, image?.bytes)
    }

    fun glTexParameterf(target: Int, pname: Int, param: Float) {
        throw NotImplementedError()
    }

    fun glTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, pixels: IByteData) {
        throw NotImplementedError()
    }

    fun glViewport(x: Int, y: Int, width: Int, height: Int) {
        throw NotImplementedError()
    }

    fun glAttachShader(program: Int, shader: Int) {
        throw NotImplementedError()
    }

    fun glBindAttribLocation(program: Int, index: Int, name: String) {
        throw NotImplementedError()
    }

    fun glBindBuffer(target: Int, buffer: Int) {
        throw NotImplementedError()
    }

    fun glBindFramebuffer(target: Int, framebuffer: Int) {
        throw NotImplementedError()
    }

    fun glBindRenderbuffer(target: Int, renderbuffer: Int) {
        throw NotImplementedError()
    }

    fun glBlendColor(red: Float, green: Float, blue: Float, alpha: Float) {
        throw NotImplementedError()
    }

    fun glBlendEquation(mode: Int) {
        throw NotImplementedError()
    }

    fun glBlendEquationSeparate(modeRGB: Int, modeAlpha: Int) {
        throw NotImplementedError()
    }

    fun glBlendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int) {
        throw NotImplementedError()
    }

    fun glBufferData(target: Int, size: Int, data: IByteData?, usage: Int) {
        throw NotImplementedError()
    }

    fun glBufferSubData(target: Int, offset: Int, size: Int, data: IByteData) {
        throw NotImplementedError()
    }

    fun glCheckFramebufferStatus(target: Int): Int {
        throw NotImplementedError()
    }

    fun glCompileShader(shader: Int) {
        throw NotImplementedError()
    }

    fun glCreateProgram(): Int {
        throw NotImplementedError()
    }

    fun glCreateShader(type: Int): Int {
        throw NotImplementedError()
    }

    fun glDeleteBuffer(buffer: Int) {
        throw NotImplementedError()
    }

    fun glDeleteBuffers(buffers: IntArray) {
        throw NotImplementedError()
    }

    fun glDeleteFramebuffer(framebuffer: Int) {
        throw NotImplementedError()
    }

    fun glDeleteFramebuffers(framebuffers: IntArray) {
        throw NotImplementedError()
    }

    fun glDeleteProgram(program: Int) {
        throw NotImplementedError()
    }

    fun glDeleteRenderbuffer(renderbuffer: Int) {
        throw NotImplementedError()
    }

    fun glDeleteRenderbuffers(renderbuffers: IntArray) {
        throw NotImplementedError()
    }

    fun glDeleteShader(shader: Int) {
        throw NotImplementedError()
    }

    fun glDetachShader(program: Int, shader: Int) {
        throw NotImplementedError()
    }

    fun glDisableVertexAttribArray(index: Int) {
        throw NotImplementedError()
    }

    fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) {
        throw NotImplementedError()
    }

    fun glEnableVertexAttribArray(index: Int) {
        throw NotImplementedError()
    }

    fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) {
        throw NotImplementedError()
    }

    fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) {
        throw NotImplementedError()
    }

    fun glGenBuffer(): Int {
        throw NotImplementedError()
    }

    fun glGenBuffers(buffers: IntArray) {
        throw NotImplementedError()
    }

    fun glGenerateMipmap(target: Int) {
        throw NotImplementedError()
    }

    fun glGenFramebuffer(): Int {
        throw NotImplementedError()
    }

    fun glGenFramebuffers(framebuffers: IntArray) {
        throw NotImplementedError()
    }

    fun glGenRenderbuffer(): Int {
        throw NotImplementedError()
    }

    fun glGenRenderbuffers(renderbuffers: IntArray) {
        throw NotImplementedError()
    }

    // deviates
    fun glGetActiveAttrib(program: Int, index: Int, size: IntArray, type: IntArray): String {
        throw NotImplementedError()
    }

    // deviates
    fun glGetActiveUniform(program: Int, index: Int, size: IntArray, type: IntArray): String {
        throw NotImplementedError()
    }

    fun glGetAttachedShaders(program: Int, maxcount: Int, count: IntArray, shaders: IntArray) {
        throw NotImplementedError()
    }

    fun glGetAttribLocation(program: Int, name: String): Int {
        throw NotImplementedError()
    }

    fun glGetBooleanv(pname: Int, params: IByteData) {
        throw NotImplementedError()
    }

    fun glGetBufferParameteriv(target: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetFloatv(pname: Int, params: FloatArray) {
        throw NotImplementedError()
    }

    fun glGetFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetProgramiv(program: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetProgramInfoLog(program: Int): String {
        throw NotImplementedError()
    }

    fun glGetRenderbufferParameteriv(target: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetShaderiv(shader: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetShaderInfoLog(shader: Int): String {
        throw NotImplementedError()
    }

    fun glGetShaderPrecisionFormat(shadertype: Int, precisiontype: Int, range: IntArray, precision: IntArray) {
        throw NotImplementedError()
    }

    fun glGetTexParameterfv(target: Int, pname: Int, params: FloatArray) {
        throw NotImplementedError()
    }

    fun glGetTexParameteriv(target: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetUniformfv(program: Int, location: Int, params: FloatArray) {
        throw NotImplementedError()
    }

    fun glGetUniformiv(program: Int, location: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetUniformLocation(program: Int, name: String): Int {
        throw NotImplementedError()
    }

    fun glGetVertexAttribfv(index: Int, pname: Int, params: FloatArray) {
        throw NotImplementedError()
    }

    fun glGetVertexAttribiv(index: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glGetVertexAttribPointerv(index: Int, pname: Int, pointer: IntArray) {
        throw NotImplementedError()
    }

    fun glIsBuffer(buffer: Int): Boolean {
        throw NotImplementedError()
    }

    fun glIsEnabled(cap: Int): Boolean {
        throw NotImplementedError()
    }

    fun glIsFramebuffer(framebuffer: Int): Boolean {
        throw NotImplementedError()
    }

    fun glIsProgram(program: Int): Boolean {
        throw NotImplementedError()
    }

    fun glIsRenderbuffer(renderbuffer: Int): Boolean {
        throw NotImplementedError()
    }

    fun glIsShader(shader: Int): Boolean {
        throw NotImplementedError()
    }

    fun glIsTexture(texture: Int): Boolean {
        throw NotImplementedError()
    }

    fun glLinkProgram(program: Int) {
        throw NotImplementedError()
    }

    fun glReleaseShaderCompiler() {
        throw NotImplementedError()
    }

    fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        throw NotImplementedError()
    }

    fun glSampleCoverage(value: Float, invert: Boolean) {
        throw NotImplementedError()
    }

    fun glShaderBinary(n: Int, shaders: IntArray, binaryformat: Int, binary: IByteData, length: Int) {
        throw NotImplementedError()
    }

    fun glShaderSource(shader: Int, string: String) {
        throw NotImplementedError()
    }

    fun glStencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int) {
        throw NotImplementedError()
    }

    fun glStencilMaskSeparate(face: Int, mask: Int) {
        throw NotImplementedError()
    }

    fun glStencilOpSeparate(face: Int, fail: Int, zfail: Int, zpass: Int) {
        throw NotImplementedError()
    }

    fun glTexParameterfv(target: Int, pname: Int, params: FloatArray) {
        throw NotImplementedError()
    }

    fun glTexParameteri(target: Int, pname: Int, param: Int) {
        throw NotImplementedError()
    }

    fun glTexParameteriv(target: Int, pname: Int, params: IntArray) {
        throw NotImplementedError()
    }

    fun glUniform1f(location: Int, x: Float) {
        throw NotImplementedError()
    }

    fun glUniform1fv(location: Int, count: Int, v: FloatArray) {
        throw NotImplementedError()
    }

    fun glUniform1fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        throw NotImplementedError()
    }

    fun glUniform1i(location: Int, x: Int) {
        throw NotImplementedError()
    }

    fun glUniform1iv(location: Int, count: Int, v: IntArray) {
        throw NotImplementedError()
    }

    fun glUniform1iv(location: Int, count: Int, v: IntArray, offset: Int) {
        throw NotImplementedError()
    }

    fun glUniform2f(location: Int, x: Float, y: Float) {
        throw NotImplementedError()
    }

    fun glUniform2fv(location: Int, count: Int, v: FloatArray) {
        throw NotImplementedError()
    }

    fun glUniform2fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        throw NotImplementedError()
    }

    fun glUniform2i(location: Int, x: Int, y: Int) {
        throw NotImplementedError()
    }

    fun glUniform2iv(location: Int, count: Int, v: IntArray) {
        throw NotImplementedError()
    }

    fun glUniform2iv(location: Int, count: Int, v: IntArray, offset: Int) {
        throw NotImplementedError()
    }

    fun glUniform3f(location: Int, x: Float, y: Float, z: Float) {
        throw NotImplementedError()
    }

    fun glUniform3fv(location: Int, count: Int, v: FloatArray) {
        throw NotImplementedError()
    }

    fun glUniform3fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        throw NotImplementedError()
    }

    fun glUniform3i(location: Int, x: Int, y: Int, z: Int) {
        throw NotImplementedError()
    }

    fun glUniform3iv(location: Int, count: Int, v: IntArray) {
        throw NotImplementedError()
    }

    fun glUniform3iv(location: Int, count: Int, v: IntArray, offset: Int) {
        throw NotImplementedError()
    }

    fun glUniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) {
        throw NotImplementedError()
    }

    fun glUniform4fv(location: Int, count: Int, v: FloatArray) {
        throw NotImplementedError()
    }

    fun glUniform4fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        throw NotImplementedError()
    }

    fun glUniform4i(location: Int, x: Int, y: Int, z: Int, w: Int) {
        throw NotImplementedError()
    }

    fun glUniform4iv(location: Int, count: Int, v: IntArray) {
        throw NotImplementedError()
    }

    fun glUniform4iv(location: Int, count: Int, v: IntArray, offset: Int) {
        throw NotImplementedError()
    }

    fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        throw NotImplementedError()
    }

    fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        throw NotImplementedError()
    }

    fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        throw NotImplementedError()
    }

    fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        throw NotImplementedError()
    }

    fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        throw NotImplementedError()
    }

    fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        throw NotImplementedError()
    }

    fun glUseProgram(program: Int) {
        throw NotImplementedError()
    }

    fun glValidateProgram(program: Int) {
        throw NotImplementedError()
    }

    fun glVertexAttrib1f(indx: Int, x: Float) {
        throw NotImplementedError()
    }

    fun glVertexAttrib1fv(indx: Int, values: FloatArray) {
        throw NotImplementedError()
    }

    fun glVertexAttrib2f(indx: Int, x: Float, y: Float) {
        throw NotImplementedError()
    }

    fun glVertexAttrib2fv(indx: Int, values: FloatArray) {
        throw NotImplementedError()
    }

    fun glVertexAttrib3f(indx: Int, x: Float, y: Float, z: Float) {
        throw NotImplementedError()
    }

    fun glVertexAttrib3fv(indx: Int, values: FloatArray) {
        throw NotImplementedError()
    }

    fun glVertexAttrib4f(indx: Int, x: Float, y: Float, z: Float, w: Float) {
        throw NotImplementedError()
    }

    fun glVertexAttrib4fv(indx: Int, values: FloatArray) {
        throw NotImplementedError()
    }

    /**
     * In OpenGl core profiles (3.1+), passing a pointer to client memory is not valid.
     * In 3.0 and later, use the other version of this function instead, pass a zero-based
     * offset which references the buffer currently bound to GL_ARRAY_BUFFER.
     */
    fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: IByteData) {
        throw NotImplementedError()
    }

    fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: Int) {
        throw NotImplementedError()
    }

    fun getErrorString(error: Int = GL.glGetError()) = when (error) {
        GL_NO_ERROR -> "GL_NO_ERROR"
        GL_INVALID_ENUM -> "GL_INVALID_ENUM"
        GL_INVALID_OPERATION -> "GL_INVALID_OPERATION"
        GL_INVALID_FRAMEBUFFER_OPERATION -> "GL_INVALID_FRAMEBUFFER_OPERATION"
        GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY"
        else -> "Unknown GL error"
    }
}