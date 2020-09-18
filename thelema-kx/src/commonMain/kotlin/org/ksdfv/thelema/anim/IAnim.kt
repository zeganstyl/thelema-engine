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

package org.ksdfv.thelema.anim

/** @author zeganstyl */
interface IAnim {
    var name: String

    var translationTracks: MutableList<Vec3Track>
    var rotationTracks: MutableList<Vec4Track>
    var scaleTracks: MutableList<Vec3Track>
    var actionTrack: ActionTrack

    /** Additional tracks */
    var extra: MutableMap<Int, IAnimTrack>

    var duration: Float

    fun copy(): IAnim

    fun calculateDuration() {
        var duration = 0f

        translationTracks.forEach { if (it.duration > duration) duration = it.duration }
        rotationTracks.forEach { if (it.duration > duration) duration = it.duration }
        scaleTracks.forEach { if (it.duration > duration) duration = it.duration }
        if (actionTrack.duration > duration) duration = actionTrack.duration
        extra.values.forEach { if (it.duration > duration) duration = it.duration }

        this.duration = duration
    }

    fun clear() {
        extra = HashMap()
        duration = 0f
    }
}
