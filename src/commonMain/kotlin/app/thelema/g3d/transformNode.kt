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

package app.thelema.g3d

import app.thelema.ecs.*
import app.thelema.g3d.cam.Camera
import app.thelema.g3d.cam.ICamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.light.DirectionalLight
import app.thelema.g3d.light.PointLight
import app.thelema.gl.IMesh
import app.thelema.json.IJsonObject
import app.thelema.math.*
import app.thelema.utils.iterate

/** Spatial transform node. Node may contain children and forms a tree of nodes.
 *
 * @author zeganstyl */
interface ITransformNode: IEntityComponent {
    /** Local translation, relative to the parent. */
    val position: IVec3

    /** Local rotation, relative to the parent. It may not work, check type of this node [TransformDataType]. */
    val rotation: IVec4

    /** Local scale, relative to the parent. It may not work, check type of this node [TransformDataType]. */
    val scale: IVec3

    /** Global world transform, product of multiply local and parent transform data, calculated via [updateTransform]. */
    val worldMatrix: IMat4

    val previousWorldMatrix: IMat4?

    /** Global position */
    val worldPosition: IVec3

    override val componentName: String
        get() = "TransformNode"

    var isTransformUpdateRequested: Boolean

    fun getDirection(out: IVec3): IVec3 = worldMatrix.getWorldForward(out).nor()

    fun getUpVector(out: IVec3): IVec3 = worldMatrix.getWorldUp(out).nor()

    fun rotateAroundAxis(axisX: Float, axisY: Float, axisZ: Float, radians: Float) {
        if (MATH.isNotZero(radians)) {
            if (MATH.isZero(axisX) && MATH.isZero(axisZ) && MATH.isEqual(axisY, 1f)) {
                rotation.rotateQuaternionByY(radians)
            } else {
                rotation.rotateQuaternionByAxis(axisX, axisY, axisZ, radians)
            }
            requestTransformUpdate()
        }
    }

    /** Rotate around Up vector */
    fun rotateAroundUp(radians: Float) {
        val x = worldMatrix.m01
        val y = worldMatrix.m11
        val z = worldMatrix.m21
        val len2 = x * x + y * y + z * z
        if (len2 == 0f || len2 == 1f) {
            rotateAroundAxis(x, y, z, radians)
        } else {
            val inv = MATH.invSqrt(len2)
            rotateAroundAxis(x * inv, y * inv, z * inv, radians)
        }
    }

    fun translate(x: Float, y: Float, z: Float) {
        position.add(x, y, z)
        requestTransformUpdate()
    }

    fun translateForward(distance: Float) {
        // vector normalization
        val x = worldMatrix.m02
        val y = worldMatrix.m12
        val z = worldMatrix.m22
        val len2 = x * x + y * y + z * z
        if (len2 == 0f || len2 == 1f) {
            position.add(x * distance, y * distance, z * distance)
        } else {
            val inv = MATH.invSqrt(len2)
            position.add(x * inv * distance, y * inv * distance, z * inv * distance)
        }
        requestTransformUpdate()
    }

    fun setPosition(x: Float, y: Float, z: Float) {
        position.set(x, y, z)
        requestTransformUpdate()
    }

    fun scale(x: Float, y: Float, z: Float) {
        scale.add(x, y, z)
        requestTransformUpdate()
    }

    fun setScale(x: Float, y: Float, z: Float) {
        scale.set(x, y, z)
        requestTransformUpdate()
    }

    fun addListener(listener: TransformNodeListener)

    fun removeListener(listener: TransformNodeListener)

    fun onWorldMatrixChanged(block: ITransformNode.() -> Unit): TransformNodeListener =
        TransformNodeListener(block).also { addListener(it) }
    
    fun requestTransformUpdate(recursive: Boolean = true)

    fun enablePreviousMatrix(enable: Boolean = true)

    fun updatePreviousMatrix()

    fun updateTransform()

    fun reset()
}

fun IEntity.transformNode(block: ITransformNode.() -> Unit) = component(block)
fun IEntity.transformNode() = component<ITransformNode>()

/** Translation (position), rotation, scale node (TRS). This is default transform node.
 *
 * @author zeganstyl */
