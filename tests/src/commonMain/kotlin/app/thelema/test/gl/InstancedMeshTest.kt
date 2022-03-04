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

package app.thelema.test.gl

import app.thelema.ecs.mainEntity
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.material
import app.thelema.g3d.mesh.boxMesh
import app.thelema.g3d.mesh.planeMesh
import app.thelema.gl.Vertex
import app.thelema.gl.instancedMesh
import app.thelema.gl.meshInstance
import app.thelema.shader.Shader
import app.thelema.test.Test

/** @author zeganstyl */
class InstancedMeshTest: Test {

    override fun testMain() = mainEntity {
        ActiveCamera {
            setNearFar(0.1f, 1000f)
        }

        orbitCameraControl()

        entity("plane") {
            planeMesh { setSize(1000f) }
            meshInstance()
        }

        val box = entity("box").boxMesh { setSize(2f) }

        entity("instances") {
            instancedMesh {
                mesh = box.mesh

                val stepX = 5f
                val numX = 200
                val startX = -numX * stepX * 0.5f

                val stepZ = 5f
                val numZ = 200
                val startZ = -numZ * stepZ * 0.5f

                val cubesY = 1f

                instancesCount = numX * numZ

                addVertexBuffer(instancesCount, Vertex.INSTANCE_POSITION) {
                    // fill buffer with positions
                    var xi = startX
                    val endX = startX + stepX * numX
                    while (xi < endX) {

                        var zi = startZ
                        val endZ = startZ + stepZ * numZ
                        while (zi < endZ) {
                            putFloats(xi, cubesY, zi)
                            zi += stepZ
                        }

                        xi += stepX
                    }
                }
            }
            material {
                shader = Shader(
                    vertCode = """
in vec3 POSITION;
in vec2 TEXCOORD_0;
in vec3 INSTANCE_POSITION;
uniform mat4 viewProj;

out vec2 uv;

void main() {
    uv = TEXCOORD_0;
    gl_Position = viewProj * vec4(POSITION + INSTANCE_POSITION, 1.0);
}""",
                    fragCode = """
in vec2 uv;
out vec4 FragColor;

void main() {
    FragColor = vec4(uv, 1.0, 1.0);
}""")

                shader?.onBind {
                    this["viewProj"] = ActiveCamera.viewProjectionMatrix
                }
            }
        }
    }
}
