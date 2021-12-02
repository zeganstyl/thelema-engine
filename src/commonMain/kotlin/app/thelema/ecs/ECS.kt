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

package app.thelema.ecs

import app.thelema.action.*
import app.thelema.g3d.Blending
import app.thelema.g3d.Material
import app.thelema.g3d.TransformNode
import app.thelema.g3d.particles.*
import app.thelema.gl.*
import app.thelema.gltf.GLTF
import app.thelema.gltf.GLTFSceneInstance
import app.thelema.gltf.IGLTF
import app.thelema.img.*
import app.thelema.input.KeyboardHandler
import app.thelema.input.MouseHandler
import app.thelema.script.BakedKotlinScripts
import app.thelema.script.IKotlinScript
import app.thelema.script.KotlinScript
import app.thelema.shader.Shader
import app.thelema.utils.iterate
import kotlin.native.concurrent.ThreadLocal

/** Entity component system */
@ThreadLocal
object ECS: IEntityComponentSystem, ComponentDescriptorList("ECS") {
    val allDescriptors = LinkedHashMap<String, ComponentDescriptor<IEntityComponent>>()

    private val systemsInternal = ArrayList<IComponentSystem>()
    override val systems: List<IComponentSystem>
        get() = systemsInternal

    private val entitiesInternal = ArrayList<IEntity>()
    override val entities: List<IEntity>
        get() = entitiesInternal

    override fun getDescriptor(typeName: String): ComponentDescriptor<IEntityComponent>? =
        allDescriptors[typeName]

    override var currentEntity: IEntity?
        get() = entities.getOrNull(0)
        set(value) {
            if (value != null) {
                if (entitiesInternal.size == 0) {
                    addEntity(value)
                } else {
                    removeEntity(entitiesInternal[0])
                    addEntity(value)
                }
            } else if (entitiesInternal.size != 0) {
                entitiesInternal.iterate { removeEntity(it) }
                entitiesInternal.clear()
            }
        }

    override fun addEntity(entity: IEntity) {
        entitiesInternal.add(entity)
        systemsInternal.iterate { it.addedScene(entity) }
    }

    override fun removeEntity(entity: IEntity) {
        entitiesInternal.remove(entity)
        systemsInternal.iterate { it.removedScene(entity) }
    }

    fun removeAllEntities() {
        entitiesInternal.forEach { entity -> systemsInternal.forEach { entity.removeEntity(entity) } }
        entitiesInternal.clear()
    }

    override fun addSystem(system: IComponentSystem) {
        systemsInternal.add(system)
    }

    override fun setSystem(index: Int, system: IComponentSystem) {
        systemsInternal[index] = system
    }

    override fun removeSystem(system: IComponentSystem) {
        systemsInternal.remove(system)
    }

    override fun removeSystem(index: Int) {
        systemsInternal.removeAt(index)
    }

    override fun update(delta: Float) {
        systemsInternal.iterate { it.update(delta) }
    }

    override fun render(shaderChannel: String?) {
        systemsInternal.iterate { it.render(shaderChannel) }
    }

    fun setupDefaultComponents() {
        TransformNode.setupTransformNodeComponents()
        Mesh.setupMeshComponents()
        Shader.setupShaderComponents()

        descriptor({ app.thelema.g3d.Material() }) {
            setAliases(app.thelema.g3d.IMaterial::class)
            stringEnum("alphaMode", Blending.items, { alphaMode }) { alphaMode = it }
            int("translucentPriority", { translucentPriority }) { translucentPriority = it }
            refAbs(Material::shader)
        }
        descriptor({ app.thelema.g3d.Armature() }) {
            setAliases(app.thelema.g3d.IArmature::class)
        }
        descriptor { app.thelema.anim.AnimationPlayer() }
        descriptor({ app.thelema.anim.Animation() }) {
            setAliases(app.thelema.anim.IAnimation::class)
        }
        descriptor { app.thelema.anim.AnimationAction() }

        descriptor { app.thelema.phys.PhysicsProperties() }

        descriptor { MainLoop() }

        descriptor({ app.thelema.res.Loader() }) {
            setAliases(app.thelema.res.ILoader::class)
            file("file", { file }) { file = it }

            descriptor { EntityLoader() }
            descriptor({ GLTF() }) {
                setAliases(IGLTF::class)

                bool(GLTF::saveMeshesInCPUMem)
                bool(GLTF::mergeVertexAttributes)
                bool(GLTF::generateShaders)
                bool(GLTF::setupDepthRendering)
                bool(GLTF::setupVelocityShader)
                bool(GLTF::setupGBufferShader)
                bool(GLTF::receiveShadows)
                bool(GLTF::ibl)
                int(GLTF::iblMaxMipLevels)

                descriptor({ GLTFSceneInstance() }) {
                    refAbs("model", { model }) { model = it }
                }
            }
            descriptor { app.thelema.font.BitmapFont() }
            descriptor({ app.thelema.res.Project() }) {
                setAliases(app.thelema.res.IProject::class)
                refAbs("mainScene", { mainScene }) { mainScene = it }
            }
            descriptor { app.thelema.res.ResourceHolder() }
        }

        descriptor { BakedKotlinScripts() }

        descriptor({ KotlinScript() }) {
            setAliases(IKotlinScript::class)
            string(KotlinScript::customMainFunctionName)
        }

        descriptor { SimulationNode() }

        descriptor { KeyboardHandler() }
        descriptor { MouseHandler() }

        descriptor({ Image() }) {
            setAliases(IImage::class)
            int(Image::width)
            int(Image::height)
            int(Image::pixelFormat)
            int(Image::pixelChannelType)
            int(Image::internalFormat)
        }
        descriptor({ Texture2D() }) {
            setAliases(ITexture2D::class)
            ref("image", { image }) { image = it }
            intEnum(
                Texture2D::minFilter,
                linkedMapOf(
                    GL_NEAREST to "Nearest",
                    GL_LINEAR to "Linear",
                    GL_LINEAR_MIPMAP_NEAREST to "Linear Mipmap Nearest",
                    GL_NEAREST_MIPMAP_LINEAR to "Nearest Mipmap Linear",
                    GL_LINEAR_MIPMAP_LINEAR to "Linear Mipmap Linear"
                ),
                "???"
            )
            intEnum(
                Texture2D::magFilter,
                linkedMapOf(GL_NEAREST to "Nearest", GL_LINEAR to "Linear"),
                "???"
            )
            int("sWrap", { sWrap }) { sWrap = it }
            int("tWrap", { tWrap }) { tWrap = it }
            float(Texture2D::anisotropicFilter)
        }
        descriptor { TextureCube() }

        descriptor({ ParticleSystem() }) {
            setAliases(IParticleSystem::class)

            ref(ParticleSystem::mesh)
            string(ParticleSystem::instancePositionName)
            string(ParticleSystem::instanceLifeTimeName)

            descriptor({ ParticleEmitter() }) {
                setAliases(IParticleEmitter::class)

                ref(ParticleEmitter::particleSystem)
                int(ParticleEmitter::maxParticles)
                float(ParticleEmitter::particleEmissionSpeed)
                float(ParticleEmitter::maxParticleLifeTime)
            }

            descriptor({ MoveParticleEffect() }) {
                vec3(MoveParticleEffect::speed)
            }

            descriptor({ ParticleTextureFrameEffect() }) {
                int(ParticleTextureFrameEffect::framesU)
                int(ParticleTextureFrameEffect::framesV)
            }
        }

        Action.setupActionComponents()

        addSystem(DefaultComponentSystem())
    }
}