class TransformNode: ITransformNode {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            value?.forEachComponent {
                addedSiblingComponent(it)
            }
            value?.forEachChildEntity { childEntity ->
                childEntity.forEachComponent { component ->
                    addedChildComponent(component)
                }
            }
        }

    override var isTransformUpdateRequested: Boolean = true

    var attachedTo: ITransformNode? = null

    override val position: IVec3 = Vec3(0f, 0f, 0f)
    override val rotation: IVec4 = Vec4(0f, 0f, 0f, 1f)
    override val scale: IVec3 = Vec3(1f, 1f, 1f)
    override val worldMatrix: IMat4 = Mat4()

    override val worldPosition: IVec3 = Vec3Mat4Translation(worldMatrix)

    private var previousWorldMatrixInternal: IMat4? = if (isPreviousMatrixEnabled) Mat4() else null
    override val previousWorldMatrix: IMat4?
        get() = previousWorldMatrixInternal

    var listeners: ArrayList<TransformNodeListener>? = null

    override fun addListener(listener: TransformNodeListener) {
        if (listeners == null) listeners = ArrayList(1)
        listeners?.add(listener)
        listeners?.trimToSize()
    }

    override fun removeListener(listener: TransformNodeListener) {
        listeners?.remove(listener)
        listeners?.trimToSize()
    }

    override fun requestTransformUpdate(recursive: Boolean) {
        isTransformUpdateRequested = true
        if (recursive) {
            entityOrNull?.forEachEntityInBranch {
                it.componentOrNull<ITransformNode>()?.isTransformUpdateRequested = true
            }
        }
    }

    override fun addedSiblingComponent(component: IEntityComponent) {
        addedChildComponent(component)
    }

    override fun addedEntity(entity: IEntity) {
        entity.forEachComponent { addedChildComponent(it) }
    }

    override fun addedChildComponent(component: IEntityComponent) {
        if (component is IMesh) {
            if (component.node == null) component.node = this
        }
    }

    override fun enablePreviousMatrix(enable: Boolean) {
        if (enable) {
            if (previousWorldMatrixInternal == null) previousWorldMatrixInternal = Mat4()
        } else {
            previousWorldMatrixInternal = null
        }
    }

    override fun updatePreviousMatrix() {
        previousWorldMatrixInternal?.set(worldMatrix)
    }

    override fun updateTransform() {
        worldMatrix.set(position, rotation, scale)

        val parent = attachedTo ?: entityOrNull?.parentEntity?.componentOrNull(componentName) as ITransformNode?
        if (parent != null) {
            if (parent.isTransformUpdateRequested) parent.updateTransform()
            worldMatrix.mulLeft(parent.worldMatrix)
        }

        isTransformUpdateRequested = false

        listeners?.iterate { it.worldMatrixChanged(this) }
    }

    override fun reset() {
        position.set(0f, 0f, 0f)
        rotation.set(0f, 0f, 0f, 1f)
        scale.set(1f, 1f, 1f)
        worldMatrix.idt()
        updateTransform()
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)
        requestTransformUpdate()
    }

    companion object {
        /** Default value */
        var isPreviousMatrixEnabled: Boolean = true

        fun setupTransformNodeComponents() {
            ECS.descriptor({ TransformNode() }) {
                setAliases(ITransformNode::class)
                vec3("position", { position }) { position.set(it) }
                vec4("rotation", { rotation }) { rotation.set(it) }
                vec3("scale", MATH.One3, { scale }) { scale.set(it) }
                mat4("worldMatrix", { worldMatrix }) { worldMatrix.set(it) }

                descriptor({ Scene() }) {
                    setAliases(IScene::class)

                    descriptor({ SceneInstance() }) {
                        setAliases(ISceneInstance::class)
                        refAbs(SceneInstance::provider)
                    }

                    descriptor({ SceneProvider() }) {
                        setAliases(ISceneProvider::class)
                    }
                }
                descriptor { SimpleSkybox() }
                descriptor { Skybox() }
                descriptor({ PointLight() }) {
                    float("range", { range }) { range = it }
                    vec4("color", { color }) { color.set(it) }
                }
                descriptor({ DirectionalLight() }) {
                    vec4("color", { color }) { color.set(it) }
                }
                descriptor({ Camera() }) {
                    setAliases(ICamera::class)
                    descriptor { OrbitCameraControl() }
                }
            }
        }
    }
}

interface TransformNodeListener {
    fun worldMatrixChanged(node: ITransformNode)
}

fun TransformNodeListener(block: ITransformNode.() -> Unit): TransformNodeListener = object : TransformNodeListener {
    override fun worldMatrixChanged(node: ITransformNode) {
        block(node)
    }
}
