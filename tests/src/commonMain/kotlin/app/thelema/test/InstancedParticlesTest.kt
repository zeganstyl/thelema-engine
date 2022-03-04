package app.thelema.test

import app.thelema.ecs.component
import app.thelema.ecs.mainEntity
import app.thelema.ecs.sibling
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.material
import app.thelema.g3d.mesh.planeMesh
import app.thelema.g3d.particles.*
import app.thelema.g3d.transformNode
import app.thelema.gl.meshInstance
import app.thelema.img.Texture2D
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.shader.node.*

class InstancedParticlesTest: Test {
    override fun testMain() {
        mainEntity {
            orbitCameraControl()

            particleSystem()

            entity("plane") {
                planeMesh { setSize(10f) }
                meshInstance()
            }

            val particleMaterial = entity("particleMaterial") {
                planeMesh {
                    normal = Vec3(0f, 0f, 1f)
                    setSize(1f)
                }
                particleMaterial {
                    lifeTimesAsAttribute = true

                    component<MoveParticleEffect>()
                    val frame = component<ParticleTextureFrameEffect> { setupFrames(6, 5) }
                    val shader = Shader()
                    val material = material()
                    material.shader = shader

                    shader.apply {
                        val vertex = VertexNode()

                        val particleData = ParticleDataNode()
                        particleData.maxLifeTime = 5f

                        val scaling = ParticleScaleNode().apply {
                            scaling = 5f
                            lifeTimePercent = particleData.lifeTimePercent
                            inputPosition = vertex.position
                        }

                        val camera = CameraDataNode().apply {
                            vertexPosition = scaling.scaledPosition
                            useInstancePosition = true
                            alwaysRotateObjectToCamera = true
                        }

                        val uvNode = UVNode {
                            uvName = "TEXCOORD_0"
                        }

                        val textureFrame = ParticleUVFrameNode().apply {
                            inputUv = uvNode.uv
                            framesU = frame.framesU
                            framesV = frame.framesV
                        }

                        val texture = Texture2DNode {
                            texture = Texture2D("Smoke30Frames.png")
                        }
                        texture.uv = textureFrame.resultUv

                        val particleColor = ParticleColorNode().apply {
                            lifeTimePercent = particleData.lifeTimePercent
                            inputColor = texture.texColor
                            color1 = GLSLVec4Literal(1f, 1f, 1f, 0f)
                        }

                        val output = component<OutputNode> {
                            vertPosition = camera.clipSpacePosition
                            fragColor = particleColor.result
                        }
                        rootNode = output.sibling()

                        depthMask = false

                        build()

                        println(printCode())
                    }
                }
            }

            entity("smoke") {
                particleEmitter {
                    maxParticles = 100
                    maxParticleLifeTime = 5f
                    particleEmissionSpeed = 4f
                    this.particleMaterial = particleMaterial.component()
                }
            }

            entity("smoke2") {
                transformNode {
                    setPosition(3f, 0f, 0f)
                    requestTransformUpdate()
                }
                particleEmitter {
                    maxParticles = 100
                    maxParticleLifeTime = 5f
                    particleEmissionSpeed = 2f
                    this.particleMaterial = particleMaterial.component()
                }
            }
        }
    }
}