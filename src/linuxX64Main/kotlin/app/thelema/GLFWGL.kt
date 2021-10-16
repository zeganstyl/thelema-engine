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

package app.thelema
import glfw.glfwExtensionSupported
import kotlinx.cinterop.*
import app.thelema.data.IByteData
import app.thelema.data.IFloatData
import app.thelema.data.IIntData
import app.thelema.gl.AbstractGL
import app.thelema.gl.GL_SHADING_LANGUAGE_VERSION
import app.thelema.gl.GL_VERSION

class GLFWGL: AbstractGL() {
    override var mainFrameBufferWidth: Int = 0
    override var mainFrameBufferHeight: Int = 0

    override val mainFrameBufferHandle: Int
        get() = 0

    override var majVer: Int = 0
    override var minVer: Int = 0
    override var relVer: Int = 0
    override var glslVer: Int = 0

    val strBufLen = 4096
    val strBuf = nativeHeap.allocArray<ByteVar>(strBufLen)
    val retLen = nativeHeap.alloc<IntVar>()
    val retLenPtr = retLen.ptr

    override fun glGenTextureBase(): Int = uintOut { glfw.glGenTextures(1, it) }.sint()

    override fun glGenBufferBase(): Int = uintOut { glfw.glGenBuffers(1, it) }.sint()

    override fun glGenFrameBufferBase(): Int = uintOut { glfw.glGenFramebuffers(1, it) }.sint()

    override fun glGenRenderBufferBase(): Int = uintOut { glfw.glGenRenderbuffers(1, it) }.sint()

    override fun glDeleteTextureBase(id: Int) {
        glfw.glDeleteTextures(1, id.uintPtr())
    }

    override fun glDeleteBufferBase(id: Int) {
        glfw.glDeleteBuffers(1, id.uintPtr())
    }

    override fun glDeleteFrameBufferBase(id: Int) {
        glfw.glDeleteFramebuffers(1, id.uintPtr())
    }

    override fun glDeleteRenderBufferBase(id: Int) {
        glfw.glDeleteRenderbuffers(1, id.uintPtr())
    }

    override fun glActiveTextureBase(value: Int) {
        glfw.glActiveTexture(value.uint())
    }

    override fun glUseProgramBase(value: Int) {
        glfw.glUseProgram(value.uint())
    }

    override fun glBindBufferBase(target: Int, buffer: Int) {
        glfw.glBindBuffer(target.uint(), buffer.uint())
    }

    override fun glBindVertexArrayBase(value: Int) {
        glfw.glBindVertexArray(value.uint())
    }

    override fun glCullFaceBase(value: Int) {
        glfw.glCullFace(value.uint())
    }

    override fun glBlendFuncBase(factorS: Int, factorD: Int) {
        glfw.glBlendFunc(factorS.uint(), factorD.uint())
    }

    override fun glEnableBase(value: Int) {
        glfw.glEnable(value.uint())
    }

    override fun glDisableBase(value: Int) {
        glfw.glDisable(value.uint())
    }

    override fun glDepthFuncBase(value: Int) {
        glfw.glDepthFunc(value.uint())
    }

    override fun glDepthMaskBase(value: Boolean) {
        glfw.glDepthMask(value.ubyte())
    }

    override fun glBindTextureBase(target: Int, texture: Int) {
        glfw.glBindTexture(target.uint(), texture.uint())
    }

    private fun readStrBuf(): String {
        strBuf[retLen.value + 1] = 0
        return strBuf.toKString()
    }

    override fun initGL() {
        val verStr = glGetString(GL_VERSION)
        val spaceSplit = verStr.split("\\s+".toRegex())
        val dotSplit = spaceSplit[0].split('.')
        majVer = dotSplit[0].toInt()
        minVer = dotSplit[1].toInt()
        relVer = dotSplit.getOrNull(2)?.toInt() ?: 0

        val glslVerStr = glGetString(GL_SHADING_LANGUAGE_VERSION)
        val glslSpaceSplit = glslVerStr.split("\\s+".toRegex())
        val glslDotSplit = glslSpaceSplit[0].split('.')
        glslVer = glslDotSplit[0].toInt() * 100 + glslDotSplit[1].toInt()
    }

    override fun isExtensionSupported(extension: String) = glfwExtensionSupported(extension) == 1

    override fun enableExtension(extension: String): Boolean {
        // TODO
        return true
    }

    override fun glAttachShader(program: Int, shader: Int) {
        glfw.glAttachShader(program.uint(), shader.uint())
    }

    override fun glBindAttribLocation(program: Int, index: Int, name: String) {
        glfw.glBindAttribLocation(program.uint(), index.uint(), name)
    }

    override fun glBlendColor(red: Float, green: Float, blue: Float, alpha: Float) {
        glfw.glBlendColor(red, green, blue, alpha)
    }

    override fun glBlendEquation(mode: Int) {
        glfw.glBlendEquation(mode.uint())
    }

    override fun glBlendEquationSeparate(modeRGB: Int, modeAlpha: Int) {
        glfw.glBlendEquationSeparate(modeRGB.uint(), modeAlpha.uint())
    }

