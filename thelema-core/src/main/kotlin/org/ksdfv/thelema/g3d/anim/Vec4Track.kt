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

package org.ksdfv.thelema.g3d.anim

import org.ksdfv.thelema.math.IVec4

/** @author zeganstyl */
class Vec4Track(
    /** Use [AnimInterpolation] */
    var interpolation: Int = 0,

    /** Node index in [IAnim.nodes] */
    var nodeIndex: Int = 0
): IAnimTrack {
    override var times: MutableList<Float> = ArrayList()
    var values: MutableList<IVec4> = ArrayList()
    var inTangents: MutableList<IVec4> = ArrayList()
    var outTangents: MutableList<IVec4> = ArrayList()

    override var duration: Float = 0f
}