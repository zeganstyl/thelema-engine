package app.thelema.test

import app.thelema.ecs.mainEntity
import app.thelema.ecs.mainLoop
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.material
import app.thelema.g3d.mesh.planeMesh
import app.thelema.g3d.particles.Particle
import app.thelema.gl.*
import app.thelema.img.Texture2D
import app.thelema.math.*
import app.thelema.shader.IShader
import app.thelema.shader.Shader
import app.thelema.shader.ShaderListener
import kotlin.random.Random

class InstancedParticlesBaseTest: Test {
    override fun testMain() = mainEntity {
        val smokeTexture = Texture2D("Smoke30Frames.png")
        val framesU = 6 // 6 frames by horizontal
        val framesV = 5 // 5 frames by vertical
        val smokeSizeU = 1f / framesU
        val smokeSizeV = 1f / framesV
        val particleLifeTime = 5f
        val maxParticles = 100

        val vec3Temp2 = Vec3()
        val mat3Tmp = Mat3()

        orbitCameraControl()

        entity("plane") {
            planeMesh { setSize(10f) }
            meshInstance()
        }

        val particle = entity("particle").planeMesh {
            normal = MATH.Z
            setSize(1f)
        }

        val particles = entity("particles") {
            instancedMesh {
                mesh = particle.mesh
                instancesCount = maxParticles
                addVertexBuffer(maxParticles, Particle.POSITION_LIFE, Particle.UV_START)
            }
            material {
                shader = Shader(
                    vertCode = """
in vec3 POSITION;
in vec2 TEXCOORD_0;
in vec4 PARTICLE_POSITION;
in vec2 PARTICLE_UV_START;
uniform mat4 viewProj;
uniform mat3 view;
uniform vec2 texSize;

out vec2 uv;
out float life;

void main() {
    uv = TEXCOORD_0 * texSize + PARTICLE_UV_START;
    life = PARTICLE_POSITION.w;
    gl_Position = viewProj * vec4(view * (1.0 + life) * POSITION + PARTICLE_POSITION.xyz, 1.0);
}""",
                    fragCode = """
in vec2 uv;
in float life;
out vec4 FragColor;
uniform sampler2D tex;
uniform float maxLife;

void main() {
    FragColor = texture(tex, uv);
    FragColor.a *= 1.0 - life / maxLife;
}""")

                shader?.listener = object : ShaderListener {
                    override fun loaded(shader: IShader) {
                        shader.bind()
                        shader["tex"] = 0
                        shader.set("texSize", smokeSizeU, smokeSizeV)
                        shader["maxLife"] = particleLifeTime
                        shader.depthMask = false
                    }

                    override fun bind(shader: IShader) {
                        smokeTexture.bind(0)
                        shader["viewProj"] = ActiveCamera.viewProjectionMatrix

                        mat3Tmp.set(ActiveCamera.viewMatrix).transpose()
                        shader["view"] = mat3Tmp
                    }
                }
            }
        }

        val particleInstances = particles.instancedMesh()

        val particlesPositions = particleInstances.getAccessor(Particle.POSITION_LIFE)
        val particlesStartUvs = particleInstances.getAccessor(Particle.UV_START)

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

                particlesPositions.prepare()
                particlesStartUvs.prepare()

                for (i in visibleParticles.indices) {
                    val index = visibleParticles[i]
                    particlesPositions.setVec3(positions[index])
                    particlesPositions.setFloat(12, lifeTimes[index])
                    particlesStartUvs.setVec2(uvs[index])

                    particlesPositions.nextVertex()
                    particlesStartUvs.nextVertex()
                }

                particleInstances.instancesCount = visibleParticles.size
            }
        }
    }
}