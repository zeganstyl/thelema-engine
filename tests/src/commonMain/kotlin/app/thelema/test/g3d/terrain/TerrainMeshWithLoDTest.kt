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
import app.thelema.g3d.mesh.FrustumMesh
import app.thelema.g3d.terrain.Terrain
import app.thelema.g3d.terrain.TerrainLevel
import app.thelema.g3d.terrain.TerrainListener
import app.thelema.gl.*
import app.thelema.img.Texture2D
import app.thelema.input.IKeyListener
import app.thelema.input.KB
import app.thelema.input.KEY
import app.thelema.math.Frustum
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.SimpleMeshShader
import app.thelema.test.Test
import app.thelema.utils.Color

class TerrainMeshWithLoDTest: Test {
    override val name: String
        get() = "Terrain mesh with LoD"

    override fun testMain() {
        val tilePosScaleName = "tilePosScale"

        val terrainShader = Shader(
            vertCode = """
attribute vec3 POSITION;
varying vec2 uv;
uniform mat4 viewProj;
uniform sampler2D heightMap;
uniform vec3 $tilePosScaleName;

void main() {
    vec3 pos = POSITION;
    pos.x *= tilePosScale.z;
    pos.z *= tilePosScale.z;
    pos.x += tilePosScale.x;
    pos.z += tilePosScale.y;
    uv = pos.xz;
    pos.y = texture(heightMap, uv * 0.001 + vec2(0.5)).r * 100.0;
    gl_Position = viewProj * vec4(pos, 1.0);
}""",
            fragCode = """
varying vec2 uv;
uniform sampler2D heightMap;

void main() {
    gl_FragColor = texture(heightMap, uv * 0.001 + vec2(0.5));
}"""
        )
        terrainShader["heightMap"] = 0

        val simpleMeshShader = SimpleMeshShader(colorName = "color")

        val terrain = Terrain(10f, 25, 8, vertexPositionName = "POSITION")
        terrain.maxY = 100f

        terrain.listeners.add(object : TerrainListener {
            override fun beforeTileRender(level: TerrainLevel, tileX: Float, tileZ: Float) {
                terrainShader.set(tilePosScaleName, tileX, tileZ, level.tileSize)
            }
        })

        val sourcePlaneIndices = terrain.plane.mesh.indices!!
        val wireframePlaneIndices = sourcePlaneIndices.trianglesToWireframe()
        val sourceIndexBuffers = terrain.frameIndexBuffers.map { it.bytes }
        val wireframeIndexBuffers = terrain.frameIndexBuffers.map { it.trianglesToWireframe().bytes }

        ActiveCamera {
            lookAt(Vec3(0f, 200f, 0.001f), MATH.Zero3)
            near = 0.1f
            far = 5000f
            updateCamera()
        }

        val cameraPosition = Vec3()
        cameraPosition.set(ActiveCamera.eye)
        var updateCameraPositionOnSpacePress = false

        val frustum = Frustum(ActiveCamera.inverseViewProjectionMatrix)
        terrain.frustum = frustum

        val frustumMesh = FrustumMesh(frustum)

        KB.addListener(object : IKeyListener {
            override fun keyDown(keycode: Int) {
                when (keycode) {
                    KEY.SPACE -> {
                        // update frustum
                        cameraPosition.set(ActiveCamera.eye)
                        frustum.setFromMatrix(ActiveCamera.inverseViewProjectionMatrix)
                        frustumMesh.frustumPoints = frustum.points
                        frustumMesh.updateMesh()
                    }
                    KEY.NUM_1 -> {
                        // switch to normal mode
                        terrain.plane.mesh.primitiveType = GL_TRIANGLES
                        terrain.plane.mesh.indices = sourcePlaneIndices
                        terrain.frameMesh.primitiveType = GL_TRIANGLES
                        for (i in terrain.frameIndexBuffers.indices) {
                            val buffer = terrain.frameIndexBuffers[i]
                            buffer.bytes = sourceIndexBuffers[i]
                            buffer.uploadBufferToGpu()
                        }
                    }
                    KEY.NUM_2 -> {
                        // switch to wireframe mode
                        terrain.plane.mesh.primitiveType = GL_LINES
                        terrain.plane.mesh.indices = wireframePlaneIndices
                        terrain.frameMesh.primitiveType = GL_LINES
                        for (i in terrain.frameIndexBuffers.indices) {
                            val buffer = terrain.frameIndexBuffers[i]
                            buffer.bytes = wireframeIndexBuffers[i]
                            buffer.uploadBufferToGpu()
                        }
                    }
                    KEY.NUM_3 -> updateCameraPositionOnSpacePress = false
                    KEY.NUM_4 -> updateCameraPositionOnSpacePress = true
                }
            }
        })

        val control = OrbitCameraControl()

        val heightMap = Texture2D("terrain/heightmap.png")

        APP.onRender = {
            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            if (updateCameraPositionOnSpacePress) {
                // render frustum only if camera position updates on SPACE press
                simpleMeshShader.bind()
                simpleMeshShader["color"] = Color.CHARTREUSE
                simpleMeshShader[simpleMeshShader.viewProjName] = ActiveCamera.viewProjectionMatrix
                frustumMesh.render(simpleMeshShader)
            } else {
                cameraPosition.set(ActiveCamera.eye)
                frustum.setFromMatrix(ActiveCamera.inverseViewProjectionMatrix)
            }

            terrain.update(cameraPosition)

            terrainShader.bind()
            terrainShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            heightMap.bind(0)
            terrain.render(terrainShader)
        }
    }
}
