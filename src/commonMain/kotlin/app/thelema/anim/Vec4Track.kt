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

package app.thelema.anim

import app.thelema.math.IVec4

/** @author zeganstyl */
class Vec4Track(
    /** Use [AnimInterpolation] */
    var interpolation: Int = 0,

    /** Node index in [AnimationPlayer.nodes] */
    var nodeIndex: Int = 0,
    override var duration: Float = 0f
): IAnimTrack {
    override var times: MutableList<Float> = ArrayList()
    var values: MutableList<IVec4> = ArrayList()
    var inTangents: MutableList<IVec4> = ArrayList()
    var outTangents: MutableList<IVec4> = ArrayList()

    fun getValueAtTime(time: Float, out: IVec4): IVec4 {
        val frames = values
        if (frames.size == 1) return out.set(frames[0])

        var index = getFirstKeyframeIndexAtTime(time)
        val value1 = frames[index]
        val time1 = times[index]
        out.set(value1)

        if (++index < frames.size) {
            val value2 = frames[index]
            val time2 = times[index]
            val t = (time - time1) / (time2 - time1)
            out.slerp(value2, t)
        }

        return out
    }

    override fun destroy() {
        super.destroy()

        times.clear()
        times = ArrayList()
        values.clear()
        values = ArrayList()
        inTangents.clear()
        inTangents = ArrayList()
        outTangents.clear()
        outTangents = ArrayList()
    }
}
