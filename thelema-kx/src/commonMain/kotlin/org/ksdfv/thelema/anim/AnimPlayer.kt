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

import org.ksdfv.thelema.g3d.node.ITransformNode
import org.ksdfv.thelema.math.*
import org.ksdfv.thelema.utils.Pool
import kotlin.math.abs

// TODO: implement https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#appendix-c-spline-interpolation

/** @author Xoppa, zeganstyl */
class AnimPlayer {
    private val animationPool = Pool { AnimationDesc() }
    private val vec3Pool: Pool<IVec3> = Pool { Vec3() }
    private val vec4Pool: Pool<IVec4> = Pool { Vec4() }

    var applying = false

    /** The animation currently playing. Do not alter this value.  */
    var current: AnimationDesc? = null
    /** The animation queued to be played when the [current] animation is completed. Do not alter this value.  */
    var queued: AnimationDesc? = null
    /** The transition time which should be applied to the queued animation. Do not alter this value.  */
    var queuedTransitionTime = 0f
    /** The animation which previously played. Do not alter this value.  */
    var previous: AnimationDesc? = null
    /** The current transition time. Do not alter this value.  */
    var transitionCurrentTime = 0f
    /** The target transition time. Do not alter this value.  */
    var transitionTargetTime = 0f
    /** Whether an action is being performed. Do not alter this value.  */
    var inAction: Boolean = false
    /** When true a call to [update] will not be processed.  */
    var paused: Boolean = false
    /** Whether to allow the same animation to be played while playing that animation.  */
    var allowSameAnimation: Boolean = false

    private var justChangedAnimation = false

    val blendingPosition = HashMap<ITransformNode, IVec3>()
    val blendingRotation = HashMap<ITransformNode, IVec4>()
    val blendingScale = HashMap<ITransformNode, IVec3>()
    var nodes: List<ITransformNode> = ArrayList()

    private fun obtain(anim: IAnim?, offset: Float, duration: Float, loopCount: Int, speed: Float, listener: AnimListener?): AnimationDesc? {
        if (anim == null) return null
        val result = animationPool.get()
        result.animation = anim
        result.listener = listener
        result.loopCount = loopCount
        result.speed = speed
        result.offset = offset
        result.duration = duration
        result.time = if (speed < 0) result.duration else 0f
        return result
    }

    private fun obtain(anim: AnimationDesc) = obtain(anim.animation, anim.offset, anim.duration, anim.loopCount, anim.speed, anim.listener)

    /** Update any animations currently being played.
     * @param delta The time elapsed since last update, change this to alter the overall speed (can be negative).
     */
    fun update(delta: Float) {
        if (paused) return

        transitionCurrentTime += delta

        val previous = previous
        val current = current

        if (previous != null && transitionCurrentTime >= transitionTargetTime) {
            justChangedAnimation = true
            animationPool.free(previous)
            this.previous = null
        }

        if (justChangedAnimation) {
            justChangedAnimation = false
        }

        if (current == null || current.loopCount == 0 || current.animation == null) return

        val remain = current.update(delta)
        if (remain != 0f && queued != null) {
            inAction = false
            animate(queued, queuedTransitionTime)
            queued = null
            update(remain)
            return
        }

        if (previous != null) {
            applyAnimations(previous.animation, previous.offset + previous.time, current.animation, current.offset + current.time,
                    transitionCurrentTime / transitionTargetTime)
        } else {
            applyAnimation(current.animation!!, current.offset + current.time)
        }

        val actionTrack = current.animation?.actionTrack
        if (actionTrack != null && actionTrack.actions.size > 0) {
            val times = actionTrack.times
            val actions = actionTrack.actions
            for (i in times.indices) {
                val time = times[i]
                if (time >= current.previousTime && time <= current.time) {
                    val action = actions[i]
                    current.executedActions.add(action)
                    action()
                }
            }
        }
    }

