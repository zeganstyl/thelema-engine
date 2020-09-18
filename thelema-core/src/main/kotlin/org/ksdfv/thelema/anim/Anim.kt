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
class Anim: IAnim {
    override var translationTracks: MutableList<Vec3Track> = ArrayList()
    override var rotationTracks: MutableList<Vec4Track> = ArrayList()
    override var scaleTracks: MutableList<Vec3Track> = ArrayList()
    override var actionTrack: ActionTrack = ActionTrack()
    override var extra: MutableMap<Int, IAnimTrack> = HashMap()
    override var duration: Float = 0f
    override var name: String = ""

    override fun copy(): IAnim {
        val newAnim = Anim()
        newAnim.translationTracks = translationTracks
        newAnim.duration = duration
        newAnim.name = name
        return newAnim
    }
}