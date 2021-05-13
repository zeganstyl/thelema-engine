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

import app.thelema.app.APP
import app.thelema.ecs.ECS
import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.g3d.Blending
import app.thelema.g3d.Material
import app.thelema.g3d.Object3D
import app.thelema.g3d.Scene
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.light.DirectionalLight
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.g3d.mesh.SphereMesh
import app.thelema.g3d.node.TransformNode
import app.thelema.gl.GL
import app.thelema.gl.GL_LINEAR
import app.thelema.gl.GL_LINEAR_MIPMAP_LINEAR
import app.thelema.img.Texture2D
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.math.Vec4
import app.thelema.shader.ComplexPBRShader
import app.thelema.shader.SimpleShader3D
import app.thelema.shader.node.GLSLType
import app.thelema.shader.node.OperationNode
import app.thelema.utils.Color

class ComplexPBRShaderTest: Test {
    override val name: String
        get() = "PBR Shader"

    override fun testMain() {
        ActiveCamera {
            lookAt(Vec3(3f, 3f, 3f), Vec3(0f, 0f, 0f))
            updateCamera()
        }

        Entity("scene") {
            component<Scene>()

            entity("light") {
                component<TransformNode> {
                    rotation.setQuaternionByAxis(1f, 0.5f, 0f, 2f)
                }
                component<DirectionalLight> {
                    lightIntensity = 5f
                }
            }

            entity("object") {
                component<Object3D>()
                component<Material> {
                    shader = ComplexPBRShader {
//                        val posUvOp = addNode(OperationNode(arrayListOf(vertexNode.position), "vec2(arg1.x * 0.2, arg1.z * 0.2)", GLSLType.Vec2))
//
//                        colorTextureNode.uv = posUvOp.result
//                        normalTextureNode.uv = posUvOp.result
//                        normalMapNode.uv = posUvOp.result
//                        metallicRoughnessTextureNode.uv = posUvOp.result

                        outputNode.cullFaceMode = 0
                        outputNode.alphaMode = Blending.MASK

                        setColorTexture(Texture2D().load("Chainmail004_1K_Color.png", minFilter = GL_LINEAR_MIPMAP_LINEAR, magFilter = GL_LINEAR))
                        setNormalTexture(Texture2D().load("Chainmail004_1K_Normal.png", minFilter = GL_LINEAR_MIPMAP_LINEAR, magFilter = GL_LINEAR))
                        setMetallicRoughnessTexture(Texture2D().load("Chainmail004_1K_ORM.png", minFilter = GL_LINEAR_MIPMAP_LINEAR, magFilter = GL_LINEAR))
                    }
                }
                component<SphereMesh> {
                    setSize(2f)
                    updateMesh()
                }
            }

            GL.glClearColor(Color.SKY)

            APP.onUpdate = { ECS.update(this, it) }
            APP.onRender = {
                ECS.render(this)
            }
        }
    }
}