    /** Set the active animation, replacing any current animation.
     * @param offset The offset in seconds to the start of the animation.
     * @param duration The duration in seconds of the animation (or negative to play till the end of the animation).
     * @param loopCount The number of times to loop the animation, zero to play the animation only once, negative to continuously
     * loop the animation.
     * @param speed The speed at which the animation should be played. Default is 1.0f. A value of 2.0f will play the animation at
     * twice the normal speed, a value of 0.5f will play the animation at half the normal speed, etc. This value can be
     * negative, causing the animation to played in reverse. This value cannot be zero.
     * @param listener The [AnimListener] which will be informed when the animation is looped or completed.
     * @return The [AnimationDesc] which can be read to get the progress of the animation. Will be invalid when the animation
     * is completed.
     */
    fun setAnimation(
        animation: IAnim,
        loopCount: Int = 1,
        speed: Float = 1f,
        duration: Float = -1f,
        offset: Float = 0f,
        listener: AnimListener? = null
    ) = setAnimation(obtain(animation, offset, duration, loopCount, speed, listener))

    /** Set the active animation, replacing any current animation.  */
    fun setAnimation(anim: AnimationDesc?): AnimationDesc? {
        if (current == null)
            current = anim
        else {
            if (!allowSameAnimation && anim != null && current!!.animation === anim.animation)
                anim.time = current!!.time
            animationPool.free(current!!)
            current = anim
        }
        justChangedAnimation = true
        return anim
    }

    /** Changes the current animation by blending the new on top of the old during the transition time.
     * @param offset The offset in seconds to the start of the animation.
     * @param duration The duration in seconds of the animation (or negative to play till the end of the animation).
     * @param loopCount The number of times to loop the animation, zero to play the animation only once, negative to continuously
     * loop the animation.
     * @param speed The speed at which the animation should be played. Default is 1.0f. A value of 2.0f will play the animation at
     * twice the normal speed, a value of 0.5f will play the animation at half the normal speed, etc. This value can be
     * negative, causing the animation to played in reverse. This value cannot be zero.
     * @param listener The [AnimListener] which will be informed when the animation is looped or completed.
     * @param transitionTime The time to transition the new animation on top of the currently playing animation (if any).
     * @return The [AnimationDesc] which can be read to get the progress of the animation. Will be invalid when the animation
     * is completed.
     */
    fun animate(
        animation: IAnim,
        transitionTime: Float,
        offset: Float = 0f,
        duration: Float = -1f,
        loopCount: Int = 1,
        speed: Float = 1f,
        listener: AnimListener? = null
    ) = animate(obtain(animation, offset, duration, loopCount, speed, listener), transitionTime)

    /** Changes the current animation by blending the new on top of the old during the transition time.  */
    private fun animate(anim: AnimationDesc?, transitionTime: Float): AnimationDesc? {
        if (current == null)
            current = anim
        else if (inAction)
            queue(anim, transitionTime)
        else if (!allowSameAnimation && anim != null && current!!.animation === anim.animation) {
            anim.time = current!!.time
            animationPool.free(current!!)
            current = anim
        } else {
            if (previous != null) {
                animationPool.free(previous!!)
            }
            previous = current
            current = anim
            transitionCurrentTime = 0f
            transitionTargetTime = transitionTime
        }
        return anim
    }

    /** Queue an animation to be applied when the [current] animation is finished. If the current animation is continuously
     * looping it will be synchronized on next loop.
     * @param offset The offset in seconds to the start of the animation.
     * @param duration The duration in seconds of the animation (or negative to play till the end of the animation).
     * @param loopCount The number of times to loop the animation, zero to play the animation only once, negative to continuously
     * loop the animation.
     * @param speed The speed at which the animation should be played. Default is 1.0f. A value of 2.0f will play the animation at
     * twice the normal speed, a value of 0.5f will play the animation at half the normal speed, etc. This value can be
     * negative, causing the animation to played in reverse. This value cannot be zero.
     * @param listener The [AnimListener] which will be informed when the animation is looped or completed.
     * @param transitionTime The time to transition the new animation on top of the currently playing animation (if any).
     * @return The [AnimationDesc] which can be read to get the progress of the animation. Will be invalid when the animation
     * is completed.
     */
    fun queue(
        animation: IAnim,
        transitionTime: Float,
        offset: Float = 0f,
        duration: Float = -1f,
        loopCount: Int = 1,
        speed: Float = 1f,
        listener: AnimListener? = null
    ) = queue(obtain(animation, offset, duration, loopCount, speed, listener), transitionTime)

