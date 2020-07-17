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

package org.ksdfv.thelema.test.shaders

import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.APP
import org.ksdfv.thelema.g3d.ActiveCamera
import org.ksdfv.thelema.g3d.Camera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.math.*
import org.ksdfv.thelema.mesh.ScreenQuad
import org.ksdfv.thelema.mesh.build.BoxMeshBuilder
import org.ksdfv.thelema.mesh.build.PlaneMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.texture.Attachments
import org.ksdfv.thelema.texture.FrameBuffer
import org.ksdfv.thelema.utils.LOG
import kotlin.math.max
import kotlin.math.min

class CascadedShadowMappingBaseTest: Test("Cascaded shadow mapping base") {
    val lightDirection = Vec3(-1f, -1f, -1f).nor()

    val numCascades = 4

    val cascadeEnd = arrayOf(0f, 5f, 15f, 40f, 200f)

    val lightView = Mat4().setToLookAt(IVec3.Zero, lightDirection, IVec3.Y)

    val sceneCameraFrustumPoints = Array<MutableList<IVec3>>(numCascades) {
        ArrayList<IVec3>().apply {
            for (i in 0 until 8) {
                add(Vec3())
            }
        }
    }

    val lightProj = Array<IMat4>(numCascades) { Mat4() }

    val tmpMat = Mat4()
    val ligthPos = Vec3()
    val centroid = Vec3()

    val sceneCameraFrustum = Frustum(ActiveCamera.inverseViewProjectionMatrix)

    // This can be used to view the shadows of objects that are behind the camera.
    // The higher value, the more objects will be captured, but may be impact shadow quality.
    val lightPositionOffsetMul = 5f

    fun updateFrustums() {
        ActiveCamera.update()
        sceneCameraFrustum.update(ActiveCamera.inverseViewProjectionMatrix)

        val sceneCameraFrustumPoints = sceneCameraFrustum.points

        lightView.setToLookAt(ActiveCamera.position, lightDirection, IVec3.Y)

        val camFarSubNear = ActiveCamera.far

        for (i in 0 until numCascades) {
            val frustumPoints = this.sceneCameraFrustumPoints[i]
            centroid.set(0f, 0f, 0f)

            val alphaNear = cascadeEnd[i] / camFarSubNear
            for (j in 0 until 4) {
                val p = frustumPoints[j]
                p.set(sceneCameraFrustumPoints[j]).lerp(sceneCameraFrustumPoints[j + 4], alphaNear)
                centroid.add(p)
            }

            val alphaFar = cascadeEnd[i + 1] / camFarSubNear
            for (j in 0 until 4) {
                val p = frustumPoints[j + 4]
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

            lightView.setToLookAt(centroid, lightDirection, IVec3.Y)

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
                far = (maxZ - minZ) * lightPositionOffsetMul
            )

            ligthPos.set(lightDirection).scl(-maxZ * lightPositionOffsetMul).add(centroid)
            tmpMat.setToLookAt(ligthPos, lightDirection, IVec3.Y)

            lightMat.mul(tmpMat)
        }
    }

