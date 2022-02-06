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
import kotlin.native.concurrent.ThreadLocal

/** Spatial transform node. Node may contain children and forms a tree of nodes.
 *
 * @author zeganstyl */
interface ITransformNode: IEntityComponent {
    /** Local translation, relative to the parent. */
    var position: IVec3C

    /** Local rotation. */
    var rotation: IMat3C

    /** Local scale. */
    var scale: IVec3C

    /** Global world transform, product of multiply local and parent transform data, calculated via [updateTransform]. */
    val worldMatrix: IMat4

    val previousWorldMatrix: IMat4?

    /** Global position */
    val worldPosition: IVec3C

    override val componentName: String
        get() = "TransformNode"

    var isTransformUpdateRequested: Boolean

    var useParentTransform: Boolean

    fun getDirection(out: IVec3): IVec3 = worldMatrix.getWorldForward(out).nor()

    fun getUpVector(out: IVec3): IVec3 = worldMatrix.getWorldUp(out).nor()

    fun rotateAroundAxis(axisX: Float, axisY: Float, axisZ: Float, radians: Float)

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

    fun translate(x: Float, y: Float, z: Float)

    fun translate(offset: IVec3C)

    fun translateForward(distance: Float)

    fun setPosition(x: Float, y: Float, z: Float)

    fun setRotation(qx: Float, qy: Float, qz: Float, qw: Float)

    fun addScale(x: Float, y: Float, z: Float)

    fun setScale(x: Float, y: Float, z: Float)

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

    override var useParentTransform: Boolean = true

    override val worldMatrix: IMat4 = Mat4()

    private val _position = Vec3()
    override var position: IVec3C
        get() = _position
        set(value) {
            _position.set(value)
            requestTransformUpdate()
        }

    private val _rotation = Mat3()
    override var rotation: IMat3C
        get() = _rotation
        set(value) {
            _rotation.set(value)
            requestTransformUpdate()
        }

    private val _scale = Vec3(1f, 1f, 1f)
    override var scale: IVec3C
        get() = _scale
        set(value) {
            _scale.set(value)
            requestTransformUpdate()
        }

    override val worldPosition: IVec3 = Vec3Mat4Translation(worldMatrix)

    private var previousWorldMatrixInternal: IMat4? = if (isPreviousMatrixEnabled) Mat4() else null
    override val previousWorldMatrix: IMat4?
        get() = previousWorldMatrixInternal

    var listeners: ArrayList<TransformNodeListener>? = null

    private inline fun notifyPropChanged(prop: String, value: Any?) {
        IEntityComponent.propertiesLinkingMap?.get(this)?.also { listeners ->
            listeners.iterate { it.setProperty(prop, value) }
        }
    }

    override fun translateForward(distance: Float) {
        // vector normalization
        val x = worldMatrix.m02
        val y = worldMatrix.m12
        val z = worldMatrix.m22
        val len2 = x * x + y * y + z * z
        if (len2 == 0f || len2 == 1f) {
            _position.add(x * distance, y * distance, z * distance)
        } else {
            val inv = MATH.invSqrt(len2)
            _position.add(x * inv * distance, y * inv * distance, z * inv * distance)
        }
        notifyPropChanged(ITransformNode::position.name, _position)
        requestTransformUpdate()
    }

    override fun translate(offset: IVec3C) {
        _position.add(offset)
        notifyPropChanged(ITransformNode::position.name, _position)
        requestTransformUpdate()
    }

    override fun translate(x: Float, y: Float, z: Float) {
        _position.add(x, y, z)
        notifyPropChanged(ITransformNode::position.name, _position)
        requestTransformUpdate()
    }

    override fun setPosition(x: Float, y: Float, z: Float) {
        _position.set(x, y, z)
        notifyPropChanged(ITransformNode::position.name, _position)
        requestTransformUpdate()
    }

    override fun setRotation(qx: Float, qy: Float, qz: Float, qw: Float) {
        _rotation.setAsRotation(qx, qy, qz, qw)
        notifyPropChanged(ITransformNode::rotation.name, _rotation)
        requestTransformUpdate()
    }

