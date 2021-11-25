package app.thelema.test

import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.mesh.planeMesh
import app.thelema.g3d.particles.*
import app.thelema.g3d.scene
import app.thelema.g3d.transformNode
import app.thelema.math.MATH

class InstancedParticlesTest: Test {
    override fun testMain() {
        Entity {
            makeCurrent()
            scene()
            orbitCameraControl()

            entity("plane") {
                planeMesh { setSize(10f) }
            }

            val particleSystem = entity("particlesSystem") {
                planeMesh {
                    normal = MATH.Z
                    setSize(1f)
                }
                component<IParticleSystem> {
                    lifeTimesAsAttribute = true

//                    shader.apply {
//                        load(
//                            vertCode = """
//attribute vec3 POSITION;
//attribute vec2 UV;
//attribute vec3 INSTANCE_POSITION;
//attribute vec2 INSTANCE_UV_START;
//attribute float INSTANCE_LIFE_TIME;
//uniform mat4 viewProj;
//uniform mat3 view;
//uniform vec2 texSize;
//
//varying vec2 uv;
//varying float life;
//
//void main() {
//    uv = UV;
//    uv = UV * texSize + INSTANCE_UV_START;
//    life = INSTANCE_LIFE_TIME;
//    gl_Position = viewProj * vec4(view * (1.0 + INSTANCE_LIFE_TIME) * POSITION + INSTANCE_POSITION, 1.0);
//}""",
//                            fragCode = """
//varying vec2 uv;
//varying float life;
//uniform sampler2D tex;
//uniform float maxLife;
//
//void main() {
//    gl_FragColor = texture2D(tex, uv);
//    gl_FragColor.a *= 1.0 - life / maxLife;
//}"""
//                        )
//                    }

//                    shader.apply {
//                        val vertex = addNode(VertexNode())
//
//                        val particleData = addNode(ParticleDataNode())
//                        particleData.maxLifeTime = 5f
//                        particleData.instanceLifeTimeName = instanceLifeTimeName
//
//                        val scaling = addNode(ParticleScaleNode().apply {
//                            scaling = 5f
//                            lifeTimePercent = particleData.lifeTimePercent
//                            inputPosition = vertex.position
//                        })
//
//                        val camera = addNode(CameraDataNode(scaling.scaledPosition))
//                        camera.useInstancePosition = true
//                        camera.alwaysRotateObjectToCamera = true
//
//                        val uvNode = addNode(UVNode().apply {
//                            uvName = "UV"
//                        })
//
//                        val textureFrame = addNode(ParticleUVFrameNode().apply {
//                            inputUv = uvNode.uv
//                            instanceUvStartName = frameNode.instanceUvStartName
//                            frameSizeU = frameNode.sizeU
//                            frameSizeV = frameNode.sizeV
//                        })
//
//                        val texture = addNode(TextureNode(texture = Texture2D("Smoke30Frames.png")))
//                        texture.uv = textureFrame.resultUv
//
//                        val particleColor = addNode(ParticleColorNode().apply {
//                            lifeTimePercent = particleData.lifeTimePercent
//                            inputColor = texture.color
//                            color1 = GLSLVec4Inline(1f, 1f, 1f, 0f)
//                        })
//
//                        addNode(OutputNode(camera.clipSpacePosition, particleColor.result))
//
//                        depthMask = false
//
//                        build()
//
//                        println(printCode())
//                    }
                }
                component<MoveParticleEffect>()
                component<ParticleTextureFrameEffect> {
                    setupFrames(6, 5)
                }
            }

            entity("smoke") {
                particleEmitter {
                    maxParticles = 100
                    maxParticleLifeTime = 5f
                    particleEmissionSpeed = 4f
                    this.particleSystem = particleSystem.component()
                }
            }

            entity("smoke2") {
                transformNode {
                    position.x = 3f
                    requestTransformUpdate()
                }
                particleEmitter {
                    maxParticles = 100
                    maxParticleLifeTime = 5f
                    particleEmissionSpeed = 2f
                    this.particleSystem = particleSystem.component()
                }
            }
        }
    }
}