    override fun glBlendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int) {
        glfw.glBlendFuncSeparate(srcRGB.uint(), dstRGB.uint(), srcAlpha.uint(), dstAlpha.uint())
    }

    override fun glBufferData(target: Int, size: Int, data: IByteData?, usage: Int) {
        glfw.glBufferData(target.uint(), (data?.limit ?: 0).toLong(), data?.ptr(), usage.uint())
    }

    override fun glBufferSubData(target: Int, offset: Int, size: Int, data: IByteData) {
        glfw.glBufferSubData(target.uint(), offset.toLong(), size.toLong(), data.ptr())
    }

    override fun glClear(mask: Int) {
        glfw.glClear(mask.uint())
    }

    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        glfw.glClearColor(red, green, blue, alpha)
    }

    override fun glClearDepthf(depth: Float) {
        glfw.glClearDepth(depth.toDouble())
    }

    override fun glClearStencil(s: Int) {
        glfw.glClearStencil(s)
    }

    override fun glColorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) {
        glfw.glColorMask(red.ubyte(), green.ubyte(), blue.ubyte(), alpha.ubyte())
    }

    override fun glCompileShader(shader: Int) {
        glfw.glCompileShader(shader.uint())
    }

    override fun glCompressedTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int,
                                        imageSize: Int, data: IByteData) {
        glfw.glCompressedTexImage2D(target.uint(), level, internalformat.uint(), width, height, border, data.limit, data.ptr())
    }

    override fun glCopyTexImage2D(target: Int, level: Int, internalformat: Int, x: Int, y: Int, width: Int, height: Int, border: Int) {
        glfw.glCopyTexImage2D(target.uint(), level, internalformat.uint(), x, y, width, height, border)
    }

    override fun glCopyTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, x: Int, y: Int, width: Int, height: Int) {
        glfw.glCopyTexSubImage2D(target.uint(), level, xoffset, yoffset, x, y, width, height)
    }

    override fun glCreateProgram(): Int {
        return glfw.glCreateProgram().sint()
    }

    override fun glCreateShader(type: Int): Int {
        return glfw.glCreateShader(type.uint()).sint()
    }

    override fun glCullFace(mode: Int) {
        glfw.glCullFace(mode.uint())
    }

    override fun glDeleteBuffers(buffers: IntArray) {
        glfw.glDeleteBuffers(buffers.size, buffers.uintPtr())
    }

    override fun glDeleteProgram(program: Int) {
        glfw.glDeleteProgram(program.uint())
    }

    override fun glDeleteShader(shader: Int) {
        glfw.glDeleteShader(shader.uint())
    }

    override fun glDeleteTextures(textures: IntArray) {
        glfw.glDeleteTextures(textures.size, textures.uintPtr())
    }

    override fun glDepthFunc(func: Int) {
        glfw.glDepthFunc(func.uint())
    }

    override fun glDepthMask(flag: Boolean) {
        glfw.glDepthMask(flag.ubyte())
    }

    override fun glDepthRangef(zNear: Float, zFar: Float) {
        glfw.glDepthRange(zNear.toDouble(), zFar.toDouble())
    }

    override fun glDetachShader(program: Int, shader: Int) {
        glfw.glDetachShader(program.uint(), shader.uint())
    }

    override fun glDisableVertexAttribArray(index: Int) {
        glfw.glDisableVertexAttribArray(index.uint())
    }

    override fun glDrawArrays(mode: Int, first: Int, count: Int) {
        glfw.glDrawArrays(mode.uint(), first, count)
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: IByteData) {
        glfw.glDrawElements(mode.uint(), count, type.uint(), indices.ptr())
    }

    override fun glEnableVertexAttribArray(index: Int) {
        glfw.glEnableVertexAttribArray(index.uint())
    }

    override fun glFinish() {
        glfw.glFinish()
    }

    override fun glFlush() {
        glfw.glFlush()
    }

    override fun glFrontFace(mode: Int) {
        glfw.glFrontFace(mode.uint())
    }

    override fun glGenBuffers(buffers: IntArray) {
        glfw.glGenBuffers(buffers.size, buffers.uintPtr())
    }

    override fun glGenTextures(n: Int, textures: IntArray) {
        glfw.glGenTextures(n, textures.uintPtr())
    }

    override fun glGetActiveAttrib(program: Int, index: Int, size: IntArray, type: IntArray): String {
        memScoped {
            val sizeC = allocArray<IntVar>(size.size)
            val typeC = allocArray<UIntVar>(type.size)
            glfw.glGetActiveAttrib(program.uint(), index.uint(), strBufLen, retLenPtr, sizeC, typeC, strBuf)
            for (i in size.indices) {
                size[i] = sizeC[i]
            }
            for (i in type.indices) {
                type[i] = typeC[i].toInt()
            }
        }
        return readStrBuf()
    }

    override fun glGetActiveUniform(program: Int, index: Int, size: IntArray, type: IntArray): String {
        memScoped {
            val sizeC = allocArray<IntVar>(size.size)
            val typeC = allocArray<UIntVar>(type.size)
            glfw.glGetActiveUniform(program.uint(), index.uint(), strBufLen, retLenPtr, sizeC, typeC, strBuf)
            for (i in size.indices) {
                size[i] = sizeC[i]
            }
            for (i in type.indices) {
                type[i] = typeC[i].toInt()
            }
        }
        return readStrBuf()
    }

    override fun glGetAttachedShaders(program: Int, maxcount: Int, count: IntArray, shaders: IntArray) {
        memScoped {
            val countC = allocArray<IntVar>(count.size)
            val shadersC = allocArray<UIntVar>(shaders.size)
            glfw.glGetAttachedShaders(program.uint(), maxcount, countC, shadersC)
            for (i in count.indices) {
                count[i] = countC[i]
            }
            for (i in shaders.indices) {
                shaders[i] = shadersC[i].toInt()
            }
        }
    }

    override fun glGetAttribLocation(program: Int, name: String): Int {
        return glfw.glGetAttribLocation(program.uint(), name)
    }

    override fun glGetBooleanv(pname: Int, params: IByteData) {
        glfw.glGetBooleanv(pname.uint(), params.ubytePtr())
    }

    override fun glGetBufferParameteriv(target: Int, pname: Int, params: IntArray) {
        glfw.glGetBufferParameteriv(target.uint(), pname.uint(), params.toCValues())
    }

    override fun glGetError(): Int {
        return glfw.glGetError().sint()
    }

    override fun glGetFloatv(pname: Int, params: FloatArray) {
        memScoped {
            val paramsC = allocArray<FloatVar>(params.size)
            glfw.glGetFloatv(pname.uint(), paramsC)
            for (i in params.indices) {
                params[i] = paramsC[i]
            }
        }
    }

    override fun glGetFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int, params: IntArray) {
        glfw.glGetFramebufferAttachmentParameteriv(target.uint(), attachment.uint(), pname.uint(), params.toCValues())
    }

    override fun glGetIntegerv(pname: Int, params: IntArray) {
        glfw.glGetIntegerv(pname.uint(), params.toCValues())
    }

    override fun glGetProgramInfoLog(program: Int): String {
        glfw.glGetProgramInfoLog(program.uint(), strBufLen, retLenPtr, strBuf)
        return readStrBuf()
    }

    override fun glGetProgramiv(program: Int, pname: Int, params: IntArray) {
        memScoped {
            val paramsC = allocArray<IntVar>(params.size)
            glfw.glGetProgramiv(program.uint(), pname.uint(), paramsC)
            for (i in params.indices) {
                params[i] = paramsC[i]
            }
        }
    }

    override fun glGetShaderInfoLog(shader: Int): String {
        glfw.glGetShaderInfoLog(shader.uint(), strBufLen, retLenPtr, strBuf)
        return readStrBuf()
    }

    override fun glGetShaderPrecisionFormat(shadertype: Int, precisiontype: Int, range: IntArray, precision: IntArray) {
        glfw.glGetShaderPrecisionFormat(shadertype.uint(), precisiontype.uint(), range.toCValues(), precision.toCValues())
    }

    override fun glGetShaderiv(shader: Int, pname: Int, params: IntArray) {
        memScoped {
            val paramsC = allocArray<IntVar>(params.size)
            glfw.glGetShaderiv(shader.uint(), pname.uint(), paramsC)
            for (i in params.indices) {
                params[i] = paramsC[i]
            }
        }
    }

    override fun glGetString(name: Int): String {
        val cstr = glfw.glGetString(name.uint())!!
        var i = 0
        while (cstr[i].toInt() != 0) i++
        return cstr.readBytes(i).toKString()
    }

    override fun glGetTexParameterfv(target: Int, pname: Int, params: FloatArray) {
        memScoped {
            val paramsC = allocArray<FloatVar>(params.size)
            glfw.glGetTexParameterfv(target.uint(), pname.uint(), paramsC)
            for (i in params.indices) {
                params[i] = paramsC[i]
            }
        }
    }

    override fun glGetTexParameteriv(target: Int, pname: Int, params: IntArray) {
        memScoped {
            val paramsC = allocArray<IntVar>(params.size)
            glfw.glGetTexParameteriv(target.uint(), pname.uint(), paramsC)
            for (i in params.indices) {
                params[i] = paramsC[i]
            }
        }
    }

    override fun glGetUniformLocation(program: Int, name: String): Int {
        return glfw.glGetUniformLocation(program.uint(), name)
    }

    override fun glGetUniformfv(program: Int, location: Int, params: FloatArray) {
        memScoped {
            val paramsC = allocArray<FloatVar>(params.size)
            glfw.glGetUniformfv(program.uint(), location, paramsC)
            for (i in params.indices) {
                params[i] = paramsC[i]
            }
        }
    }

    override fun glGetUniformiv(program: Int, location: Int, params: IntArray) {
        memScoped {
            val paramsC = allocArray<IntVar>(params.size)
            glfw.glGetUniformiv(program.uint(), location, paramsC)
            for (i in params.indices) {
                params[i] = paramsC[i]
            }
        }
    }

    override fun glGetVertexAttribPointerv(index: Int, pname: Int, pointer: IntArray) {
        memScoped {
            glfw.glGetVertexAttribPointerv(index.uint(), pname.uint(), ptrVarPtr)
            val intPtr = ptrVar.value?.reinterpret<IntVar>()!!
            for (i in pointer.indices) {
                pointer[i] = intPtr[i]
            }
        }
    }

    override fun glGetVertexAttribfv(index: Int, pname: Int, params: FloatArray) {
        memScoped {
            val paramsC = allocArray<FloatVar>(params.size)
            glfw.glGetVertexAttribfv(index.uint(), pname.uint(), paramsC)
            for (i in params.indices) {
                params[i] = paramsC[i]
            }
        }
    }

    override fun glGetVertexAttribiv(index: Int, pname: Int, params: IntArray) {
        memScoped {
            val paramsC = allocArray<IntVar>(params.size)
            glfw.glGetVertexAttribiv(index.uint(), pname.uint(), paramsC)
            for (i in params.indices) {
                params[i] = paramsC[i]
            }
        }
    }

    override fun glHint(target: Int, mode: Int) {
        glfw.glHint(target.uint(), mode.uint())
    }

    override fun glIsBuffer(buffer: Int): Boolean {
        return glfw.glIsBuffer(buffer.uint()).bool()
    }

    override fun glIsEnabled(cap: Int): Boolean {
        return glfw.glIsEnabled(cap.uint()).bool()
    }

    override fun glIsProgram(program: Int): Boolean {
        return glfw.glIsProgram(program.uint()).bool()
    }

    override fun glIsShader(shader: Int): Boolean {
        return glfw.glIsShader(shader.uint()).bool()
    }

    override fun glIsTexture(texture: Int): Boolean {
        return glfw.glIsTexture(texture.uint()).bool()
    }

    override fun glLineWidth(width: Float) {
        glfw.glLineWidth(width)
    }

    override fun glLinkProgram(program: Int) {
        glfw.glLinkProgram(program.uint())
    }

    override fun glPixelStorei(pname: Int, param: Int) {
        glfw.glPixelStorei(pname.uint(), param)
    }

    override fun glPolygonOffset(factor: Float, units: Float) {
        glfw.glPolygonOffset(factor, units)
    }

    override fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: IByteData) {
        glfw.glReadPixels(x, y, width, height, format.uint(), type.uint(), pixels.ptr())
    }

    override fun glReleaseShaderCompiler() { // nothing to do here
    }

    override fun glSampleCoverage(value: Float, invert: Boolean) {
        glfw.glSampleCoverage(value, invert.ubyte())
    }

    override fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        glfw.glScissor(x, y, width, height)
    }

    override fun glShaderBinary(n: Int, shaders: IntArray, binaryformat: Int, binary: IByteData, length: Int) {
        glfw.glShaderBinary(n, shaders.uintPtr(), binaryformat.uint(), binary.ptr(), binary.limit)
    }

    override fun glShaderSource(shader: Int, string: String) {
        memScoped {
            glfw.glShaderSource(shader.uint(), 1, cValuesOf(string.cstr.getPointer(this)), cValuesOf(string.length))
        }
    }

    override fun glStencilFunc(func: Int, ref: Int, mask: Int) {
        glfw.glStencilFunc(func.uint(), ref, mask.uint())
    }

    override fun glStencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int) {
        glfw.glStencilFuncSeparate(face.uint(), func.uint(), ref, mask.uint())
    }

    override fun glStencilMask(mask: Int) {
        glfw.glStencilMask(mask.uint())
    }

    override fun glStencilMaskSeparate(face: Int, mask: Int) {
        glfw.glStencilMaskSeparate(face.uint(), mask.uint())
    }

    override fun glStencilOp(fail: Int, zfail: Int, zpass: Int) {
        glfw.glStencilOp(fail.uint(), zfail.uint(), zpass.uint())
    }

    override fun glStencilOpSeparate(face: Int, fail: Int, zfail: Int, zpass: Int) {
        glfw.glStencilOpSeparate(face.uint(), fail.uint(), zfail.uint(), zpass.uint())
    }

    override fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int,
                              pixels: IByteData?) {
        glfw.glTexImage2D(target.uint(), level, internalformat, width, height, border, format.uint(), type.uint(), pixels?.ptr())
    }

    override fun glTexParameterf(target: Int, pname: Int, param: Float) {
        glfw.glTexParameterf(target.uint(), pname.uint(), param)
    }

    override fun glTexParameterfv(target: Int, pname: Int, params: FloatArray) {
        memScoped {
            val paramsC = allocArray<FloatVar>(params.size)
            glfw.glTexParameterfv(target.uint(), pname.uint(), paramsC)
            for (i in params.indices) {
                params[i] = paramsC[i]
            }
        }
    }

    override fun glTexParameteri(target: Int, pname: Int, param: Int) {
        glfw.glTexParameteri(target.uint(), pname.uint(), param)
    }

    override fun glTexParameteriv(target: Int, pname: Int, params: IntArray) {
        glfw.glTexParameteriv(target.uint(), pname.uint(), params.toCValues())
    }

    override fun glTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, pixels: IByteData) {
        glfw.glTexSubImage2D(target.uint(), level, xoffset, yoffset, width, height, format.uint(), type.uint(), pixels.ptr())
    }

    override fun glUniform1f(location: Int, x: Float) {
        glfw.glUniform1f(location, x)
    }

    override fun glUniform1fv(location: Int, count: Int, v: FloatArray) {
        glfw.glUniform1fv(location, count, v.toCValues())
    }

    override fun glUniform1fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        glfw.glUniform1fv(location, count, v.refTo(offset))
    }

    override fun glUniform1i(location: Int, x: Int) {
        glfw.glUniform1i(location, x)
    }

    override fun glUniform1iv(location: Int, count: Int, v: IntArray) {
        glfw.glUniform1iv(location, count, v.toCValues())
    }

    override fun glUniform1iv(location: Int, count: Int, v: IntArray, offset: Int) {
        glfw.glUniform1iv(location, count, v.refTo(offset))
    }

    override fun glUniform2f(location: Int, x: Float, y: Float) {
        glfw.glUniform2f(location, x, y)
    }

    override fun glUniform2fv(location: Int, count: Int, v: FloatArray) {
        glfw.glUniform2fv(location, count, v.toCValues())
    }

    override fun glUniform2fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        glfw.glUniform2fv(location, count, v.refTo(offset))
    }

    override fun glUniform2i(location: Int, x: Int, y: Int) {
        glfw.glUniform2i(location, x, y)
    }

    override fun glUniform2iv(location: Int, count: Int, v: IntArray) {
        glfw.glUniform2iv(location, count, v.toCValues())
    }

    override fun glUniform2iv(location: Int, count: Int, v: IntArray, offset: Int) {
        glfw.glUniform2iv(location, count, v.refTo(offset))
    }

    override fun glUniform3f(location: Int, x: Float, y: Float, z: Float) {
        glfw.glUniform3f(location, x, y, z)
    }

    override fun glUniform3fv(location: Int, count: Int, v: FloatArray) {
        glfw.glUniform3fv(location, count, v.toCValues())
    }

    override fun glUniform3fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        glfw.glUniform3fv(location, count, v.refTo(offset))
    }

    override fun glUniform3i(location: Int, x: Int, y: Int, z: Int) {
        glfw.glUniform3i(location, x, y, z)
    }

    override fun glUniform3iv(location: Int, count: Int, v: IntArray) {
        glfw.glUniform3iv(location, count, v.toCValues())
    }

    override fun glUniform3iv(location: Int, count: Int, v: IntArray, offset: Int) {
        glfw.glUniform3iv(location, count, v.refTo(offset))
    }

    override fun glUniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) {
        glfw.glUniform4f(location, x, y, z, w)
    }

    override fun glUniform4fv(location: Int, count: Int, v: FloatArray) {
        glfw.glUniform4fv(location, count, v.toCValues())
    }

    override fun glUniform4fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        glfw.glUniform4fv(location, count, v.refTo(offset))
    }

    override fun glUniform4i(location: Int, x: Int, y: Int, z: Int, w: Int) {
        glfw.glUniform4i(location, x, y, z, w)
    }

    override fun glUniform4iv(location: Int, count: Int, v: IntArray) {
        glfw.glUniform4iv(location, count, v.toCValues())
    }

    override fun glUniform4iv(location: Int, count: Int, v: IntArray, offset: Int) {
        glfw.glUniform4iv(location, count, v.refTo(offset))
    }

    override fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        glfw.glUniformMatrix2fv(location, count, transpose.ubyte(), value.ptr())
    }

    override fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        glfw.glUniformMatrix2fv(location, count, transpose.ubyte(), value.refTo(offset))
    }

    override fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        glfw.glUniformMatrix3fv(location, count, transpose.ubyte(), value.ptr())
    }

    override fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        glfw.glUniformMatrix3fv(location, count, transpose.ubyte(), value.refTo(offset))
    }

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        glfw.glUniformMatrix4fv(location, count, transpose.ubyte(), value.ptr())
    }

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatArray, offset: Int) {
        glfw.glUniformMatrix4fv(location, count, transpose.ubyte(), value.refTo(offset))
    }

    override fun glValidateProgram(program: Int) {
        glfw.glValidateProgram(program.uint())
    }

    override fun glVertexAttrib1f(indx: Int, x: Float) {
        glfw.glVertexAttrib1f(indx.uint(), x)
    }

    override fun glVertexAttrib1fv(indx: Int, values: FloatArray) {
        glfw.glVertexAttrib1f(indx.uint(), values[0])
    }

    override fun glVertexAttrib2f(indx: Int, x: Float, y: Float) {
        glfw.glVertexAttrib2f(indx.uint(), x, y)
    }

    override fun glVertexAttrib2fv(indx: Int, values: FloatArray) {
        glfw.glVertexAttrib2f(indx.uint(), values[0], values[1])
    }

    override fun glVertexAttrib3f(indx: Int, x: Float, y: Float, z: Float) {
        glfw.glVertexAttrib3f(indx.uint(), x, y, z)
    }

    override fun glVertexAttrib3fv(indx: Int, values: FloatArray) {
        glfw.glVertexAttrib3f(indx.uint(), values[0], values[1], values[2])
    }

    override fun glVertexAttrib4f(indx: Int, x: Float, y: Float, z: Float, w: Float) {
        glfw.glVertexAttrib4f(indx.uint(), x, y, z, w)
    }

    override fun glVertexAttrib4fv(indx: Int, values: FloatArray) {
        glfw.glVertexAttrib4f(indx.uint(), values[0], values[1], values[2], values[3])
    }

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: IByteData) {
        glfw.glVertexAttribPointer(indx.uint(), size, type.uint(), normalized.ubyte(), stride, ptr.ptr())
    }

    override fun glViewport(x: Int, y: Int, width: Int, height: Int) {
        glfw.glViewport(x, y, width, height)
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) {
        glfw.glDrawElements(mode.uint(), count, type.uint(), interpretCPointer<ByteVar>(nativeNullPtr + indices.toLong()))
    }

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: Int) {
        glfw.glVertexAttribPointer(indx.uint(), size, type.uint(), normalized.ubyte(), stride, ptr.toLong().toCPointer<ByteVar>())
    }

    override fun glReadBuffer(mode: Int) {
        glfw.glReadBuffer(mode.uint())
    }

    override fun glDrawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, indices: IByteData) {
        glfw.glDrawRangeElements(mode.uint(), start.uint(), end.uint(), count, type.uint(), indices.ptr())
    }

    override fun glDrawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, offset: Int) {
        glfw.glDrawRangeElements(mode.uint(), start.uint(), end.uint(), count, type.uint(), offset.toLong().toCPointer<ByteVar>())
    }

    override fun glTexImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, border: Int, format: Int,
                              type: Int, pixels: IByteData?) {
        glfw.glTexImage3D(target.uint(), level, internalformat, width, height, depth, border, format.uint(), type.uint(), pixels?.ptr())
    }

    override fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int,
                                 format: Int, type: Int, pixels: IByteData) {
        glfw.glTexSubImage3D(target.uint(), level, xoffset, yoffset, zoffset, width, height, depth, format.uint(), type.uint(), pixels.ptr())
    }

    override fun glCopyTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, x: Int, y: Int, width: Int,
                                     height: Int) {
        glfw.glCopyTexSubImage3D(target.uint(), level, xoffset, yoffset, zoffset, x, y, width, height)
    }

    override fun glGenQueries(ids: IntArray) {
        // FIXME
        glfw.glGenQueries(ids.size, ids.uintPtr())
    }

    override fun glGenQueries(): Int {
        return uintOut { glfw.glGenQueries(1, it) }.sint()
    }

    override fun glDeleteQueries(ids: IntArray) {
        glfw.glDeleteQueries(ids.size, ids.uintPtr())
    }

    override fun glDeleteQueries(id: Int) {
        glfw.glDeleteQueries(1, id.uintPtr())
    }

    override fun glIsQuery(id: Int): Boolean {
        return glfw.glIsQuery(id.uint()).bool()
    }

    override fun glBeginQuery(target: Int, id: Int) {
        glfw.glBeginQuery(target.uint(), id.uint())
    }

    override fun glEndQuery(target: Int) {
        glfw.glEndQuery(target.uint())
    }

    override fun glGetQueryiv(target: Int, pname: Int, params: IntArray) {
        // FIXME
        glfw.glGetQueryiv(target.uint(), pname.uint(), params.toCValues())
    }

    override fun glGetQueryObjectuiv(id: Int, pname: Int, params: IntArray) {
        // FIXME
        glfw.glGetQueryObjectuiv(id.uint(), pname.uint(), params.uintPtr())
    }

    override fun glUnmapBuffer(target: Int): Boolean {
        return glfw.glUnmapBuffer(target.uint()).bool()
    }

    override fun glGetBufferPointerv(target: Int, pname: Int): IntArray {
        glfw.glGetBufferPointerv(target.uint(), pname.uint(), ptrVarPtr)
        val intPtr = ptrVar.reinterpret<IntVar>()
        return IntArray(1) { intPtr.value } // TODO may be it must be Long
    }

    override fun glDrawBuffers(n: Int, bufs: IntArray) {
        glfw.glDrawBuffers(bufs.size, bufs.uintPtr())
    }

    override fun glUniformMatrix2x3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        glfw.glUniformMatrix2x3fv(location, count, transpose.ubyte(), value.ptr())
    }

    override fun glUniformMatrix3x2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        glfw.glUniformMatrix3x2fv(location, count, transpose.ubyte(), value.ptr())
    }

    override fun glUniformMatrix2x4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        glfw.glUniformMatrix2x4fv(location, count, transpose.ubyte(), value.ptr())
    }

    override fun glUniformMatrix4x2fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        glfw.glUniformMatrix4x2fv(location, count, transpose.ubyte(), value.ptr())
    }

    override fun glUniformMatrix3x4fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        glfw.glUniformMatrix3x4fv(location, count, transpose.ubyte(), value.ptr())
    }

    override fun glUniformMatrix4x3fv(location: Int, count: Int, transpose: Boolean, value: IFloatData) {
        glfw.glUniformMatrix4x3fv(location, count, transpose.ubyte(), value.ptr())
    }

    override fun glBlitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int,
                                   mask: Int, filter: Int) {
        glfw.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask.uint(), filter.uint())
    }

    override fun glBindFramebuffer(target: Int, framebuffer: Int) {
        glfw.glBindFramebuffer(target.uint(), framebuffer.uint())
    }

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) {
        glfw.glBindRenderbuffer(target.uint(), renderbuffer.uint())
    }

    override fun glCheckFramebufferStatus(target: Int): Int {
        return glfw.glCheckFramebufferStatus(target.uint()).sint()
    }

    override fun glDeleteFramebuffers(framebuffers: IntArray) {
        glfw.glDeleteFramebuffers(framebuffers.size, framebuffers.uintPtr())
    }

    override fun glDeleteRenderbuffers(renderbuffers: IntArray) {
        glfw.glDeleteRenderbuffers(renderbuffers.size, renderbuffers.uintPtr())
    }

    override fun glGenerateMipmap(target: Int) {
        glfw.glGenerateMipmap(target.uint())
    }

    override fun glGenFramebuffers(framebuffers: IntArray) {
        framebuffers.readUInts { glfw.glGenFramebuffers(framebuffers.size, it) }
    }

    override fun glGetRenderbufferParameteriv(target: Int, pname: Int, params: IntArray) {
        // FIXME
        glfw.glGetRenderbufferParameteriv(target.uint(), pname.uint(), params.toCValues())
    }

    override fun glIsFramebuffer(framebuffer: Int): Boolean {
        return glfw.glIsFramebuffer(framebuffer.uint()).bool()
    }

    override fun glIsRenderbuffer(renderbuffer: Int): Boolean {
        return glfw.glIsRenderbuffer(renderbuffer.uint()).bool()
    }

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        glfw.glRenderbufferStorage(target.uint(), internalformat.uint(), width, height)
    }

    override fun glRenderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) {
        glfw.glRenderbufferStorageMultisample(target.uint(), samples, internalformat.uint(), width, height)
    }

    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) {
        glfw.glFramebufferTexture2D(target.uint(), attachment.uint(), textarget.uint(), texture.uint(), level)
    }

    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) {
        glfw.glFramebufferRenderbuffer(target.uint(), attachment.uint(), renderbuffertarget.uint(), renderbuffer.uint())
    }

    override fun glFramebufferTextureLayer(target: Int, attachment: Int, texture: Int, level: Int, layer: Int) {
        glfw.glFramebufferTextureLayer(target.uint(), attachment.uint(), texture.uint(), level, layer)
    }

    override fun glFlushMappedBufferRange(target: Int, offset: Int, length: Int) {
        glfw.glFlushMappedBufferRange(target.uint(), offset.toLong(), length.toLong())
    }

    override fun glDeleteVertexArrays(arrays: IntArray) {
        glfw.glDeleteVertexArrays(1, arrays.uintPtr())
    }

    override fun glDeleteVertexArrays(id: Int) {
        glfw.glDeleteVertexArrays(1, id.uintPtr())
    }

    override fun glGenVertexArrays(arrays: IntArray) {
        // FIXME
        glfw.glGenVertexArrays(arrays.size, arrays.uintPtr())
    }

    override fun glGenVertexArrays(): Int {
        return uintOut { glfw.glGenVertexArrays(1, it) }.sint()
    }

    override fun glIsVertexArray(array: Int): Boolean {
        return glfw.glIsVertexArray(array.uint()).bool()
    }

    override fun glBeginTransformFeedback(primitiveMode: Int) {
        glfw.glBeginTransformFeedback(primitiveMode.uint())
    }

    override fun glEndTransformFeedback() {
        glfw.glEndTransformFeedback()
    }

    override fun glBindBufferRange(target: Int, index: Int, buffer: Int, offset: Int, size: Int) {
        glfw.glBindBufferRange(target.uint(), index.uint(), buffer.uint(), offset.toLong(), size.toLong())
    }

    override fun glBindBufferBase(target: Int, index: Int, buffer: Int) {
        glfw.glBindBufferBase(target.uint(), index.uint(), buffer.uint())
    }

    override fun glTransformFeedbackVaryings(program: Int, varyings: Array<String>, bufferMode: Int) {
        memScoped {
            glfw.glTransformFeedbackVaryings(program.uint(), varyings.size, varyings.toCStringArray(this), bufferMode.uint())
        }
    }

    override fun glVertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) {
        glfw.glVertexAttribIPointer(index.uint(), size, type.uint(), stride, offset.toLong().toCPointer<IntVar>())
    }

    override fun glGetVertexAttribIiv(index: Int, pname: Int, params: IntArray) {
        glfw.glGetVertexAttribIiv(index.uint(), pname.uint(), params.toCValues())
    }

    override fun glGetVertexAttribIuiv(index: Int, pname: Int, params: IntArray) {
        glfw.glGetVertexAttribIuiv(index.uint(), pname.uint(), params.uintPtr())
    }

    override fun glVertexAttribI4i(index: Int, x: Int, y: Int, z: Int, w: Int) {
        glfw.glVertexAttribI4i(index.uint(), x, y, z, w)
    }

    override fun glVertexAttribI4ui(index: Int, x: Int, y: Int, z: Int, w: Int) {
        glfw.glVertexAttribI4ui(index.uint(), x.uint(), y.uint(), z.uint(), w.uint())
    }

    override fun glGetUniformuiv(program: Int, location: Int, params: IntArray) {
        // FIXME
        glfw.glGetUniformuiv(program.uint(), location, params.uintPtr())
    }

    override fun glGetFragDataLocation(program: Int, name: String): Int {
        return glfw.glGetFragDataLocation(program.uint(), name)
    }

    override fun glUniform1uiv(location: Int, count: Int, value: IIntData) {
        glfw.glUniform1uiv(location, count, value.uintPtr())
    }

    override fun glUniform3uiv(location: Int, count: Int, value: IIntData) {
        glfw.glUniform3uiv(location, count, value.uintPtr())
    }

    override fun glUniform4uiv(location: Int, count: Int, value: IIntData) {
        glfw.glUniform4uiv(location, count, value.uintPtr())
    }

    override fun glClearBufferiv(buffer: Int, drawbuffer: Int, value: IIntData) {
        glfw.glClearBufferiv(buffer.uint(), drawbuffer, value.ptr())
    }

    override fun glClearBufferuiv(buffer: Int, drawbuffer: Int, value: IIntData) {
        glfw.glClearBufferuiv(buffer.uint(), drawbuffer, value.uintPtr())
    }

    override fun glClearBufferfv(buffer: Int, drawbuffer: Int, value: IFloatData) {
        glfw.glClearBufferfv(buffer.uint(), drawbuffer, value.ptr())
    }

    override fun glClearBufferfi(buffer: Int, drawbuffer: Int, depth: Float, stencil: Int) {
        glfw.glClearBufferfi(buffer.uint(), drawbuffer, depth, stencil)
    }

    override fun glGetStringi(name: Int, index: Int): String {
        val cstr = glfw.glGetStringi(name.uint(), index.uint())!!
        var i = 0
        while (cstr[i].toInt() != 0) i++
        return cstr.readBytes(i).toKString()
    }

    override fun glCopyBufferSubData(readTarget: Int, writeTarget: Int, readOffset: Int, writeOffset: Int, size: Int) {
        glfw.glCopyBufferSubData(readTarget.uint(), writeTarget.uint(), readOffset.toLong(), writeOffset.toLong(), size.toLong())
    }

    override fun glGetUniformIndices(program: Int, uniformNames: Array<String>, uniformIndices: IIntData) {
        memScoped {
            // FIXME
            glfw.glGetUniformIndices(program.uint(), uniformNames.size, uniformNames.toCStringArray(this), uniformIndices.uintPtr())
        }
    }

    override fun glGetActiveUniformsiv(program: Int, uniformCount: Int, uniformIndices: IntArray, pname: Int, params: IntArray) {
        // FIXME
        glfw.glGetActiveUniformsiv(program.uint(), uniformCount, uniformIndices.uintPtr(), pname.uint(), params.toCValues())
    }

    override fun glGetUniformBlockIndex(program: Int, uniformBlockName: String): Int {
        return glfw.glGetUniformBlockIndex(program.uint(), uniformBlockName).sint()
    }

    override fun glGetActiveUniformBlockiv(program: Int, uniformBlockIndex: Int, pname: Int): Int {
        glfw.glGetActiveUniformBlockiv(program.uint(), uniformBlockIndex.uint(), pname.uint(), int1Ptr)
        return int1.value
    }

    override fun glGetActiveUniformBlockName(program: Int, uniformBlockIndex: Int, length: IntArray, uniformBlockName: IByteData) {
        // FIXME
        glfw.glGetActiveUniformBlockName(program.uint(), uniformBlockIndex.uint(), length.size, length.toCValues(), uniformBlockName.ptr())
    }

    override fun glGetActiveUniformBlockName(program: Int, uniformBlockIndex: Int): String {
        glfw.glGetActiveUniformBlockName(program.uint(), uniformBlockIndex.uint(), strBufLen, retLenPtr, strBuf)
        return readStrBuf()
    }

    override fun glUniformBlockBinding(program: Int, uniformBlockIndex: Int, uniformBlockBinding: Int) {
        glfw.glUniformBlockBinding(program.uint(), uniformBlockIndex.uint(), uniformBlockBinding.uint())
    }

    override fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) {
        glfw.glDrawArraysInstanced(mode.uint(), first, count, instanceCount)
    }

    override fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) {
        glfw.glDrawElementsInstanced(mode.uint(), count, type.uint(), indicesOffset.toLong().toCPointer<ByteVar>(), instanceCount)
    }

    override fun glGetInteger64v(pname: Int, params: LongArray) {
        // FIXME
        glfw.glGetInteger64v(pname.uint(), params.toCValues())
    }

    override fun glGetBufferParameteri64v(target: Int, pname: Int, params: LongArray) {
        // FIXME
        glfw.glGetBufferParameteri64v(target.uint(), pname.uint(), params.toCValues())
    }

    override fun glGenSamplers(): Int {
        return uintOut { glfw.glGenSamplers(1, it) }.sint()
    }

    override fun glGenSamplers(samplers: IntArray) {
        glfw.glGenSamplers(samplers.size, samplers.uintPtr())
    }

    override fun glDeleteSamplers(samplers: IntArray) {
        glfw.glDeleteSamplers(1, samplers.uintPtr())
    }

    override fun glDeleteSamplers(sampler: Int) {
        glfw.glDeleteSamplers(1, sampler.uintPtr())
    }

    override fun glIsSampler(sampler: Int): Boolean {
        return glfw.glIsSampler(sampler.uint()).bool()
    }

    override fun glBindSampler(unit: Int, sampler: Int) {
        glfw.glBindSampler(unit.uint(), sampler.uint())
    }

    override fun glSamplerParameteri(sampler: Int, pname: Int, param: Int) {
        glfw.glSamplerParameteri(sampler.uint(), pname.uint(), param)
    }

    override fun glSamplerParameteriv(sampler: Int, pname: Int, param: IntArray) {
        glfw.glSamplerParameteriv(sampler.uint(), pname.uint(), param.toCValues())
    }

    override fun glSamplerParameterf(sampler: Int, pname: Int, param: Float) {
        glfw.glSamplerParameterf(sampler.uint(), pname.uint(), param)
    }

    override fun glSamplerParameterfv(sampler: Int, pname: Int, param: FloatArray) {
        glfw.glSamplerParameterfv(sampler.uint(), pname.uint(), param.toCValues())
    }

    override fun glGetSamplerParameteriv(sampler: Int, pname: Int, params: IntArray) {
        glfw.glGetSamplerParameteriv(sampler.uint(), pname.uint(), params.toCValues())
    }

    override fun glGetSamplerParameterfv(sampler: Int, pname: Int, params: FloatArray) {
        glfw.glGetSamplerParameterfv(sampler.uint(), pname.uint(), params.toCValues())
    }

    override fun glVertexAttribDivisor(index: Int, divisor: Int) {
        glfw.glVertexAttribDivisor(index.uint(), divisor.uint())
    }

    override fun glBindTransformFeedback(target: Int, id: Int) {
        glfw.glBindTransformFeedback(target.uint(), id.uint())
    }

    override fun glDeleteTransformFeedbacks(ids: IntArray) {
        glfw.glDeleteTransformFeedbacks(1, ids.uintPtr())
    }

    override fun glDeleteTransformFeedbacks(id: Int) {
        glfw.glDeleteTransformFeedbacks(1, id.uintPtr())
    }

    override fun glGenTransformFeedbacks(ids: IntArray) {
        glfw.glGenTransformFeedbacks(ids.size, ids.uintPtr())
    }

    override fun glGenTransformFeedbacks(): Int {
        return uintOut { glfw.glGenTransformFeedbacks(1, it) }.sint()
    }

    override fun glIsTransformFeedback(id: Int): Boolean {
        return glfw.glIsTransformFeedback(id.uint()).bool()
    }

    override fun glPauseTransformFeedback() {
        glfw.glPauseTransformFeedback()
    }

    override fun glResumeTransformFeedback() {
        glfw.glResumeTransformFeedback()
    }

    override fun glProgramParameteri(program: Int, pname: Int, value: Int) {
        glfw.glProgramParameteri(program.uint(), pname.uint(), value)
    }

    override fun glInvalidateFramebuffer(target: Int, numAttachments: Int, attachments: IntArray) {
        glfw.glInvalidateFramebuffer(target.uint(), attachments.size, attachments.uintPtr())
    }

    override fun glInvalidateSubFramebuffer(target: Int, numAttachments: Int, attachments: IntArray, x: Int, y: Int, width: Int, height: Int) {
        glfw.glInvalidateSubFramebuffer(target.uint(), attachments.size, attachments.uintPtr(), x, y, width, height)
    }
}