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

package app.thelema.g3d.light

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.g3d.IScene
import app.thelema.g3d.ShaderChannel
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.Camera
import app.thelema.g3d.node.ITransformNode
import app.thelema.g3d.node.TransformNode
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.gl.GL_NEAREST
import app.thelema.img.*
import app.thelema.math.*
import kotlin.math.max
import kotlin.math.min

/** @author zeganstyl */
class DirectionalLight: ILight {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            node = value?.componentTyped("TransformNode") ?: TransformNode()
        }

    override var node: ITransformNode = TransformNode()

    override val lightType: Int
        get() = LightType.Directional

    override var shadowMaps: Array<ITexture> = emptyArray()

    override var viewProjectionMatrices: Array<IMat4> = emptyArray()

    private var shadowFrameBuffers: Array<IFrameBuffer> = emptyArray()

    var shadowCascadesNum = 4

    var shadowCascadeEnd = arrayOf<Float>()

    var lightPositionOffset: Float = 0f

    override var lightIntensity: Float = 1f
    override val color: IVec3 = Vec3(1f)
    val direction: IVec3 = Vec3(0f, 0f, 1f)
    override var isLightEnabled: Boolean = true
    override var isShadowEnabled: Boolean = false

    override val componentName: String
        get() = "DirectionalLight"

    val lightViewTmp = Mat4()
    val lightPos = Vec3()
    val centroid = Vec3()

    /** Part of full camera frustum */
    val subFrustumPoints = Array<IVec3>(8) { Vec3() }

    val tmpCam = Camera()

    fun updateDirection() {
        direction.set(0f, 0f, 1f)
        node.worldMatrix.mul(direction)
        direction.nor()
    }

    override fun setupShadowMaps(width: Int, height: Int) {
        val shadowFrameBuffers = Array<IFrameBuffer>(shadowCascadesNum) {
            val depthBuffer = FrameBuffer(width, height)
            depthBuffer.attachments.add(Attachments.depth())
            depthBuffer.buildAttachments()
            if (GL.isGLES) {
                depthBuffer.getTexture(0).apply {
                    bind()
                    minFilter = GL_NEAREST
                    magFilter = GL_NEAREST
                }
            }
            depthBuffer
        }

        shadowCascadeEnd = arrayOf(
            ActiveCamera.far * 0.05f,
            ActiveCamera.far * 0.10f,
            ActiveCamera.far * 0.40f,
            ActiveCamera.far
        )

        viewProjectionMatrices = Array(shadowCascadesNum) { Mat4() }

        val shadowMaps = Array(shadowCascadesNum) { shadowFrameBuffers[it].getTexture(0) }

        this.shadowMaps = shadowMaps
        this.shadowFrameBuffers = shadowFrameBuffers
    }

    override fun renderShadowMaps(scene: IScene) {
        if (isShadowEnabled) {
            val sceneCameraFrustumPoints = ActiveCamera.frustum.points
            val camFarSubNear = ActiveCamera.far

            val tmp = ActiveCamera.proxy
            ActiveCamera.proxy = tmpCam
            tmpCam.direction.set(direction)
            tmpCam.near = 0f
            tmpCam.isOrthographic = true
            tmpCam.up.set(0f, 1f, 0f)

            val shadowFrameBuffers = shadowFrameBuffers
            for (i in 0 until shadowCascadesNum) {
                shadowFrameBuffers[i].render {
                    GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                    centroid.set(0f, 0f, 0f)

                    val alphaNear = if (i == 0) 0f else (shadowCascadeEnd[i - 1] / camFarSubNear)
                    for (j in 0 until 4) {
                        val p = subFrustumPoints[j]
                        p.set(sceneCameraFrustumPoints[j]).lerp(sceneCameraFrustumPoints[j + 4], alphaNear)
                        centroid.add(p)
                    }

                    val alphaFar = shadowCascadeEnd[i] / camFarSubNear
                    for (j in 0 until 4) {
                        val p = subFrustumPoints[j + 4]
                        p.set(sceneCameraFrustumPoints[j]).lerp(sceneCameraFrustumPoints[j + 4], alphaFar)
                        centroid.add(p)
                    }

                    centroid.scl(0.125f)

                    var minX = Float.MAX_VALUE
                    var maxX = Float.MIN_VALUE
                    var minY = Float.MAX_VALUE
                    var maxY = Float.MIN_VALUE
                    var minZ = Float.MAX_VALUE
                    var maxZ = Float.MIN_VALUE

                    lightViewTmp.setToLook(centroid, direction, MATH.Y)

                    for (j in 0 until 8) {
                        val vW = subFrustumPoints[j].mul(lightViewTmp)

                        minX = min(minX, vW.x)
                        maxX = max(maxX, vW.x)
                        minY = min(minY, vW.y)
                        maxY = max(maxY, vW.y)
                        minZ = min(minZ, vW.z)
                        maxZ = max(maxZ, vW.z)
                    }

                    val lightMat = viewProjectionMatrices[i]
                    val far = maxZ - minZ + lightPositionOffset
                    lightMat.setToOrtho(
                        left = minX,
                        right = maxX,
                        bottom = minY,
                        top = maxY,
                        near = 0f,
                        far = far
                    )

                    lightPos.set(direction).scl(-maxZ - lightPositionOffset).add(centroid)
                    lightViewTmp.setToLook(lightPos, direction, MATH.Y)

                    lightMat.mul(lightViewTmp)

                    tmpCam.far = far
                    tmpCam.position.set(lightPos)
                    tmpCam.viewProjectionMatrix.set(lightMat)
                    scene.render(ShaderChannel.Depth)
                }
            }

            ActiveCamera.proxy = tmp
        }
    }
}