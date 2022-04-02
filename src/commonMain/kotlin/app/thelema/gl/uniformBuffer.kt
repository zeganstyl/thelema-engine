package app.thelema.gl

import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.g3d.light.ILight
import app.thelema.math.IMat4
import app.thelema.utils.iterate

interface IUniformBuffer: IGLBuffer {
    override val target: Int
        get() = GL_UNIFORM_BUFFER

    val layout: IUniformLayout
}

open class UniformBuffer(layout: IUniformLayout): IUniformBuffer {
    override val layout: IUniformLayout = layout

    override var bufferHandle: Int = 0

    override var gpuUploadRequested: Boolean = false

    override var usage: Int = GL_STATIC_DRAW

    override var bytes: IByteData = DATA.bytes(layout.bytesCount())
}