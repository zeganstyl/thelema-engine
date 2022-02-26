package app.thelema.g3d.particles

import app.thelema.gl.IVertexAccessor

interface IParticleDataChannel<T: Any?> {
    val data: MutableList<T>

    var needWriteToBuffer: Boolean

    val accessor: IVertexAccessor

    fun newDataElement(): T

    fun setToAttribute(value: T)
}