    /** Queue an animation to be applied when the current is finished. If current is continuous it will be synced on next loop.  */
    private fun queue(anim: AnimationDesc?, transitionTime: Float): AnimationDesc? {
        if (current == null || current!!.loopCount == 0)
            animate(anim, transitionTime)
        else {
            if (queued != null) animationPool.free(queued!!)
            queued = anim
            queuedTransitionTime = transitionTime
            if (current!!.loopCount < 0) current!!.loopCount = 1
        }
        return anim
    }

    /** Apply an action animation on top of the current animation.
     * @param offset The offset in seconds to the start of the animation.
     * @param duration The duration in seconds of the animation (or negative to play till the end of the animation).
     * @param loopCount The number of times to loop the animation, zero to play the animation only once, negative to continuously
     * loop the animation.
     * @param speed The speed at which the animation should be played. Default is 1.0f. A value of 2.0f will play the animation at
     * twice the normal speed, a value of 0.5f will play the animation at half the normal speed, etc. This value can be
     * negative, causing the animation to played in reverse. This value cannot be zero.
     * @param listener The [AnimListener] which will be informed when the animation is looped or completed.
     * @param transitionTime The time to transition the new animation on top of the currently playing animation (if any).
     * @return The [AnimationDesc] which can be read to get the progress of the animation. Will be invalid when the animation
     * is completed.
     */
    fun action(
        animation: IAnim,
        transitionTime: Float,
        offset: Float = 0f,
        duration: Float = -1f,
        loopCount: Int = 1,
        speed: Float = 1f,
        listener: AnimListener? = null
    ) = action(obtain(animation, offset, duration, loopCount, speed, listener)!!, transitionTime)

    /** Apply an action animation on top of the current animation.  */
    private fun action(anim: AnimationDesc, transitionTime: Float): AnimationDesc {
        if (anim.loopCount < 0) throw RuntimeException("An action cannot be continuous")
        if (current == null || current!!.loopCount == 0)
            animate(anim, transitionTime)
        else {
            val toQueue = if (inAction) null else obtain(current!!)
            inAction = false
            animate(anim, transitionTime)
            inAction = true
            if (toQueue != null) queue(toQueue, transitionTime)
        }
        return anim
    }



    /** Begin applying multiple animations to the instance, must followed by one or more calls to {
     * [apply] and finally {[end].  */
    private fun begin() {
        if (applying) throw RuntimeException("You must call end() after each call to being()")
        applying = true
    }

    /** Apply an animation, must be called between {[begin] and {[end].
     * @param weight The blend weight of this animation relative to the previous applied animations.
     */
    private fun apply(animation: IAnim, time: Float, weight: Float) {
        if (!applying) throw RuntimeException("You must call render() before adding an animation")
        applyAnimation(true, weight, animation, time)
    }

    /** End applying multiple animations to the instance and update it to reflect the changes.  */
    private fun end() {
        if (!applying) throw RuntimeException("You must call render() first")
        blendingPosition.entries.forEach {
            it.key.position.set(it.value)
            vec3Pool.free(it.value)
        }
        blendingPosition.clear()

        blendingRotation.entries.forEach {
            it.key.rotation.set(it.value)
            vec4Pool.free(it.value)
        }
        blendingRotation.clear()

        blendingScale.entries.forEach {
            it.key.scale.set(it.value)
            vec3Pool.free(it.value)
        }
        blendingScale.clear()

        applying = false
    }

    /** Apply a single animation to the Model and update the it to reflect the changes.  */
    private fun applyAnimation(animation: IAnim, time: Float) {
        if (applying) throw RuntimeException("Call end() first")
        applyAnimation(false, 1f, animation, time)
    }

