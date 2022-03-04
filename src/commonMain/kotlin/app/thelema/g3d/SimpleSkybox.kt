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
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.mesh.SkyboxMesh
import app.thelema.gl.GL
import app.thelema.gl.GL_BACK
import app.thelema.gl.meshInstance
import app.thelema.img.TextureCube
import app.thelema.shader.Shader
import app.thelema.shader.useShader

/** Simple skybox */
class SimpleSkybox(
    fragCode: String? = null,
    shaderFloatPrecision: String = "mediump",
    shaderProfile: String = "",
    var texture: TextureCube = TextureCube()
): IEntityComponent {
    override val componentName: String
        get() = "SimpleSkybox"

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            box = sibling()
            box.mesh.material = sibling<IMaterial>().apply {
                this.shader = this@SimpleSkybox.shader
            }
            value?.meshInstance()
        }

    var box = SkyboxMesh {
        mesh.material = sibling<IMaterial>().apply {
            this.shader = this@SimpleSkybox.shader
        }
    }

    val shader = Shader(
        vertCode = """
attribute vec3 POSITION;
varying vec3 vPosition;

uniform mat4 viewProj;
uniform vec3 camPos;
uniform float camFar;

void main () {
    vPosition = POSITION;
    gl_Position = viewProj * vec4(POSITION * camFar + camPos, 1.0);
}
""",
        fragCode = fragCode ?: """
varying vec3 vPosition;
uniform samplerCube tex;

void main () {
    gl_FragColor = texture(tex, vPosition);
}""",
        floatPrecision = shaderFloatPrecision,
        profile = shaderProfile
    )

    init {
        shader.depthMask = false
        shader.onBind {
            shader["viewProj"] = ActiveCamera.viewProjectionMatrix
            shader["camFar"] = ActiveCamera.far
            shader["camPos"] = ActiveCamera.eye
            texture.bind(0)
            if (GL.isCullFaceEnabled) GL.cullFaceMode = GL_BACK
        }
    }

    fun render() {
        shader.useShader { box.mesh.render() }
    }
}

fun IEntity.simpleSkybox(block: SimpleSkybox.() -> Unit) = component(block)
fun IEntity.simpleSkybox() = component<SimpleSkybox>()