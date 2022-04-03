package app.thelema.test.shader

import app.thelema.anim.AnimationAction
import app.thelema.anim.animationPlayer
import app.thelema.app.APP
import app.thelema.ecs.*
import app.thelema.g3d.SceneInstance
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.light.directionalLight
import app.thelema.g3d.material
import app.thelema.g3d.mesh.planeMesh
import app.thelema.g3d.scene
import app.thelema.g3d.transformNode
import app.thelema.gl.ScreenQuad
import app.thelema.gl.meshInstance
import app.thelema.gltf.GLTFSettings
import app.thelema.gltf.gltf
import app.thelema.res.RES
import app.thelema.shader.ForwardRenderingPipeline
import app.thelema.shader.Shader
import app.thelema.shader.node
import app.thelema.shader.node.OutputNode
import app.thelema.shader.node.PBRNode
import app.thelema.test.Test

class ForwardRenderingPipelineTest: Test {
    override fun testMain() {
        testEntity {
            ActiveCamera {
                enablePreviousMatrix()
            }

            scene().renderingPipeline = component<ForwardRenderingPipeline> {
                motionBlurEnabled = true
                bloomEnabled = true
                vignetteEnabled = false
                fxaaEnabled = true
                godRaysEnabled = false
                bloomThreshold.cutoff = 1.5f
            }

            entity("light") {
                directionalLight {
                    color.set(1f, 1f, 2f)
                    isShadowEnabled = true
                }
                transformNode().setPosition(1f, 1f, 1f)
            }

            entity("plane") {
                material().shader = component<Shader> {
                    val pbr = PBRNode { receiveShadows = true }
                    rootNode = node<OutputNode> { fragColor = pbr.result }
                }
                planeMesh {
                    setSize(100f)
                }
                meshInstance()
            }

            entity("model") {
                component<SceneInstance>().provider = RES.gltf("nightshade/nightshade.gltf") {
                    gltfSettings {
                        setupVelocityShader = true
                        setupDepthRendering = true
                    }
                }.sibling()

                entity("Scene").animationPlayer().setAnimation(
                    AnimationAction {
                        animName = "anim"
                        speed = 1f
                    }
                )
            }
        }
    }
}