    override fun setScale(x: Float, y: Float, z: Float)  {
        _scale.set(x, y, z)
        notifyPropChanged(ITransformNode::scale.name, _scale)
        requestTransformUpdate()
    }

    override fun addScale(x: Float, y: Float, z: Float) {
        _scale.add(x, y, z)
        ITransformNode::position.name
        notifyPropChanged(ITransformNode::scale.name, _scale)
        requestTransformUpdate()
    }

    override fun rotateAroundAxis(axisX: Float, axisY: Float, axisZ: Float, radians: Float)  {
        if (MATH.isNotZero(radians)) {
            if (MATH.isZero(axisX) && MATH.isZero(axisZ) && MATH.isEqual(axisY, 1f)) {
                _rotation.rotateAroundY33(radians)
            } else {
                _rotation.rotateAroundAxis(axisX, axisY, axisZ, radians)
            }
            requestTransformUpdate()
        }
    }

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
            entityOrNull?.forEachChildEntity {
                it.componentOrNull<ITransformNode>()?.requestTransformUpdate(recursive)
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
        worldMatrix.set(_position, _rotation, _scale)

        if (useParentTransform) {
            val parent = entityOrNull?.parentEntity?.componentOrNull(componentName) as ITransformNode?
            if (parent != null) {
                if (parent.isTransformUpdateRequested) parent.updateTransform()
                worldMatrix.mulLeft(parent.worldMatrix)
            }
        }

        isTransformUpdateRequested = false

        listeners?.iterate { it.worldMatrixChanged(this) }
    }

    override fun reset() {
        _position.set(0f, 0f, 0f)
        _rotation.idt()
        _scale.set(1f, 1f, 1f)
        worldMatrix.idt()
        updateTransform()
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)
        requestTransformUpdate()
    }

    @ThreadLocal
    companion object {
        /** Default value */
        var isPreviousMatrixEnabled: Boolean = true

        fun setupTransformNodeComponents() {
            ECS.descriptorI<ITransformNode>({ TransformNode() }) {
                vec3C(ITransformNode::position)
                mat3C(ITransformNode::rotation)
                vec3C(ITransformNode::scale, MATH.One3)

                descriptorI<IScene>({ Scene() }) {
                    refAbs(IScene::renderingPipeline)
                    ref(IScene::activeCamera)

                    descriptorI<ISceneInstance>({ SceneInstance() }) {
                        refAbs(ISceneInstance::provider)
                    }

                    descriptorI<ISceneProvider>({ SceneProvider() }) {}
                }
                descriptor { SimpleSkybox() }
                descriptor { Skybox() }
                descriptor({ PointLight() }) {
                    float(PointLight::range)
                    vec3(PointLight::color)
                    float(PointLight::intensity, 1f)
                    bool(PointLight::isShadowEnabled, false)
                    bool(PointLight::isLightEnabled, true)
                }
                descriptor({ DirectionalLight() }) {
                    vec3(DirectionalLight::color)
                    float(DirectionalLight::intensity, 1f)
                    bool(DirectionalLight::lookAtZero, true)
                    bool(DirectionalLight::isShadowEnabled, false)
                    bool(DirectionalLight::isLightEnabled, true)
                    float(DirectionalLight::lightPositionOffset, 50f)
                    int(DirectionalLight::shadowCascadesNum, 4)
                    int(DirectionalLight::shadowMapWidth, 1024)
                    int(DirectionalLight::shadowMapHeight, 1024)
                    float(DirectionalLight::shadowSoftness, 1f)
                }
                descriptor({ Camera() }) {
                    setAliases(ICamera::class)
                    descriptor({ OrbitCameraControl() }) {
                        float(OrbitCameraControl::azimuth, 0f)
                        float(OrbitCameraControl::zenith, 1f)
                        float(OrbitCameraControl::minTargetDistance, -1f)
                        float(OrbitCameraControl::maxTargetDistance, -1f)
                        float(OrbitCameraControl::targetDistance, 5f)
                        vec3C(OrbitCameraControl::target)
                        float(OrbitCameraControl::scrollFactor, 0.1f)
                        float(OrbitCameraControl::scrollTransition, 0.3f)
                    }
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
