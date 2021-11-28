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
import app.thelema.gl.IMesh
import app.thelema.math.Vec4
import app.thelema.shader.IShader
import app.thelema.shader.SimpleShader3D
import kotlin.native.concurrent.ThreadLocal

/** @author zeganstyl */
interface IMaterial: IEntityComponent {
    /** Use [Blending] */
    var alphaMode: String

    /** The higher value the later the object will be rendered */
    var translucentPriority: Int

    /** Default shader */
    var shader: IShader?

    /** Channels may be used to store additional shaders, like velocity and etc */
    val shaderChannels: MutableMap<String, IShader>
}

fun IEntity.material(block: IMaterial.() -> Unit) = component(block)
fun IEntity.material() = component<IMaterial>()

/** @author zeganstyl */
class Material: IMaterial {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            value?.componentOrNull<IMesh>()?.material = this@Material
        }

    override val shaderChannels: MutableMap<String, IShader> = HashMap()

    override var shader: IShader?
        get() = shaderChannels[ShaderChannel.Default]
        set(value) {
            if (value != null) {
                shaderChannels[ShaderChannel.Default] = value
            } else {
                shaderChannels.remove(ShaderChannel.Default)
            }
        }

    override var alphaMode: String = Blending.MASK

    override var translucentPriority: Int = 0

    override val componentName: String
        get() = "Material"

    init {
        shader = DEFAULT_SHADER
    }
}

var DEFAULT_SHADER: IShader? = null
    get() {
        if (field == null)
            field = SimpleShader3D(false).also {
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