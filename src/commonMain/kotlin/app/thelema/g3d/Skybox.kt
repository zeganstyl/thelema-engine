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
import app.thelema.ecs.sibling
import app.thelema.g3d.mesh.SkyboxMesh
import app.thelema.gl.meshInstance
import app.thelema.img.TextureCube
import app.thelema.shader.IShader
import app.thelema.shader.Shader
import app.thelema.shader.node.*
import app.thelema.shader.useShader

/** Skybox with shader nodes. Supports velocity rendering (for motion blur) */
class Skybox: IEntityComponent {
    override val componentName: String
        get() = "Skybox"

    override var entityOrNull: IEntity? = null
        set(value) {
            if (field == null) box.mesh.destroy()
            field = value
            box = sibling()
            box.mesh.material = sibling<Material>().apply {
                this.shader = this@Skybox.shader
            }
            value?.meshInstance()
        }

    val shader = Shader()

    val velocityShader: IShader by lazy {
        Shader {
            addNode(velocityOutputNode)
        }
    }

    val vertexNode = shader.addNode(SkyboxVertexNode())

    val velocityOutputNode = OutputNode().apply {
        vertPosition = vertexNode.clipSpacePosition
        fragColor = vertexNode.velocity
        fadeStart = -1f
    }

    val outputNode = shader.addNode(OutputNode().apply {
        vertPosition = vertexNode.worldSpacePosition
        fadeStart = -1f
        cullFaceMode = 0
    })

    val textureNode = TextureCubeNode {
        sRGB = false
        uv = vertexNode.textureCoordinates

        outputNode.fragColor = texColor
    }

    var box = SkyboxMesh {
        mesh.material = sibling<IMaterial>().apply {
            this.shader = this@Skybox.shader
        }
    }

    init {
        shader.depthMask = false
        shader.rootNode = outputNode
    }

    fun setupVelocityShader() {
        getOrCreateEntity().material().setChannel(ShaderChannel.Velocity, velocityShader)
        velocityShader.build()
    }

    fun setupTexture(block: TextureCube.() -> Unit) {
        textureNode.texture = TextureCube().apply(block)
    }

    fun render() {
        shader.useShader { box.mesh.render() }
    }
}

inline fun IEntity.skybox(block: Skybox.() -> Unit) = component(block)
inline fun IEntity.skybox() = component<Skybox>()