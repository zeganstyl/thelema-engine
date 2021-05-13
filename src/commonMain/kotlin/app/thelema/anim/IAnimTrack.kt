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

/** @author zeganstyl */
interface IAnimTrack {
    var times: MutableList<Float>

    var duration: Float

    fun calculateDuration(): Float {
        var duration = 0f

        val keyTimes = times
        for (i in keyTimes.indices) {
            val time = keyTimes[i]
            if (time > duration) duration = time
        }

        this.duration = duration

        return duration
    }

    /** Find first key frame index just before a given time
     * @param time Time to search
     * @return key frame index, 0 if time is out of key frames time range
     */
    fun getFirstKeyframeIndexAtTime(time: Float): Int {
        val lastIndex = times.size - 1

        // edges cases : time out of range always return first index
        if (lastIndex <= 0 || time < times[0] || time > times[lastIndex]) {
            return 0
        }

        // binary search
        var minIndex = 0
        var maxIndex = lastIndex

        while (minIndex < maxIndex) {
            val i = (minIndex + maxIndex) / 2
            when {
                time > times[i + 1] -> minIndex = i + 1
                time < times[i] -> maxIndex = i - 1
                else -> return i
            }
        }
        return minIndex
    }

    fun destroy() {
        times.clear()
    }
}