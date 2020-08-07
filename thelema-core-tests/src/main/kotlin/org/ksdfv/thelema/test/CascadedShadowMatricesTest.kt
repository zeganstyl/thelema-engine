/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.test

import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.APP
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.input.KB
import org.ksdfv.thelema.math.*
import org.ksdfv.thelema.mesh.IMesh
import org.ksdfv.thelema.mesh.Mesh
import org.ksdfv.thelema.mesh.build.FrustumMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.utils.Color
import org.ksdfv.thelema.utils.LOG
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
    val lightView = Mat4().setToLookAt(IVec3.Zero, lightDirection, IVec3.Y)

    val sceneCameraFrustumPoints = Array<MutableList<IVec3>>(numCascades) {
        ArrayList<IVec3>().apply {
            for (i in 0 until 8) {
                add(Vec3())
            }
        }
    }

    val cascadeFrustums = Array(numCascades) { Frustum() }

    val sceneCameraFrustumMeshes = Array<IMesh>(numCascades) { Mesh() }
    val cascadeFrustumMeshes = Array<IMesh>(numCascades) { Mesh() }

    val lightProj = Array<IMat4>(numCascades) { Mat4() }

    val tmpMat = Mat4()
    val tmpVec = Vec3()
    val centroid = Vec3()

    val sceneCamera =
        Camera(near = 0.1f, far = 10f, isOrthographic = false)
    val sceneCameraFrustum = Frustum(sceneCamera.inverseViewProjectionMatrix)

    fun updateFrustums() {
        sceneCamera.update()
        sceneCameraFrustum.update(sceneCamera.inverseViewProjectionMatrix)

        val sceneCameraFrustumPoints = sceneCameraFrustum.points
        val camFarSubNear = sceneCamera.far - sceneCamera.near

        lightView.setToLookAt(sceneCamera.position, lightDirection, IVec3.Y)

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
            FrustumMeshBuilder.updateMesh(sceneCameraFrustumMeshes[i], frustumPoints)
            sceneCameraFrustumMeshes[i].vertices?.loadBufferToGpu()

            centroid.scl(0.125f)

            lightView.setToLookAt(centroid, lightDirection, IVec3.Y)

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

            tmpMat.setToLookAt(tmpVec.set(lightDirection).scl(-maxZ - lightPositionOffset).add(centroid), lightDirection, IVec3.Y)

            lightMat.mul(tmpMat)

            cascadeFrustums[i].update(tmpMat.set(lightMat).inv())
            FrustumMeshBuilder.updateMesh(cascadeFrustumMeshes[i], cascadeFrustums[i].points)
            cascadeFrustumMeshes[i].vertices?.loadBufferToGpu()
        }
    }

    override fun testMain() {
        @Language("GLSL")
        val shader = Shader(
            vertCode = """
attribute vec3 aPosition;
uniform mat4 viewProj;

void main () {
    gl_Position = viewProj * vec4(aPosition, 1.0);
}""",
            fragCode = """
uniform vec4 color;
void main () {
    gl_FragColor = color;
}""")

        val colors = arrayOf(Color.ORANGE, Color.GREEN, Color.CYAN, Color.OLIVE, Color.PINK)

        for (i in sceneCameraFrustumMeshes.indices) {
            sceneCameraFrustumMeshes[i] = FrustumMeshBuilder(sceneCameraFrustumPoints[i]).build()
        }

        for (i in cascadeFrustumMeshes.indices) {
            cascadeFrustumMeshes[i] = FrustumMeshBuilder(cascadeFrustums[i].points).build()
        }

        updateFrustums()

        ActiveCamera.api = Camera(
            from = Vec3(3f, 3f, -3f),
            to = Vec3(0f, 0f, 0f),
            near = 0.1f,
            far = 100f
        )

        val control = OrbitCameraControl(
            zenith = 1f,
            azimuth = 0f,
            target = Vec3(10f, 3f, 0f),
            targetDistance = 10f
        )
        control.listenToMouse()
        LOG.info(control.help)

        LOG.info("Use W A S D keys to move object")

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            val delta = APP.deltaTime

            control.update(delta)
            ActiveCamera.update()

            if (KB.isKeyPressed(KB.A)) {
                sceneCamera.direction.rotate(delta, 0f, 1f, 0f)
                updateFrustums()
            } else if (KB.isKeyPressed(KB.D)) {
                sceneCamera.direction.rotate(-delta, 0f, 1f, 0f)
                updateFrustums()
            }

            if (KB.isKeyPressed(KB.W)) {
                sceneCamera.position.add(tmpVec.set(sceneCamera.direction).scl(delta * 5f))
                updateFrustums()
            } else if (KB.isKeyPressed(KB.S)) {
                sceneCamera.position.sub(tmpVec.set(sceneCamera.direction).scl(delta * 5f))
                updateFrustums()
            }

            shader.bind()
            shader["viewProj"] = ActiveCamera.viewProjectionMatrix

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