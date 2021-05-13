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
import app.thelema.g3d.light.DirectionalLight
import app.thelema.math.MATH
import kotlin.native.concurrent.ThreadLocal

/** Entity component system */
@ThreadLocal
object ECS: IEntityComponentSystem, ComponentDescriptorList("") {
    val allDescriptors = LinkedHashMap<String, ComponentDescriptor<IEntityComponent>>()

    override fun getDescriptor(typeName: String): ComponentDescriptor<IEntityComponent>? =
        allDescriptors[typeName]

    private val systemsInternal = ArrayList<IEntitySystem>()
    override val systems: List<IEntitySystem>
        get() = systemsInternal

    override fun addSystem(system: IEntitySystem) {
        systemsInternal.add(system)
    }

    override fun setSystem(index: Int, system: IEntitySystem) {
        systemsInternal[index] = system
    }

    override fun removeSystem(system: IEntitySystem) {
        systemsInternal.remove(system)
    }

    override fun removeSystem(index: Int) {
        systemsInternal.removeAt(index)
    }

    override fun entity(name: String, block: IEntity.() -> Unit): IEntity {
        val entity = Entity()
        entity.name = name
        block(entity)
        return entity
    }

    override fun update(entity: IEntity, delta: Float) {
        for (i in systemsInternal.indices) {
            systemsInternal[i].update(entity, delta)
        }
    }

    override fun render(entity: IEntity) {
        for (i in systemsInternal.indices) {
            systemsInternal[i].render(entity)
        }
    }

    fun setupDefaultComponents() {
        descriptor({ app.thelema.g3d.node.TransformNode() }) {
            addAliases(app.thelema.g3d.node.ITransformNode::class)
            vec3("position", { position }) { position.set(it) }
            vec4("rotation", { rotation }) { rotation.set(it) }
            vec3("scale", { scale }) { scale.set(it) }.apply { default = { MATH.One3 } }
            mat4("worldMatrix", { worldMatrix }) { worldMatrix.set(it) }

            descriptor({ app.thelema.g3d.Scene() }) {
                addAliases(app.thelema.g3d.IScene::class)
            }
            descriptor({ app.thelema.g3d.Object3D() }) {
                addAliases(app.thelema.g3d.IObject3D::class)
                ref("armature", { armature }) { armature = it }
            }
            descriptor({ app.thelema.g3d.light.PointLight() }) {}
            descriptor({ DirectionalLight() }) {
                vec3("color", { color }) { color.set(it) }
            }
            descriptor({ app.thelema.g3d.cam.Camera() }) {
                addAliases(app.thelema.g3d.cam.ICamera::class)
            }
        }

        descriptor({ app.thelema.gl.Mesh() }) {
            addAliases(app.thelema.gl.IMesh::class)
            int("primitiveType", { primitiveType }) { primitiveType = it }
            int("verticesCount", { verticesCount }) { verticesCount = it }
            ref("material", { material }) { material = it }

            descriptor({ app.thelema.g3d.mesh.MeshBuilder() }) {
                int("indexType", { indexType }) { indexType = it }
                bool("uvs", { uvs }) { uvs = it }
                bool("normals", { normals }) { normals = it }
                string("positionName", { positionName }) { positionName = it }
                string("uvName", { uvName }) { uvName = it }
                string("normalName", { normalName }) { normalName = it }

                descriptor({ app.thelema.g3d.mesh.BoxMesh() }) {
                    float("xSize", { xSize }) { xSize = it }
                    float("ySize", { ySize }) { ySize = it }
                    float("zSize", { zSize }) { zSize = it }
                }

                descriptor({ app.thelema.g3d.mesh.SphereMesh() }) {
                    float("radius", { radius }) { radius = it }
                    int("hDivisions", { hDivisions }) { hDivisions = it }
                    int("vDivisions", { vDivisions }) { vDivisions = it }
                }

                descriptor({ app.thelema.g3d.mesh.PlaneMesh() }) {
                    float("width", { width }) { width = it }
                    float("height", { height }) { height = it }
                    int("xDivisions", { xDivisions }) { xDivisions = it }
                    int("yDivisions", { yDivisions }) { yDivisions = it }
                }
            }
        }
        descriptor({ app.thelema.g3d.Material() }) {
            addAliases(app.thelema.g3d.IMaterial::class)
            vec4("baseColor", { baseColor }) { baseColor.set(it) }
            float("metallic", { metallic }) { metallic = it }
            float("roughness", { roughness }) { roughness = it }
            float("alphaCutoff", { alphaCutoff }) { alphaCutoff = it }
            string("alphaMode", { alphaMode }) { alphaMode = it }
            int("cullFaceMode", { cullFaceMode }) { cullFaceMode = it }
            int("translucentPriority", { translucentPriority }) { translucentPriority = it }
            bool("depthMask", { depthMask }) { depthMask = it }
        }
        descriptor({ app.thelema.g3d.Armature() }) {
            addAliases(app.thelema.g3d.IArmature::class)
        }
        descriptor { app.thelema.anim.AnimationPlayer() }
        descriptor({ app.thelema.anim.Animation() }) {
            addAliases(app.thelema.anim.IAnimation::class)
        }
        descriptor { app.thelema.anim.AnimationAction() }

        descriptor { app.thelema.phys.PhysicsContext() }

        descriptor({ app.thelema.res.Resource() }) {
            uri("uri", { uri }) { uri = it }

            descriptor { app.thelema.res.Project() }
            descriptor({ app.thelema.res.LoadingLauncher() }) {
                uri("uri", { uri }) { uri = it }
                string("loader", { loader }) { loader = it }
                bool("launchOnInit", { launchOnInit }) { launchOnInit = it }
            }
            descriptor { EntityLoader() }
            descriptor { app.thelema.gltf.GLTF() }
            descriptor { app.thelema.font.BitmapFont() }
        }

        descriptor({ Action() }) {
            addAliases(app.thelema.action.IAction::class)

            descriptor { ActionList() }
            descriptor({ DelayAction() }) {
                float("delay", { delay }) { delay = it }
            }
            descriptor({ MoveToTargetAction() }) {
                float("speed", { speed }) { speed = it }
                ref("target", { target }) { target = it }
            }
            descriptor({ MoveForwardAction() }) {
                float("length", { length }) { length = it }
            }
            descriptor({ RotateYAction() }) {
                float("angleLength", { angleLength }) { angleLength = it }
            }
            descriptor { SwitchAction() }
            descriptor { BiOperation() }
            descriptor { VariableAction() }
        }

        addSystem(DefaultEntitySystem())
    }

    const val Resource = "Resource"
}