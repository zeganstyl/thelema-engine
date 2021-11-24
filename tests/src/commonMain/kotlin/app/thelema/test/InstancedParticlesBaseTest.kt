package app.thelema.test

import app.thelema.ecs.Entity
import app.thelema.ecs.mainLoop
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.material
import app.thelema.g3d.mesh.planeMesh
import app.thelema.g3d.scene
import app.thelema.gl.mesh
import app.thelema.img.Texture2D
import app.thelema.math.*
import app.thelema.shader.Shader
import kotlin.random.Random

class InstancedParticlesBaseTest: Test {
    override fun testMain() {
        val particlesShader = Shader(
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
}""")

        val vec3Temp2 = Vec3()
        val mat3Tmp = Mat3()

        Entity {
            makeCurrent()
            scene()
            orbitCameraControl()

            entity("plane") {
                planeMesh { setSize(10f) }
            }

            val particles = entity("particles") {
                planeMesh {
                    normal = MATH.Z
                    setSize(1f)
                }
                material {
                    shader = particlesShader
                }
            }

            val particlesMesh = particles.mesh()

            val maxParticles = 1000
            particlesMesh.instancesCountToRender = 0
            particlesMesh.addVertexBuffer {
                addAttribute(3, "INSTANCE_POSITION")
                addAttribute(1, "INSTANCE_LIFE_TIME")
                addAttribute(2, "INSTANCE_UV_START")
                initVertexBuffer(maxParticles)

                setDivisor()

                requestBufferUploading()
            }

            val particlesLifeTimes = particlesMesh.getAttribute("INSTANCE_LIFE_TIME")
            val particlesPositions = particlesMesh.getAttribute("INSTANCE_POSITION")
            val particlesStartUvs = particlesMesh.getAttribute("INSTANCE_UV_START")

            val particleLifeTime = 5f

            val smokeTexture = Texture2D("Smoke30Frames.png")
            val framesU = 6 // 6 frames by horizontal
            val framesV = 5 // 5 frames by vertical
            val smokeSizeU = 1f / framesU
            val smokeSizeV = 1f / framesV

            particlesShader.bind()
            particlesShader["tex"] = 0
            particlesShader.set("texSize", smokeSizeU, smokeSizeV)
            particlesShader["maxLife"] = particleLifeTime
            particlesShader.depthMask = false
            particlesShader.onPrepareShader = { mesh, scene ->
                smokeTexture.bind(0)
                particlesShader["viewProj"] = ActiveCamera.viewProjectionMatrix

                mat3Tmp.set(ActiveCamera.viewMatrix)
                particlesShader.set("view", mat3Tmp, true)
            }

            val startPoint = Vec3(0f, 0.5f, 0f)
            val lifeTimes = Array(maxParticles) { Float.MAX_VALUE }
            val positions = Array(maxParticles) { Vec3(0f, 0f, 0f) }
            val uvs = Array(maxParticles) {
                Vec2(
                    smokeSizeU * Random.nextInt(framesU),
                    smokeSizeV * Random.nextInt(framesV)
                )
            }
            val forces = Array(maxParticles) { Vec3(0f, 1f, 0f) }
            val visibleParticles = ArrayList<Int>(maxParticles)
            //val order = Array(maxParticles) { it }

            val particlesEmissionSpeed = 10f
            val particlesEmissionSpeedInv = 1 / particlesEmissionSpeed
            var emitParticleDelay = 0f

            mainLoop {
                onUpdate { delta ->
                    particlesLifeTimes.prepare()
                    particlesPositions.prepare()
                    particlesStartUvs.prepare()

                    if (emitParticleDelay > 0f) {
                        emitParticleDelay -= delta
                    } else if (emitParticleDelay < 0f) {
                        emitParticleDelay = 0f
                    }

                    visibleParticles.clear()

                    for (i in 0 until maxParticles) {
                        val time = lifeTimes[i]
                        if (time >= particleLifeTime && emitParticleDelay <= delta) {
                            emitParticleDelay += particlesEmissionSpeedInv
                            positions[i].set(vec3Temp2.set(forces[i]).scl(emitParticleDelay * 2f)).add(startPoint)
                            lifeTimes[i] = emitParticleDelay
                        } else if (time < particleLifeTime) {
                            visibleParticles.add(i)
                            positions[i].add(vec3Temp2.set(forces[i]).scl(delta * 2f))
                            lifeTimes[i] += delta
                        }
                    }

//                    val camPos = ActiveCamera.viewMatrix.getTranslation(tmpVec3)
//                    visibleParticles.sortBy { positions[it].dst2(camPos) }

                    for (i in visibleParticles.indices) {
                        val index = visibleParticles[i]
                        particlesLifeTimes.setFloat(lifeTimes[index])
                        particlesPositions.setVec3(positions[index])
                        particlesStartUvs.setVec2(uvs[index])

                        particlesLifeTimes.nextVertex()
                        particlesPositions.nextVertex()
                        particlesStartUvs.nextVertex()
                    }

                    particlesMesh.instancesCountToRender = visibleParticles.size
                }
            }
        }
    }
}