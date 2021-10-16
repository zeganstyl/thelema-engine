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

package app.thelema.test

import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.g3d.*
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.light.DirectionalLight
import app.thelema.g3d.light.directionalLight
import app.thelema.g3d.mesh.SphereMesh
import app.thelema.gl.GL
import app.thelema.shader.PBRShader
import app.thelema.utils.Color

class PBRShaderTest: Test {
    override val name: String
        get() = "PBR Shader"

    override fun testMain() {
        GL.glClearColor(Color.SKY)

        Entity {
            makeCurrent()
            scene()
            orbitCameraControl()

            entity("light") {
                directionalLight {
                    setDirectionFromPosition(1f, 0f, 1f)
                    intensity = 5f
                }
            }

            entity("object") {
                material {
                    shader = PBRShader {
                        setBaseColorTexture("MetalWalkway012_1K-JPG/MetalWalkway012_1K_Color.jpg")
                        setAlphaTexture("MetalWalkway012_1K-JPG/MetalWalkway012_1K_Opacity.jpg")
                        setNormalTexture("MetalWalkway012_1K-JPG/MetalWalkway012_1K_Normal.jpg")
                        setRoughnessTexture("MetalWalkway012_1K-JPG/MetalWalkway012_1K_Roughness.jpg")
                        setMetallicTexture("MetalWalkway012_1K-JPG/MetalWalkway012_1K_Metalness.jpg")

                        outputNode.cullFaceMode = 0
                        outputNode.alphaMode = Blending.MASK

                        baseColorTextureNode.sRGB = false
                        normalTextureNode.sRGB = false
                        metallicTextureNode.sRGB = false
                        roughnessTextureNode.sRGB = false
                        alphaTextureNode.sRGB = false
                    }
                }
                component<SphereMesh> {
                    setSize(2f)
                    updateMesh()
                }
            }
        }
    }
}