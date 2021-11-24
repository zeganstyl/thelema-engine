package app.thelema.g3d.particles

import app.thelema.gl.IVertexAttribute

interface IParticleDataChannel<T: Any?> {
    val data: MutableList<T>

    var needWriteToBuffer: Boolean

    val attribute: IVertexAttribute

    fun newDataElement(): T

    fun setToAttribute(value: T)
}

