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
import app.thelema.audio.Sound3D
import app.thelema.g3d.Blending
import app.thelema.g3d.Material
import app.thelema.g3d.TransformNode
import app.thelema.g3d.particles.*
import app.thelema.g3d.terrain.Terrain
import app.thelema.g3d.terrain.TerrainTileMesh
import app.thelema.gl.*
import app.thelema.gltf.GLTF
import app.thelema.gltf.GLTFSettings
import app.thelema.gltf.IGLTF
import app.thelema.img.*
import app.thelema.input.KeyboardHandler
import app.thelema.input.MouseHandler
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

        descriptorI<app.thelema.res.ILoader>({ app.thelema.res.Loader() }) {
            file("file", { file }) { file = it }

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

        descriptor { SimulationNode() }

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
            ref(IParticleSystem::mesh)
            string(IParticleSystem::instancePositionName, "INSTANCE_POSITION")
            string(IParticleSystem::instanceLifeTimeName, "INSTANCE_LIFE_TIME")
            bool(IParticleSystem::lifeTimesAsAttribute, false)

            descriptorI<IParticleEmitter>({ ParticleEmitter() }) {
                ref(IParticleEmitter::particleSystem)
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

        descriptor({ Sound3D() }) {
            refAbs(Sound3D::loader)
            float(Sound3D::volume, 1f)
            float(Sound3D::pitch, 1f)
            bool(Sound3D::isLooped, false)
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
