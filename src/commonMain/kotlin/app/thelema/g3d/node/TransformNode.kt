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

package app.thelema.g3d.node

import app.thelema.ecs.ECS
import app.thelema.ecs.IEntity
import app.thelema.g3d.IObject3D
import app.thelema.g3d.Object3D
import app.thelema.g3d.Scene
import app.thelema.g3d.cam.Camera
import app.thelema.g3d.cam.ICamera
import app.thelema.g3d.light.DirectionalLight
import app.thelema.g3d.light.PointLight
import app.thelema.math.MATH

/** Translation (position), rotation, scale node (TRS). It is default node.
 *
 * @author zeganstyl */
class TransformNode(
    override var transformData: ITransformData = TRSTransformData()
): ITransformNode {
    override var entityOrNull: IEntity? = null

    override val componentName: String
        get() = ITransformNode.Name

    override var isTransformUpdateRequested: Boolean = true

    var parent: ITransformNode? = null

    override fun updateTransform() {
        worldMatrix.set(position, rotation, scale)

        val parent = parent ?: entityOrNull?.parentEntity?.getComponentOrNull(componentName) as ITransformNode?
        if (parent != null) worldMatrix.mulLeft(parent.worldMatrix)

        isTransformUpdateRequested = false
    }

    companion object {
        fun initComponents() {
            ECS.descriptor({ TransformNode() }) {
                addAliases(ITransformNode::class)
                vec3("position", { position }) { position.set(it) }
                vec4("rotation", { rotation }) { rotation.set(it) }
                vec3("scale", { scale }) { scale.set(it) }.apply { default = { MATH.One3 } }
                mat4("worldMatrix", { worldMatrix }) { worldMatrix.set(it) }

                descriptor { Scene() }
                descriptor({ Object3D() }) {
                    addAliases(IObject3D::class)
                    ref("armature", { armature }) { armature = it }
                }
                descriptor({ PointLight() }) {}
                descriptor({ DirectionalLight() }) {
                    vec3("color", { color }) { color.set(it) }
                }
                descriptor({ Camera() }) {
                    addAliases(ICamera::class)
                }
            }
        }
    }
}