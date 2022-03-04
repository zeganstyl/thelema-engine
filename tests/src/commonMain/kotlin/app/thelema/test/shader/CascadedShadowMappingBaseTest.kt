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

package app.thelema.test.shader

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.gl.*
import app.thelema.img.Attachments
import app.thelema.img.FrameBuffer
import app.thelema.math.*
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.gl.TextureRenderer
import app.thelema.img.render
import app.thelema.shader.Shader
import app.thelema.test.Test
import kotlin.math.max
import kotlin.math.min

/** @author zeganstyl */
class CascadedShadowMappingBaseTest: Test {
    override val name: String
        get() = "Cascaded shadow mapping base"

    val lightDirection = Vec3(-1f, -1f, -1f).nor()

    val numCascades = 4

    val cascadeEnd = arrayOf(0f, 5f, 15f, 40f, 200f)

    val lightView = Mat4().setToLook(MATH.Zero3, lightDirection, MATH.Y)

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
        ActiveCamera.updateCamera()
        sceneCameraFrustum.setFromMatrix(ActiveCamera.inverseViewProjectionMatrix)

        val sceneCameraFrustumPoints = sceneCameraFrustum.points

        lightView.setToLook(ActiveCamera.node.position, lightDirection, MATH.Y)

        val camFar = ActiveCamera.far

        for (i in 0 until numCascades) {
            val frustumPoints = this.sceneCameraFrustumPoints[i]
            centroid.set(0f, 0f, 0f)

            val alphaNear = cascadeEnd[i] / camFar
            for (j in 0 until 4) {
                val p = frustumPoints[j]
                p.set(sceneCameraFrustumPoints[j]).lerp(sceneCameraFrustumPoints[j + 4], alphaNear)
                centroid.add(p)
            }

            val alphaFar = cascadeEnd[i + 1] / camFar
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

            lightView.setToLook(centroid, lightDirection, MATH.Y)

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
            tmpMat.setToLook(ligthPos, lightDirection, MATH.Y)

            lightMat.mul(tmpMat)
        }
    }

    override fun testMain() {

        val sceneObjectShader = Shader(
            vertCode = """
attribute vec3 POSITION;
uniform vec3 pos;
uniform mat4 viewProj;

uniform mat4 lightMatrix[$numCascades];
varying vec4 lightSpacePos[$numCascades];
varying vec4 clipSpacePos;

void main() {
    vec4 worldPos = vec4(POSITION + pos, 1.0);
    
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


        val depthRenderShader = Shader(
            vertCode = """
attribute vec3 POSITION;
uniform vec3 pos;
uniform mat4 viewProj;

void main() {
    vec4 worldPos = vec4(POSITION + pos, 1.0);
    gl_Position = viewProj * vec4(POSITION + pos, 1.0);
}""",
            // fragment shader can be empty for depth rendering
            fragCode = "void main() {}"
        )

        ActiveCamera {
            near = 0.1f
            far = 200f
        }

        updateFrustums()

        val depthBuffers = Array(numCascades) {
            FrameBuffer {
                setResolution(1024, 1024)
                addAttachment(Attachments.depth())
                buildAttachments()
                getTexture(0).apply {
                    bind()
                    minFilter = GL_NEAREST
                    magFilter = GL_NEAREST
                }
            }
        }

        val box = BoxMesh { setSize(2f) }
        val plane = PlaneMesh { setSize(500f) }

        val cubeColor = Vec4(1f, 0.5f, 0f, 1f)

        val control = OrbitCameraControl {
            zenith = 1f
            azimuth = 0f
            target = Vec3(10f, 3f, 0f)
            targetDistance = 10f
        }

        // create screen quad for debugging
        val screenQuad = TextureRenderer()

        val cubesStartX = -100f
        val cubesEndX = 100f
        val cubesStepX = 20f

        val cubesStartZ = -100f
        val cubesEndZ = 100f
        val cubesStepZ = 20f

        val cubesY = 1f

        GL.render {
            control.update(APP.deltaTime)

            updateFrustums()

            // render to depth maps
            for (i in 0 until numCascades) {
                depthBuffers[i].render {
                    GL.glClear()
                    GL.cullFaceMode = GL_FRONT

                    val shader = depthRenderShader
                    shader.bind()
                    shader["viewProj"] = lightProj[i]

                    // render plane
//                    shader.set("pos", 0f, 0f, 0f)
//                    plane.render(shader)

                    // render cubes
                    var xi = cubesStartX
                    while (xi < cubesEndX) {
                        var zi = cubesStartZ
                        while (zi < cubesEndZ) {
                            shader.set("pos", xi, cubesY, zi)
                            box.render(shader)
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
                    box.render(shader)
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