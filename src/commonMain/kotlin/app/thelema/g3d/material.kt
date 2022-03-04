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

package app.thelema.g3d

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.ecs.componentOrNull
import app.thelema.gl.GL
import app.thelema.gl.IRenderable
import app.thelema.math.Vec4
import app.thelema.shader.IShader
import app.thelema.shader.SimpleShader3D
import app.thelema.utils.iterate

/** @author zeganstyl */
interface IMaterial: IEntityComponent {
    /** Use [Blending] */
    var alphaMode: String

    /** The higher value the later the object will be rendered */
    var renderingOrder: Int

    /** Default shader */
    var shader: IShader?

    /** Channels may be used to store additional shaders, like velocity and etc */
    val channels: Map<String, IShader>

    val listeners: List<MaterialListener>

    fun setChannel(channel: String, shader: IShader?)

    fun addListener(material: MaterialListener)

    fun removeListener(material: MaterialListener)

    fun render(shaderChannel: String?, renderable: IRenderable) {
        val shader = if (shaderChannel != null) channels[shaderChannel] else shader
        if (shader != null) renderable.render(shader)
    }
}

interface MaterialListener {
    fun addedShader(channel: String, shader: IShader) {}

    fun removedShader(channel: String, shader: IShader) {}
}

fun IEntity.material(block: IMaterial.() -> Unit) = component(block)
fun IEntity.material() = component<IMaterial>()

/** Material for meshes
 *
 * @author zeganstyl */
class Material: IMaterial {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            if (shader == null || shader == DEFAULT_SHADER) value?.componentOrNull<IShader>()?.also { shader = it }
        }

    private val _shaderChannels = HashMap<String, IShader>()
    override val channels: Map<String, IShader>
        get() = _shaderChannels

    override var shader: IShader?
        get() = channels[ShaderChannel.Default]
        set(value) {
            if (value != null) {
                _shaderChannels[ShaderChannel.Default] = value
            } else {
                _shaderChannels.remove(ShaderChannel.Default)
            }
        }

    override var alphaMode: String = Blending.MASK

    override var renderingOrder: Int = 0

    override val componentName: String
        get() = "Material"

    private var _listeners: ArrayList<MaterialListener>? = null
    override val listeners: List<MaterialListener>
        get() = _listeners ?: emptyList()

    init {
        shader = DEFAULT_SHADER
    }

    override fun setChannel(channel: String, shader: IShader?)  {
        if (shader != null) {
            _shaderChannels[channel] = shader
            _listeners?.iterate { it.addedShader(channel, shader) }
        } else {
            val s = _shaderChannels.remove(channel)
            if (s != null) _listeners?.iterate { it.removedShader(channel, s) }
        }
    }

    override fun addListener(material: MaterialListener) {
        if (_listeners == null) _listeners = ArrayList()
        _listeners?.add(material)
    }

    override fun removeListener(material: MaterialListener) {
        _listeners?.remove(material)
    }

    override fun addedSiblingComponent(component: IEntityComponent) {
        if ((shader == null || shader == DEFAULT_SHADER) && component is IShader) shader = component
    }
}

var DEFAULT_SHADER: IShader? = null
    get() {
        if (field == null)
            field = SimpleShader3D().also {
                it.renderAttributeName = ""
                it.color = Vec4(0.5f, 0.5f, 0.5f, 1f)
                GL.call { it.initShader() }
            }
        return field
    }

var DEFAULT_MATERIAL: IMaterial? = null
    get() {
        if (field == null)
            field = Material()
        return field
    }