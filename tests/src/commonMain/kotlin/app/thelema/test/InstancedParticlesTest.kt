package app.thelema.test

import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.mesh.planeMesh
import app.thelema.g3d.particles.*
import app.thelema.g3d.scene
import app.thelema.img.Texture2D
import app.thelema.math.MATH
import app.thelema.math.Mat3

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

                    shader.apply {
                        load(
                            vertCode = """
attribute vec3 POSITION;
attribute vec2 UV;
attribute vec3 INSTANCE_POSITION;
attribute vec2 INSTANCE_UV_START;
attribute float INSTANCE_LIFE_TIME;
uniform mat4 viewProj;
uniform mat3 view;
uniform vec2 texSize;

varying vec2 uv;
varying float life;

void main() {
    uv = UV;
    uv = UV * texSize + INSTANCE_UV_START;
    life = INSTANCE_LIFE_TIME;
    gl_Position = viewProj * vec4(view * (1.0 + INSTANCE_LIFE_TIME) * POSITION + INSTANCE_POSITION, 1.0);
}""",
                            fragCode = """
varying vec2 uv;
varying float life;
uniform sampler2D tex;
uniform float maxLife;

void main() {
    gl_FragColor = texture2D(tex, uv);
    gl_FragColor.a *= 1.0 - life / maxLife;
}"""
                        )
                    }

                    val frameNode = ParticleTextureRandomFrame().apply {
                        setupFrames(6, 5)
                    }

                    addParticleNodes(
                        MoveParticleNode(),
                        frameNode
                    )

                    val smokeTexture = Texture2D("Smoke30Frames.png")

                    val mat3Tmp = Mat3()

                    shader.bind()
                    shader["tex"] = 0
                    shader.set("texSize", frameNode.sizeU, frameNode.sizeV)
                    shader["maxLife"] = 5f
                    shader.depthMask = false
                    shader.onPrepareShader = { mesh, scene ->
                        smokeTexture.bind(0)
                        shader["viewProj"] = ActiveCamera.viewProjectionMatrix

                        mat3Tmp.set(ActiveCamera.viewMatrix)
                        shader.set("view", mat3Tmp, true)
                    }
                }
            }

            entity("smoke") {
                particleEmitter {
                    this.particleSystem = particleSystem.component()
                }
            }
        }
    }
}