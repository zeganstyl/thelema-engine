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

import app.thelema.img.ITexture
import app.thelema.img.TextureCube
import app.thelema.math.IVec3
import app.thelema.math.IVec4
import app.thelema.math.Vec3
import app.thelema.math.Vec4

/** @author zeganstyl */
class World: IWorld {
    override var fogColor: IVec4 = Vec4(0.5f, 0.5f, 0.5f, 1f)
    override var fogHeight: Float = 50f
    override var fogMul: Float = 0.001f

    override var ambientColor: IVec3 = Vec3(0.03f)

    override var exposure: Float = 1f

    override var environmentPrefilterMap: TextureCube? = null
    override var environmentPrefilterMipCount: Int = 0
    override var environmentIrradianceMap: TextureCube? = null
    override var brdfLUTMap: ITexture? = null
}