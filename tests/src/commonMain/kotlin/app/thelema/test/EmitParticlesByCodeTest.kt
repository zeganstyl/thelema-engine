package app.thelema.test

import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.ecs.mainLoopOnUpdate
import app.thelema.ecs.sibling
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.material
import app.thelema.g3d.mesh.planeMesh
import app.thelema.g3d.particles.*
import app.thelema.g3d.scene
import app.thelema.img.Texture2D
import app.thelema.math.MATH
import app.thelema.shader.Shader
import app.thelema.shader.node.*
import kotlin.random.Random

class EmitParticlesByCodeTest: Test {
    override fun testMain() {
        Entity {
            makeCurrent()
            scene()
            orbitCameraControl()

            entity("plane") {
                planeMesh { setSize(10f) }
            }

            val particleMaterial = entity("particleMaterial") {
                planeMesh {
                    normal = MATH.Z
                    setSize(1f)
                    mesh.isVisible = false
                }
                particleMaterial {
                    lifeTimesAsAttribute = true

                    component<MoveParticleEffect>()
                    val shader = Shader()
                    val material = material()
                    material.shader = shader

                    shader.apply {
                        val vertex = VertexNode()

                        val particleData = ParticleDataNode()
                        particleData.maxLifeTime = 5f
                        particleData.instanceLifeTimeName = instanceLifeTimeName

                        val camera = CameraDataNode().apply {
                            vertexPosition = vertex.position
                            useInstancePosition = true
                            alwaysRotateObjectToCamera = true
                        }

                        val texture = Texture2DNode {
                            texture = Texture2D("thelema-logo-256.png")
                        }

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
                    }
                }
            }

            entity("emitter") {
                val emitter = particleEmitter {
                    maxParticles = 100
                    maxParticleLifeTime = 5f
                    particleEmissionSpeed = 0f
                    this.particleMaterial = particleMaterial.component()
                }

                var delay = 0f

                mainLoopOnUpdate {
                    if (delay > 0f) {
                        delay -= it
                    }
                    if (delay <= 0f) {
                        delay = 1f

                        // emit particles by code
                        val xzScale = 5f
                        emitter.emitParticle(
                            (Random.nextFloat() - 0.5f) * xzScale,
                            Random.nextFloat() + 0.5f,
                            (Random.nextFloat() - 0.5f) * xzScale
                        )
                    }
                }
            }
        }
    }
}