    /** Apply two animations, blending the second onto to first using weight.  */
    private fun applyAnimations(anim1: IAnim?, time1: Float, anim2: IAnim?, time2: Float,
                                weight: Float) {
        if (anim2 == null || weight == 0f)
            applyAnimation(anim1!!, time1)
        else if (anim1 == null || weight == 1f)
            applyAnimation(anim2, time2)
        else if (applying)
            throw RuntimeException("Call end() first")
        else {
            begin()
            apply(anim1, time1, 1f)
            apply(anim2, time2, weight)
            end()
        }
    }

    /** Helper method to apply one animation to either an objectmap for blending or directly to the bones.  */
    private fun applyAnimation(blending: Boolean, alpha: Float, animation: IAnim, time: Float) {
        val translationTracks = animation.translationTracks
        val rotationTracks = animation.rotationTracks
        val scaleTracks = animation.scaleTracks

        if (blending) {
            // apply blending
            for (i in translationTracks.indices) {
                val track = translationTracks[i]
                val node = nodes[track.nodeIndex]
                val target = tmpPosition
                getTranslationAtTime(track, time, target)

                val t = blendingPosition[node]
                if (t != null) {
                    if (alpha > 0.999999f) {
                        t.set(target)
                    } else {
                        t.lerp(target, alpha)
                    }
                } else {
                    if (alpha > 0.999999f) {
                        blendingPosition[node] = vec3Pool.get().set(target)
                    } else {
                        blendingPosition[node] = vec3Pool.get().apply {
                            set(node.position)
                            lerp(target, alpha)
                        }
                    }
                }
            }

            for (i in rotationTracks.indices) {
                val track = rotationTracks[i]
                val node = nodes[track.nodeIndex]
                val target = tmpRotation
                getRotationAtTime(track, time, target)

                val t = blendingRotation[node]
                if (t != null) {
                    if (alpha > 0.999999f)
                        t.set(target)
                    else {
                        t.lerp(target, alpha)
                    }
                } else {
                    if (alpha > 0.999999f) {
                        blendingRotation[node] = vec4Pool.get().set(target)
                    } else {
                        blendingRotation[node] = vec4Pool.get().apply {
                            set(node.rotation)
                            lerp(target, alpha)
                        }
                    }
                }
            }

            for (i in scaleTracks.indices) {
                val track = scaleTracks[i]
                val node = nodes[track.nodeIndex]
                val target = tmpScale
                getScalingAtTime(track, time, target)

                val t = blendingScale[node]
                if (t != null) {
                    if (alpha > 0.999999f) {
                        t.set(target)
                    } else {
                        t.lerp(target, alpha)
                    }
                } else {
                    if (alpha > 0.999999f) {
                        blendingScale[node] = vec3Pool.get().set(target)
                    } else {
                        blendingScale[node] = vec3Pool.get().apply {
                            set(node.scale)
                            lerp(target, alpha)
                        }
                    }
                }
            }
        } else {
            // apply directly
            val nodes = nodes
            for (i in translationTracks.indices) {
                val track = translationTracks[i]
                getTranslationAtTime(track, time, nodes[track.nodeIndex].position)
            }
            for (i in translationTracks.indices) {
                val track = rotationTracks[i]
                getRotationAtTime(track, time, nodes[track.nodeIndex].rotation)
            }
            for (i in translationTracks.indices) {
                val track = scaleTracks[i]
                getScalingAtTime(track, time, nodes[track.nodeIndex].scale)
            }
        }
    }

    private val tmpPosition = Vec3()
    private val tmpRotation = Vec4()
    private val tmpScale = Vec3()

    /** Find first key frame index just before a given time
     * @param arr Key frames ordered by time ascending
     * @param time Time to search
     * @return key frame index, 0 if time is out of key frames time range
     */
    private fun getFirstKeyframeIndexAtTime(arr: List<Float>, time: Float): Int {
        val lastIndex = arr.size - 1

        // edges cases : time out of range always return first index
        if (lastIndex <= 0 || time < arr[0] || time > arr[lastIndex]) {
            return 0
        }

        // binary search
        var minIndex = 0
        var maxIndex = lastIndex

        while (minIndex < maxIndex) {
            val i = (minIndex + maxIndex) / 2
            when {
                time > arr[i + 1] -> minIndex = i + 1
                time < arr[i] -> maxIndex = i - 1
                else -> return i
            }
        }
        return minIndex
    }

//    private fun <T> getFirstKeyframeIndexAtTime(arr: ArrayEx<NodeKeyframeEx<T>>, time: Float): Int {
//        val n = arr.size - 1
//        for (i in 0 until n) {
//            if (time >= arr[i].keytime && time <= arr[i + 1].keytime) {
//                return i
//            }
//        }
//        return 0
//    }

