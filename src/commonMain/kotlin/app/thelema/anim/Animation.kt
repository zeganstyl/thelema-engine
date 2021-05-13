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

import app.thelema.ecs.ECS
import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent

/** @author zeganstyl */
class Animation: IAnimation {
    override var translationTracks: MutableList<Vec3Track> = ArrayList()
    override var rotationTracks: MutableList<Vec4Track> = ArrayList()
    override var scaleTracks: MutableList<Vec3Track> = ArrayList()
    override var actionTrack: ActionTrack = ActionTrack()
    override var extra: MutableMap<Int, IAnimTrack> = HashMap()
    override var duration: Float = 0f

    override var entityOrNull: IEntity? = null

    override val componentName: String
        get() = "Animation"

    override fun setComponent(other: IEntityComponent): IEntityComponent {
        if (other.componentName == componentName && this != other) {
            other as IAnimation
            translationTracks = other.translationTracks
            rotationTracks = other.rotationTracks
            scaleTracks = other.scaleTracks
            actionTrack = other.actionTrack
            extra.putAll(other.extra)
            duration = other.duration
        }

        return super.setComponent(other)
    }

    override fun destroy() {
        super.destroy()

        for (i in translationTracks.indices) {
            translationTracks[i].destroy()
        }

        for (i in rotationTracks.indices) {
            rotationTracks[i].destroy()
        }

        for (i in scaleTracks.indices) {
            scaleTracks[i].destroy()
        }

        actionTrack.destroy()

        translationTracks.clear()
        translationTracks = ArrayList()
        rotationTracks.clear()
        rotationTracks = ArrayList()
        scaleTracks.clear()
        scaleTracks = ArrayList()
        extra.clear()
        extra = HashMap()
        duration = 0f
    }
}