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
import app.thelema.g3d.terrain.Terrain
import app.thelema.gl.*
import app.thelema.gltf.GLTF
import app.thelema.gltf.GLTFSettings
import app.thelema.gltf.IGLTF
import app.thelema.img.*
import app.thelema.input.KeyboardHandler
import app.thelema.input.MouseHandler
import app.thelema.res.ILoader
import app.thelema.res.IProject
import app.thelema.res.Project
import app.thelema.script.BakedKotlinScripts
import app.thelema.script.IKotlinScript
import app.thelema.script.KotlinScript
import app.thelema.shader.ForwardRenderingPipeline
import app.thelema.shader.IRenderingPipeline
import app.thelema.shader.RenderingPipeline
import app.thelema.shader.Shader
import app.thelema.utils.iterate
import kotlin.native.concurrent.ThreadLocal

/** Entity component system */
@ThreadLocal
object ECS: IEntityComponentSystem, ComponentDescriptorList("ECS") {
    val allDescriptors = LinkedHashMap<String, ComponentDescriptor<IEntityComponent>>()

    private val _systems = ArrayList<IComponentSystem>()
    override val systems: List<IComponentSystem>
        get() = _systems

    private val _entities = ArrayList<IEntity>()
    override val entities: List<IEntity>
        get() = _entities

    override fun getDescriptor(typeName: String): ComponentDescriptor<IEntityComponent>? =
        allDescriptors[typeName]

    override var currentEntity: IEntity? = null
        set(value) {
            if (field != value) {
                val oldValue = field
                if (oldValue != null) _systems.iterate { it.removedScene(oldValue) }
                field = value
                if (value != null && !_entities.contains(oldValue)) _systems.iterate { it.addedScene(value) }
            }
        }

    override fun addEntity(entity: IEntity) {
        _entities.add(entity)
        _systems.iterate { it.addedScene(entity) }
    }

    override fun removeEntity(entity: IEntity) {
        _entities.remove(entity)
        _systems.iterate { it.removedScene(entity) }
    }

    fun removeAllEntities() {
        _entities.iterate { entity -> _systems.iterate { entity.removeEntity(entity) } }
        _entities.clear()
    }

    override fun addSystem(system: IComponentSystem) {
        _systems.add(system)
    }

    override fun setSystem(index: Int, system: IComponentSystem) {
        _systems[index] = system
    }

    override fun removeSystem(system: IComponentSystem) {
        _systems.remove(system)
    }

    override fun removeSystem(index: Int) {
        _systems.removeAt(index)
    }

    override fun update(delta: Float) {
        _systems.iterate { it.update(delta) }
    }

    override fun render(shaderChannel: String?) {
        _systems.iterate { it.render(shaderChannel) }
    }

    fun setupDefaultComponents() {
        TransformNode.setupTransformNodeComponents()
        Mesh.setupMeshComponents()
        Shader.setupShaderComponents()

        descriptor({ Material() }) {
            setAliases(app.thelema.g3d.IMaterial::class)
            stringEnum(Material::alphaMode, Blending.items)
            int(Material::translucentPriority)
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

        descriptorI<ILoader>({ app.thelema.res.Loader() }) {
            file(ILoader::file)

            descriptor { EntityLoader() }
            descriptorI<IGLTF>({ GLTF() }) {
                bool(IGLTF::overrideAssets)

                descriptor({ GLTFSettings() }) {
                    bool(GLTFSettings::saveMeshesInCPUMem)
                    bool(GLTFSettings::mergeVertexAttributes)
                    bool(GLTFSettings::generateShaders)
                    bool(GLTFSettings::setupDepthRendering)
                    bool(GLTFSettings::setupVelocityShader)
                    bool(GLTFSettings::setupGBufferShader)
                    bool(GLTFSettings::receiveShadows)
                    bool(GLTFSettings::ibl)
                    int(GLTFSettings::iblMaxMipLevels)
                }
            }
            descriptor { app.thelema.font.BitmapFont() }
            descriptorI<IProject>({ Project() }) {
                refAbs(IProject::mainScene)
                string(IProject::appPackage)
            }
            descriptor { app.thelema.res.ResourceHolder() }
        }

        descriptor { BakedKotlinScripts() }

        descriptorI<IKotlinScript>({ KotlinScript() }) {
            string(IKotlinScript::functionName)
        }

        descriptor { KeyboardHandler() }
        descriptor { MouseHandler() }

        descriptorI<IImage>(::Image) {
            int(IImage::width)
            int(IImage::height)
            int(IImage::pixelFormat)
            int(IImage::pixelChannelType)
            int(IImage::internalFormat)
        }
        descriptorI<ITexture2D>(::Texture2D) {
            ref(ITexture2D::image)
            intEnum(
                ITexture2D::minFilter,
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
                ITexture2D::magFilter,
                linkedMapOf(GL_NEAREST to "Nearest", GL_LINEAR to "Linear"),
                "???"
            )
            int(ITexture2D::sWrap)
            int(ITexture2D::tWrap)
            float(ITexture2D::anisotropicFilter)
        }
        descriptor { TextureCube() }

        descriptorI<IParticleSystem>({ ParticleSystem() }) {
            int(IParticleSystem::translucencyPriority, 1)
            bool(IParticleSystem::isVisible, true)
            string(IParticleSystem::alphaMode, Blending.BLEND)

            descriptorI<IParticleMaterial>({ ParticleMaterial() }) {
                string(IParticleMaterial::instancePositionName, "INSTANCE_POSITION")
                string(IParticleMaterial::instanceLifeTimeName, "INSTANCE_LIFE_TIME")
                bool(IParticleMaterial::lifeTimesAsAttribute, true)
                ref(IParticleMaterial::mesh)
            }

            descriptorI<IParticleEmitter>({ ParticleEmitter() }) {
                ref(IParticleEmitter::particleMaterial)
                int(IParticleEmitter::maxParticles)
                float(IParticleEmitter::particleEmissionSpeed)
                float(IParticleEmitter::maxParticleLifeTime)
            }

            descriptor({ MoveParticleEffect() }) {
                vec3(MoveParticleEffect::speed)
            }

            descriptor({ ParticleTextureFrameEffect() }) {
                int(ParticleTextureFrameEffect::framesU)
                int(ParticleTextureFrameEffect::framesV)
            }
        }

        descriptorI<IRenderingPipeline>({ RenderingPipeline() }) {
            descriptor({ ForwardRenderingPipeline() }) {
                bool(ForwardRenderingPipeline::fxaaEnabled)
                bool(ForwardRenderingPipeline::vignetteEnabled)
                bool(ForwardRenderingPipeline::bloomEnabled)
                bool(ForwardRenderingPipeline::godRaysEnabled)
                bool(ForwardRenderingPipeline::motionBlurEnabled)
            }
        }

        descriptor({ Terrain() }) {
            refAbs(Terrain::material)
            int(Terrain::tileDivisions, 10)
            float(Terrain::minTileSize, 10f)
            int(Terrain::levelsNum, 10)
            bool(Terrain::useCameraFrustum, true)
            string(Terrain::tilePositionScaleName, "tilePositionScale")
            string(Terrain::vertexPositionName, "POSITION")
            float(Terrain::minY, 0f)
            float(Terrain::maxY, 100f)
        }

        Action.setupActionComponents()

        addSystem(DefaultComponentSystem())
    }
}
