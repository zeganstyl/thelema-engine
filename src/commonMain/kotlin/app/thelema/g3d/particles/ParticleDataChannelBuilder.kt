package app.thelema.g3d.particles

import app.thelema.gl.IVertexAccessor
import app.thelema.math.*

object ParticleDataChannelBuilder {
    val float = InstanceDataChannelBuilder({ 0f }) { setFloat(it) }
    val vec2 = InstanceDataChannelBuilder<IVec2>({ Vec2() }) { setVec2(it) }
    val vec3 = InstanceDataChannelBuilder<IVec3>({ Vec3() }) { setVec3(it) }
    val vec4 = InstanceDataChannelBuilder<IVec4>({ Vec4() }) { setVec4(it) }
}

interface IInstanceDataChannelBuilder<T: Any?> {
    fun build(attribute: IVertexAccessor): IParticleDataChannel<T>
}

class InstanceDataChannelBuilder<T: Any?>(
    val createElement: () -> T,
    val setToAttributeBlock: IVertexAccessor.(value: T) -> Unit
): IInstanceDataChannelBuilder<T> {
    override fun build(attribute: IVertexAccessor) = object : IParticleDataChannel<T> {
        override val data: MutableList<T> = ArrayList()
        override var needWriteToBuffer: Boolean = false
        override val accessor: IVertexAccessor = attribute

        override fun newDataElement(): T = createElement()

        override fun setToAttribute(value: T) {
            setToAttributeBlock(attribute, value)
        }
    }
}