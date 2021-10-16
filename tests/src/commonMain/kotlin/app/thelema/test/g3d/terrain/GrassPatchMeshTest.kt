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
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.g3d.terrain.GrassPatchMesh
import app.thelema.gl.GL
import app.thelema.gl.GL_LINEAR
import app.thelema.gl.GL_LINEAR_MIPMAP_LINEAR
import app.thelema.img.Texture2D
import app.thelema.math.Vec3
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test
import kotlin.random.Random

class GrassPatchMeshTest: Test {
    override val name: String
        get() = "Grass patch test"

    override fun testMain() {
        // https://developer.download.nvidia.com/books/HTML/gpugems/gpugems_ch07.html
        // get textures: https://opengameart.org/content/grass-pack-02

        val grassTexture = Texture2D("terrain/grass-diffuse.png")
        val treeTexture = Texture2D("terrain/tree-diffuse.png")

        val plantShader = SimpleShader3D {
            renderAttributeName = ""
            colorTexture = grassTexture
        }

        val groundShader = SimpleShader3D {
            renderAttributeName = ""
            colorTexture = Texture2D("terrain/Ground013_2K_Color.jpg")
        }

        val grassPatch = GrassPatchMesh {
            width = 0.5f
            height = 0.5f
            polygonsNum = 3
        }

        val points = ArrayList<Vec3>()
        for (i in 0 until 10) {
            points.add(Vec3(Random.nextFloat() * 2f - 1f, 0f, Random.nextFloat() * 2f - 1f))
        }

        val grassPatchLarge = GrassPatchMesh {
            width = 0.5f
            height = 0.5f
            polygonsNum = 3
            this.points = points
        }

        val treePatch = GrassPatchMesh {
            width = 3f
            height = 3f
            polygonsNum = 2
            this.points = listOf(Vec3(0f, 0f, 0f))
        }

        points.forEach { it.add(0.1f, 0f, 0.1f) }

        val treePatchLarge = GrassPatchMesh {
            width = 3f
            height = 3f
            polygonsNum = 2
            this.points = points
        }

        val plane = PlaneMesh { setSize(10f) }

        val control = OrbitCameraControl()

        APP.onRender = {
            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            plane.mesh.render(groundShader)

            GL.isCullFaceEnabled = false

            plantShader.colorTexture = grassTexture
            grassPatch.render(plantShader)
            grassPatchLarge.render(plantShader)

            plantShader.colorTexture = treeTexture
            treePatch.render(plantShader)
            treePatchLarge.render(plantShader)
        }
    }
}