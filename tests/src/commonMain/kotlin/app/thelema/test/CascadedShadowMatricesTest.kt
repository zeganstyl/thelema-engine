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

package app.thelema.test

import app.thelema.app.APP
import app.thelema.g3d.cam.Camera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.input.KEY
import app.thelema.math.*
import app.thelema.g3d.mesh.FrustumMesh
import app.thelema.shader.Shader
import app.thelema.utils.Color
import app.thelema.utils.LOG
import kotlin.math.max
import kotlin.math.min

/** @author zeganstyl */
class CascadedShadowMatricesTest: Test {
    override val name: String
        get() = "Cascaded shadow matrices"

    val lightDirection = Vec3(1f, -1f, -1f).nor()
    val lightPositionOffset = 0f

    val numCascades = 2
    val cascadeEnd = arrayOf(0.1f, 5f, 10f)
    val lightView = Mat4().setToLook(MATH.Zero3, lightDirection, MATH.Y)

    val sceneCameraFrustumPoints = Array<MutableList<IVec3>>(numCascades) {
        ArrayList<IVec3>().apply {
            for (i in 0 until 8) {
                add(Vec3())
            }
        }
    }

    val cascadeFrustums = Array(numCascades) { Frustum() }

    val sceneCameraFrustumMeshes = Array(numCascades) { FrustumMesh() }
    val cascadeFrustumMeshes = Array(numCascades) { FrustumMesh() }

    val lightProj = Array<IMat4>(numCascades) { Mat4() }

    val tmpMat = Mat4()
    val tmpVec = Vec3()
    val centroid = Vec3()

    val sceneCamera = Camera {
        near = 0.1f
        far = 10f
        isOrthographic = false
    }
    val sceneCameraFrustum = Frustum(sceneCamera.inverseViewProjectionMatrix)

    fun updateFrustums() {
        sceneCamera.updateCamera()
        sceneCameraFrustum.setFromMatrix(sceneCamera.inverseViewProjectionMatrix)

        val sceneCameraFrustumPoints = sceneCameraFrustum.points
        val camFarSubNear = sceneCamera.far - sceneCamera.near

        lightView.setToLook(sceneCamera.position, lightDirection, MATH.Y)

        for (i in 0 until numCascades) {
            val alphaNear = cascadeEnd[i] / camFarSubNear
            val frustumPoints = this.sceneCameraFrustumPoints[i]

            centroid.set(0f, 0f, 0f)

            var minZ = Float.MAX_VALUE
            var maxZ = Float.MIN_VALUE

            for (j in 0 until 4) {
                val p = frustumPoints[j]
                p.set(sceneCameraFrustumPoints[j]).lerp(sceneCameraFrustumPoints[j + 4], alphaNear)
                centroid.add(p)
                minZ = min(minZ, p.z)
                maxZ = max(maxZ, p.z)
            }

            val alphaFar = cascadeEnd[i + 1] / camFarSubNear
            for (j in 0 until 4) {
                val p = frustumPoints[j + 4]
                p.set(sceneCameraFrustumPoints[j]).lerp(sceneCameraFrustumPoints[j + 4], alphaFar)
                centroid.add(p)
                minZ = min(minZ, p.z)
                maxZ = max(maxZ, p.z)
            }

            // scene camera frustum part
            sceneCameraFrustumMeshes[i].frustumPoints = frustumPoints
            sceneCameraFrustumMeshes[i].updateMesh()

            centroid.scl(0.125f)

            lightView.setToLook(centroid, lightDirection, MATH.Y)

            var minX = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var minY = Float.MAX_VALUE
            var maxY = Float.MIN_VALUE
            minZ = Float.MAX_VALUE
            maxZ = Float.MIN_VALUE

            for (j in 0 until 8) {
                val vW = frustumPoints[j].mul(lightView)

                minX = min(minX, vW.x)
                maxX = max(maxX, vW.x)
                minY = min(minY, vW.y)
                maxY = max(maxY, vW.y)
                minZ = min(minZ, vW.z)
                maxZ = max(maxZ, vW.z)
            }

            val lightMat = lightProj[i]
            lightMat.setToOrtho(
                left = minX,
                right = maxX,
                bottom = minY,
                top = maxY,
                near = 0f,
                far = maxZ - minZ + lightPositionOffset
            )

            tmpMat.setToLook(tmpVec.set(lightDirection).scl(-maxZ - lightPositionOffset).add(centroid), lightDirection, MATH.Y)

            lightMat.mul(tmpMat)

            cascadeFrustums[i].setFromMatrix(tmpMat.set(lightMat).inv())
            cascadeFrustumMeshes[i].frustumPoints = cascadeFrustums[i].points
            cascadeFrustumMeshes[i].updateMesh()
        }
    }

    override fun testMain() {

        val shader = Shader(
            vertCode = """
attribute vec3 POSITION;
uniform mat4 viewProj;

void main () {
    gl_Position = viewProj * vec4(POSITION, 1.0);
}""",
            fragCode = """
uniform vec4 color;
void main () {
    gl_FragColor = color;
}""")

        val colors = arrayOf(Color.ORANGE, Color.GREEN, Color.CYAN, Color.OLIVE, Color.PINK)

        for (i in sceneCameraFrustumMeshes.indices) {
            sceneCameraFrustumMeshes[i] = FrustumMesh(sceneCameraFrustumPoints[i])
        }

        for (i in cascadeFrustumMeshes.indices) {
            cascadeFrustumMeshes[i] = FrustumMesh(cascadeFrustums[i].points)
        }

        updateFrustums()

        val camera = Camera {
            lookAt(Vec3(3f, 3f, -3f), Vec3(0f, 0f, 0f))
            near = 0.1f
            far = 100f
        }

        val control = OrbitCameraControl {
            this.camera = camera
            zenith = 1f
            azimuth = 0f
            target = Vec3(10f, 3f, 0f)
            targetDistance = 10f
        }
        control.listenToMouse()

        LOG.info("Use W A S D keys to move object")

        val quaternion = Vec4()

        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            val delta = APP.deltaTime

            control.update(delta)
            camera.updateCamera()

            if (KEY.isPressed(KEY.A)) {
                quaternion.setQuaternionByAxis(x = 0f, y = 1f, z = 0f, delta)
                quaternion.rotateVec3(sceneCamera.direction)
                updateFrustums()
            } else if (KEY.isPressed(KEY.D)) {
                quaternion.setQuaternionByAxis(x = 0f, y = 1f, z = 0f, -delta)
                quaternion.rotateVec3(sceneCamera.direction)
                updateFrustums()
            }

            if (KEY.isPressed(KEY.W)) {
                sceneCamera.position.add(tmpVec.set(sceneCamera.direction).scl(delta * 5f))
                updateFrustums()
            } else if (KEY.isPressed(KEY.S)) {
                sceneCamera.position.sub(tmpVec.set(sceneCamera.direction).scl(delta * 5f))
                updateFrustums()
            }

            shader.bind()
            shader["viewProj"] = camera.viewProjectionMatrix

            for (i in sceneCameraFrustumMeshes.indices) {
                shader["color"] = colors[i]
                sceneCameraFrustumMeshes[i].render(shader)
            }

            for (i in cascadeFrustumMeshes.indices) {
                shader["color"] = colors[i + sceneCameraFrustumMeshes.size]
                cascadeFrustumMeshes[i].render(shader)
            }
        }
    }
}