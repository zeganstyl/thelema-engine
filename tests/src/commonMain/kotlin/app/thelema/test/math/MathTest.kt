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

package app.thelema.test.math

import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.math.Vec4
import app.thelema.test.Test

class MathTest: Test {
    override val name: String
        get() = "Math"

    override fun testMain() {
        run {
            val vec = Vec4()
            vec.setQuaternionByAxis(0f, 1f, 0f, 1f)
            if (
                !check("vec4.setQuaternionByAxis",
                    isEqual(vec.x, 0f),
                    isEqual(vec.y, 0.47942555f),
                    isEqual(vec.z, 0f),
                    isEqual(vec.w, 0.87758255f)
                )
            ) {
                println("Expected:")
                println("(0.0, 0.47942555, 0.0, 0.87758255)")
                println("Actual:")
                println(vec.toString())
            }
        }
        run {
            val vec = Vec4(0f, 0.47942555f, 0f, 0.87758255f)
            val vec2 = Vec3(1f, 0f, 0f)
            vec.rotateVec3(vec2)
            if (
                !check("vec4.rotateVec3",
                    isEqual(vec2.x, 0.5403023f),
                    isEqual(vec2.y, 0.0f),
                    isEqual(vec2.z, -0.841471f)
                )
            ) {
                println("Expected:")
                println("(0.5403023, 0.0, -0.841471)")
                println("Actual:")
                println(vec2.toString())
            }
        }

        run {
            val mat = Mat4()
            mat.translate(1f, 2f, 3f)
            check("mat4.translate", mat.m03 == 1f && mat.m13 == 2f && mat.m23 == 3f)
        }
        run {
            val mat = Mat4()
            mat.rotate(1f, 0.5f, 0f, 1f)
            check("mat4.rotate",
                isEqual(mat.m00, 0.90806043f),
                isEqual(mat.m01, 0.18387909f),
                isEqual(mat.m02, 0.37631726f),

                isEqual(mat.m10, 0.18387909f),
                isEqual(mat.m11, 0.63224185f),
                isEqual(mat.m12, -0.7526345f),

                isEqual(mat.m20, -0.37631726f),
                isEqual(mat.m21, 0.7526345f),
                isEqual(mat.m22, 0.5403023f)
            )
        }
        run {
            val mat = Mat4()
            mat.setToLook(Vec3(5f, 1f, 2f), Vec3(0f, -1f, 0f), Vec3(1f, 0f, 0f))
            check("mat4.setToLook",
                isEqual(mat.m00, 0f),
                isEqual(mat.m01, 0f),
                isEqual(mat.m02, 1f),

                isEqual(mat.m10, 1f),
                isEqual(mat.m11, 0f),
                isEqual(mat.m12, 0f),

                isEqual(mat.m20, 0f),
                isEqual(mat.m21, 1f),
                isEqual(mat.m22, 0f),

                isEqual(mat.m03, -2f),
                isEqual(mat.m13, -5f),
                isEqual(mat.m23, -1f)
            )
        }
        run {
            val mat = Mat4()
            mat.setToLookAt(Vec3(5f, 1f, 2f), Vec3(1f, -1f, 0f).nor(), Vec3(1f, 0f, 1f).nor())
            mat.inv()
            if (!check("mat4.inv",
                isEqual(mat.m00, -0.5773503f),
                isEqual(mat.m01, 0.4082483f),
                isEqual(mat.m02, -0.70710677f),
                isEqual(mat.m03, 5f),

                isEqual(mat.m10, -0.5773503f),
                isEqual(mat.m11, 0.4082483f),
                isEqual(mat.m12, 0.70710677f),
                isEqual(mat.m13, 1f),

                isEqual(mat.m20, 0.5773503f),
                isEqual(mat.m21, 0.8164966f),
                isEqual(mat.m22, 0f),
                isEqual(mat.m23, 2f),

                isEqual(mat.m30, 0f),
                isEqual(mat.m31, 0f),
                isEqual(mat.m32, 0f),
                isEqual(mat.m33, 1f)
            )) {
                println("Expected:")
                println("""[-0.5773503|0.4082483|-0.70710677|5.0]
[-0.5773503|0.4082483|0.70710677|1.0]
[0.5773503|0.8164966|0.0|2.0]
[0.0|0.0|0.0|1.0]""")
                println("Actual:")
                println(mat.toString())
            }
        }
    }
}