    override fun testMain() {
        @Language("GLSL")
        val sceneObjectShader = Shader(
            vertCode = """
attribute vec3 aPosition;
uniform vec3 pos;
uniform mat4 viewProj;

uniform mat4 lightMatrix[$numCascades];
varying vec4 lightSpacePos[$numCascades];
varying vec4 clipSpacePos;

void main() {
    vec4 worldPos = vec4(aPosition + pos, 1.0);
    
    for (int i = 0; i < $numCascades; i++) {
        lightSpacePos[i] = lightMatrix[i] * worldPos;    
    }
    
    clipSpacePos = viewProj * worldPos;
    gl_Position = clipSpacePos;
}""",
            fragCode = """
uniform vec4 color;

uniform sampler2D shadowMap[$numCascades];
uniform float cascadeEnd[$numCascades];
varying vec4 lightSpacePos[$numCascades];
varying vec4 clipSpacePos;

void main () {
    gl_FragColor = color;

    for (int i = 0; i < $numCascades; i++) {
        if (clipSpacePos.z < cascadeEnd[i]) {
            vec3 cascadeColor = vec3(1.0);
        
            if (i == 0) {
                cascadeColor = vec3(1.0, 0.0, 0.0);
            } else if (i == 1) {
                cascadeColor = vec3(0.0, 1.0, 0.0);
            } else if (i == 2) {
                cascadeColor = vec3(0.0, 0.0, 1.0);
            }
            
            float shadowFactor = 0.0;
            vec4 lightSpacePos = lightSpacePos[i];
            
            vec3 projCoords = (lightSpacePos.xyz / lightSpacePos.w) * 0.5 + 0.5;
        
            float currentDepth = projCoords.z;
            float closestDepth = texture2D(shadowMap[i], projCoords.xy).x;
        
            float bias = 0.001;
            if (currentDepth - bias > closestDepth) {
                shadowFactor = 0.5;
            }
            
            gl_FragColor = vec4(mix(gl_FragColor.xyz * (1.0 - shadowFactor), cascadeColor, 0.5), color.a);
            break;
        }    
    }
}""")

        sceneObjectShader.bind()
        for (i in 0 until numCascades) {
            sceneObjectShader["shadowMap[$i]"] = i
        }

        @Language("GLSL")
        val depthRenderShader = Shader(
            vertCode = """
attribute vec3 aPosition;
uniform vec3 pos;
uniform mat4 viewProj;

void main() {
    vec4 worldPos = vec4(aPosition + pos, 1.0);
    gl_Position = viewProj * vec4(aPosition + pos, 1.0);
}""",
            // fragment shader can be empty for depth rendering
            fragCode = "void main() {}"
        )

        ActiveCamera.api = Camera(near = 0.1f, far = 200f)

        updateFrustums()

        val depthBuffers = Array(numCascades) {
            val buffer = FrameBuffer(1024, 1024)
            buffer.attachments.add(Attachments.depth())
            buffer.buildAttachments()
            buffer.getTexture(0).apply {
                bind()
                minFilter = GL_NEAREST
                magFilter = GL_NEAREST
            }
            buffer
        }

        val cube = BoxMeshBuilder().build()
        val plane = PlaneMeshBuilder(width = 500f, height = 500f).build()

        val cubeColor = Vec4(1f, 0.5f, 0f, 1f)

        GL.isDepthTestEnabled = true

        val control = OrbitCameraControl(
            zenith = 1f,
            azimuth = 0f,
            target = Vec3(10f, 3f, 0f),
            targetDistance = 10f
        )

        control.listenToMouse()
        LOG.info(control.help)

        // create screen quad for debugging
        val screenQuad = ScreenQuad.TextureRenderer()

        val cubesStartX = -100f
        val cubesEndX = 100f
        val cubesStepX = 20f

        val cubesStartZ = -100f
        val cubesEndZ = 100f
        val cubesStepZ = 20f

        val cubesY = 1f

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)

            updateFrustums()

            for (i in 0 until numCascades) {
                depthBuffers[i].render {
                    GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                    GL.cullFaceMode = GL_FRONT

                    val shader = depthRenderShader
                    shader.bind()
                    shader["viewProj"] = lightProj[i]

                    // render plane
                    shader.set("pos", 0f, 0f, 0f)
                    plane.render(shader)

                    // render cubes
                    var xi = cubesStartX
                    while (xi < cubesEndX) {
                        var zi = cubesStartZ
                        while (zi < cubesEndZ) {
                            shader.set("pos", xi, cubesY, zi)
                            cube.render(shader)
                            zi += cubesStepZ
                        }
                        xi += cubesStepX
                    }

                    GL.cullFaceMode = GL_BACK
                }
            }

            val shader = sceneObjectShader
            shader.bind()
            shader["viewProj"] = ActiveCamera.viewProjectionMatrix

            for (i in 0 until numCascades) {
                shader["lightMatrix[$i]"] = lightProj[i]
                shader["cascadeEnd[$i]"] = cascadeEnd[i + 1]

                depthBuffers[i].getTexture(0).bind(i)
            }

            // render plane
            shader.set("pos", 0f, 0f, 0f)
            shader.set("color", 1f, 1f, 1f, 1f)
            plane.render(shader)

            // render cubes
            shader["color"] = cubeColor
            var xi = cubesStartX
            while (xi < cubesEndX) {
                var zi = cubesStartZ
                while (zi < cubesEndZ) {
                    shader.set("pos", xi, cubesY, zi)
                    cube.render(shader)
                    zi += cubesStepZ
                }
                xi += cubesStepX
            }

            // render light depth maps
            for (i in 0 until numCascades) {
                screenQuad.setPosition(-0.75f + i * 0.45f, -0.75f)
                screenQuad.setScale(0.2f, 0.2f)
                screenQuad.render(depthBuffers[i].getTexture(0), clearMask = null)
            }
        }
    }
}