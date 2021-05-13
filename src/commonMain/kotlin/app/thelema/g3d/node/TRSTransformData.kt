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

package app.thelema.g3d.node

import app.thelema.math.*

class TRSTransformData: ITransformData {
    override val position: IVec3 = Vec3(0f, 0f, 0f)
    override val rotation: IVec4 = Vec4(0f, 0f, 0f, 1f)
    override val scale: IVec3 = Vec3(1f, 1f, 1f)
    override val worldMatrix: IMat4 = Mat4()
    override var previousWorldMatrix: IMat4? = null
}