    private fun getTranslationAtTime(track: Vec3Track, time: Float, out: IVec3): IVec3 {
        val values = track.values

        if (values.size == 1) return out.set(values[0])

        var index = getFirstKeyframeIndexAtTime(track.times, time)
        val value1 = values[index]
        val time1 = track.times[index]
        out.set(value1)

        if (++index < values.size) {
            val value2 = track.values[index]
            val time2 = track.times[index]
            val t = (time - time1) / (time2 - time1)
            out.lerp(value2, t)
        }

        return out
    }

    private fun getRotationAtTime(track: Vec4Track, time: Float, out: IVec4): IVec4 {
        val frames = track.values
        if (frames.size == 1) return out.set(frames[0])

        var index = getFirstKeyframeIndexAtTime(track.times, time)
        val value1 = frames[index]
        val time1 = track.times[index]
        out.set(value1)

        if (++index < frames.size) {
            val value2 = frames[index]
            val time2 = track.times[index]
            val t = (time - time1) / (time2 - time1)
            out.slerp(value2, t)
        }

        return out
    }

    private fun getScalingAtTime(track: Vec3Track, time: Float, out: IVec3): IVec3 {
        val scaling = track.values
        if (scaling.size == 1) return out.set(scaling[0])

        var index = getFirstKeyframeIndexAtTime(track.times, time)
        val value1 = scaling[index]
        val time1 = track.times[index]
        out.set(value1)

        if (++index < scaling.size) {
            val value2 = scaling[index]
            val time2 = track.times[index]
            val t = (time - time1) / (time2 - time1)
            out.lerp(value2, t)
        }

        return out
    }

    /** Class describing how to play animation. You can read the values within this class to get the progress of the
     * animation. Do not change the values. Only valid when the animation is currently played.
     * @author Xoppa
     */
    class AnimationDesc {
        /** Listener which will be informed when the animation is looped or ended.  */
        var listener: AnimListener? = null
        /** The animation to be applied.  */
        var animation: IAnim? = null
        /** The speed at which to play the animation (can be negative), 1.0 for normal speed.  */
        var speed = 0f
        /** The current animation time.  */
        var time = 0f
        /** The offset within the animation (animation time = offsetTime + time)  */
        var offset = 0f
        /** The duration of the animation  */
        var duration = 0f
            get() {
                return if (field < 0f) {
                    val anim = animation
                    if (anim != null) anim.duration - offset else 0f
                } else field
            }
        /** The number of remaining loops, negative for continuous, zero if stopped.  */
        var loopCount = 0

        var previousTime: Float = 0f

        val executedActions = HashSet<() -> Unit>()

        /** @return the remaining time or 0 if still animating.
         */
        fun update(delta: Float): Float {
            previousTime = time

            if (loopCount != 0 && animation != null) {
                var loops: Int
                val diff = speed * delta

                val duration = duration
                if (MATH.isNotZero(duration)) {
                    time += diff
                    loops = abs(time / duration).toInt()
                    if (time < 0f) {
                        loops++
                        while (time < 0f) {
                            time += duration
                        }
                    }
                    time = abs(time % duration)
                } else {
                    loops = 1
                }

                for (i in 0 until loops) {
                    if (loopCount > 0) loopCount--
                    val listener = listener
                    if (loopCount != 0 && listener != null) listener.onLoop(this)
                    if (loopCount == 0) {
                        val result = (loops - 1 - i) * duration + if (diff < 0f) duration - time else time
                        time = if (diff < 0f) 0f else duration
                        listener?.onEnd(this)
                        return result
                    }
                }

                return 0f
            } else
                return delta
        }
    }
}