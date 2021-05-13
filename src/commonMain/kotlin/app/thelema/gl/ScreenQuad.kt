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

package app.thelema.gl

/** @author zeganstyl */
open class ScreenQuad(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 1f,
    height: Float = 1f,
    positionName: String = "POSITION",
    uvName: String = "UV"
): IScreenQuad {
    override val mesh: IMesh = Mesh {
        primitiveType = GL_TRIANGLE_FAN

        addVertexBuffer {
            addAttribute(2, positionName)
            addAttribute(2, uvName)
            initVertexBuffer(4) {
                putFloats(x + -width, y + -height,  0f, 0f)
                putFloats(x + width, y + -height,  1f, 0f)
                putFloats(x + width, y + height,  1f, 1f)
                putFloats(x + -width, y + height,  0f, 1f)
            }
        }
    }
}
