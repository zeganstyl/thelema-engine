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

package app.thelema.test.g3d.terrain

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.PlaneMeshBuilder
import app.thelema.g3d.terrain.GrassPatchMeshBuilder
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.img.Texture2D
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.SimpleMeshShader
import app.thelema.test.Test
import kotlin.random.Random

class GrassPatchMeshBuilderTest: Test {
    override val name: String
        get() = "Grass patch test"

    override fun testMain() {
        // https://developer.download.nvidia.com/books/HTML/gpugems/gpugems_ch07.html
        // get textures: https://opengameart.org/content/grass-pack-02

        val simpleMeshShader = SimpleMeshShader()

        val grassShader = Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec2 UV;

uniform mat4 viewProj;
uniform vec3 pos;
varying vec2 uv;

void main() {
    uv = UV;
    gl_Position = viewProj * vec4(POSITION + pos, 1.0);
}""",
            fragCode = """
uniform sampler2D tex;
varying vec2 uv;

void main() {
    gl_FragColor = texture2D(tex, uv);
    if (gl_FragColor.a < 0.5) discard;
}"""
        )
        grassShader.bind()
        grassShader["tex"] = 0

        val grassTexture = Texture2D()
        grassTexture.load("terrain/grass-diffuse.png")

        val treeTexture = Texture2D()
        treeTexture.load("terrain/tree-diffuse.png")

        val builder = GrassPatchMeshBuilder()
        builder.positionName = "POSITION"

        builder.width = 0.5f
        builder.height = 0.5f
        builder.polygonsNum = 3
        val grassPatch = builder.build()

        val singlePoint = listOf(Vec3(0f, 0f, 0f))
        val points = ArrayList<Vec3>()
        for (i in 0 until 10) {
            points.add(Vec3(Random.nextFloat() * 2f - 1f, 0f, Random.nextFloat() * 2f - 1f))
        }

        builder.points = points
        val grassPatchLarge = builder.build()

        builder.width = 3f
        builder.height = 3f
        builder.polygonsNum = 2
        builder.points = singlePoint
        val treePatch = builder.build()

        builder.points = points
        val treePatchLarge = builder.build()

        val plane = PlaneMeshBuilder().apply {
            positionName = simpleMeshShader.positionName
            width = 10f
            height = 10f
        }.build()

        ActiveCamera {
            near = 0.1f
            far = 100f
        }

        val control = OrbitCameraControl()
        control.listenToMouse()

        GL.setSimpleAlphaBlending()
        GL.isBlendingEnabled = true
        GL.isDepthTestEnabled = true
        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            simpleMeshShader.bind()
            simpleMeshShader[simpleMeshShader.viewProjName] = ActiveCamera.viewProjectionMatrix
            plane.render(simpleMeshShader)

            GL.isCullFaceEnabled = false
            grassShader.bind()
            grassShader["viewProj"] = ActiveCamera.viewProjectionMatrix

            grassShader.set("pos", -0.5f, 0f, 0f)
            grassTexture.bind(0)
            grassPatch.render(grassShader)

            grassShader.set("pos", 0f, 0f, -2f)
            grassTexture.bind(0)
            grassPatchLarge.render(grassShader)

            grassShader.set("pos", 0.5f, 0f, 0f)
            treeTexture.bind(0)
            treePatch.render(grassShader)

            grassShader.set("pos", 0f, 0f, 2f)
            treeTexture.bind(0)
            treePatchLarge.render(grassShader)
        }
    }
}