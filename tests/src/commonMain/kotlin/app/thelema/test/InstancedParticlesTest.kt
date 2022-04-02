package app.thelema.test

import app.thelema.ecs.component
import app.thelema.g3d.Blending
import app.thelema.g3d.material
import app.thelema.g3d.mesh.planeMesh
import app.thelema.g3d.particles.*
import app.thelema.g3d.transformNode
import app.thelema.gl.meshInstance
import app.thelema.img.Texture2D
import app.thelema.math.TransformDataType
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.shader.node
import app.thelema.shader.node.*

class InstancedParticlesTest: Test {
    override fun testMain() = testEntity {
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
                maxLifeTime = 5f

                component<MoveParticleEffect>()
                val frame = component<ParticleTextureFrameEffect> { setupFrames(6, 5) }
                val shader = Shader()
                val material = material()
                material.shader = shader

                shader.apply {
                    val vertex = VertexNode()
                    vertex.worldTransformType = TransformDataType.None

                    val particleData = ParticleDataNode()

                    val scaling = ParticleScaleNode().apply {
                        scaling = 5f
                        lifeTimePercent = particleData.lifeTimePercent
                        inputPosition = vertex.position
                    }

                    val particlePosition = ParticlePositionNode()
                    particlePosition.vertexPosition = scaling.scaledPosition
                    particlePosition.alwaysRotateToCamera = true

                    val uvNode = UVNode {}

                    val textureFrame = ParticleUVFrameNode().apply {
                        inputUv = uvNode.uv
                        framesU = frame.framesU
                        framesV = frame.framesV
                    }

                    val texture = Texture2DNode {
                        texture = Texture2D("Smoke30Frames.png")
                        uv = textureFrame.resultUv
                    }

                    val particleColor = ParticleColorNode().apply {
                        lifeTimePercent = particleData.lifeTimePercent
                        inputColor = texture.texColor
                        color1 = GLSLVec4Literal(1f, 1f, 1f, 0f)
                    }

                    rootNode = node<OutputNode> {
                        vertPosition = particlePosition.particlePosition
                        fragColor = particleColor.result
                        cullFaceMode = 0
                        alphaMode = Blending.BLEND
                    }

                    depthMask = false

                    build()

                    println(printCode())
                }
            }
        }

        entity("smoke") {
            particleEmitter {
                maxParticles = 100
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
                particleEmissionSpeed = 2f
                this.particleMaterial = particleMaterial.component()
            }
        }
    }
}