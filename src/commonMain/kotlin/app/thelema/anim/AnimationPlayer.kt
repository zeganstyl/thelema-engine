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

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.ecs.mainLoopOnUpdate
import app.thelema.g3d.ITransformNode
import app.thelema.math.*
import app.thelema.utils.LOG
import app.thelema.utils.Pool

// TODO: implement https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#appendix-c-spline-interpolation

/** @author Xoppa, zeganstyl */
class AnimationPlayer: IEntityComponent {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            value?.mainLoopOnUpdate { update(it) }
        }

    override val componentName: String
        get() = "AnimationPlayer"

    private val animationPool = Pool { AnimationAction() }
    private val vec3Pool: Pool<IVec3> = Pool { Vec3() }
    private val vec4Pool: Pool<IVec4> = Pool { Vec4() }

    var applying = false

    /** The animation currently playing. Do not alter this value.  */
    var current: AnimationAction? = null
    /** The animation queued to be played when the [current] animation is completed. Do not alter this value.  */
    var queued: AnimationAction? = null
    /** The transition time which should be applied to the queued animation. Do not alter this value.  */
    var queuedTransitionTime = 0f
    /** The animation which previously played. Do not alter this value.  */
    var previous: AnimationAction? = null
    /** The current transition time. Do not alter this value.  */
    var transitionCurrentTime = 0f
    /** The target transition time. Do not alter this value.  */
    var transitionTargetTime = 0f
    /** Whether an action is being performed. Do not alter this value.  */
    var inAction: Boolean = false
    /** When true a call to [update] will not be processed.  */
    var paused: Boolean = false
    /** Whether to allow the same animation to be played while playing that animation.  */
    var allowSameAnimation: Boolean = true

    private var justChangedAnimation = false

    val blendingPosition = HashMap<ITransformNode, IVec3>()
    val blendingRotation = HashMap<ITransformNode, IVec4>()
    val blendingScale = HashMap<ITransformNode, IVec3>()
    val nodes: MutableList<ITransformNode> = ArrayList()
    val animations: MutableList<IAnimation> = ArrayList()

    override fun setComponent(other: IEntityComponent): IEntityComponent {
        if (other is AnimationPlayer && other != this) {
            nodes.clear()
            nodes.addAll(other.nodes)

            for (i in other.nodes.indices) {
                val it = other.nodes[i]
                val path = other.entity.getRelativePathTo((it as IEntityComponent).entity)
                val node = entity.getEntityByPath(path)?.componentOrNull("TransformNode")
                if (node != null) {
                    nodes[i] = node as ITransformNode
                } else {
                    LOG.error("AnimPlayer: can't link node ${it.entityOrNull?.name} by path: $path")
                }
            }

            animations.clear()
            animations.addAll(other.animations)
        }
        return this
    }

    private fun obtain(animation: IAnimation, offset: Float, duration: Float, loopCount: Int, speed: Float): AnimationAction {
        val result = animationPool.get()
        result.animation = animation
        result.loopCount = loopCount
        result.speed = speed
        result.offset = offset
        result.duration = duration
        result.time = if (speed < 0) result.duration else 0f
        return result
    }

    private fun obtain(animation: AnimationAction) = obtain(animation.animation!!, animation.offset, animation.duration, animation.loopCount, animation.speed)

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

        if (current != null) {
            val animName = current.animName
            if (current.animation == null && animName != null) {
                current.animation = getAnimationOrNull(animName)
            }
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
                    //current.executedActions.add(action)
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
     * is completed.
     */
    fun setAnimation(
        animation: IAnimation,
        loopCount: Int = -1,
        speed: Float = 1f,
        duration: Float = -1f,
        offset: Float = 0f
    ) = setAnimation(obtain(animation, offset, duration, loopCount, speed))

    fun getAnimation(name: String): IAnimation =
        animations.firstOrNull { it.entityOrNull?.name == name } ?: throw IllegalArgumentException("AnimationPlayer: animation \"$name\" is not found")

    fun getAnimationOrNull(name: String) =
        animations.firstOrNull { it.entityOrNull?.name == name }

    fun setAnimation(
        name: String,
        loopCount: Int = -1,
        speed: Float = 1f,
        duration: Float = -1f,
        offset: Float = 0f
    ): AnimationAction = setAnimation(obtain(getAnimation(name), offset, duration, loopCount, speed))

    /** Set the active animation, replacing any current animation.  */
    fun setAnimation(animation: AnimationAction): AnimationAction {
        if (current == null)
            current = animation
        else {
            if (!allowSameAnimation && current!!.animation === animation.animation)
                animation.time = current!!.time
            animationPool.free(current!!)
            current = animation
        }
        justChangedAnimation = true
        return animation
    }

    /** Changes the current animation by blending the new on top of the old during the transition time.
     * @param offset The offset in seconds to the start of the animation.
     * @param duration The duration in seconds of the animation (or negative to play till the end of the animation).
     * @param loopCount The number of times to loop the animation, zero to play the animation only once, negative to continuously
     * loop the animation.
     * @param speed The speed at which the animation should be played. Default is 1.0f. A value of 2.0f will play the animation at
     * twice the normal speed, a value of 0.5f will play the animation at half the normal speed, etc. This value can be
     * negative, causing the animation to played in reverse. This value cannot be zero.
     * @param transitionTime The time to transition the new animation on top of the currently playing animation (if any).
     * is completed.
     */
    fun animate(
        animation: IAnimation,
        transitionTime: Float = 0.1f,
        offset: Float = 0f,
        duration: Float = -1f,
        loopCount: Int = 1,
        speed: Float = 1f
    ) = animate(obtain(animation, offset, duration, loopCount, speed), transitionTime)

    /** Changes the current animation by blending the new on top of the old during the transition time.  */
    private fun animate(animation: AnimationAction?, transitionTime: Float): AnimationAction? {
        if (current == null)
            current = animation
        else if (inAction)
            queue(animation, transitionTime)
        else if (!allowSameAnimation && animation != null && current!!.animation === animation.animation) {
            animation.time = current!!.time
            animationPool.free(current!!)
            current = animation
        } else {
            if (previous != null) {
                animationPool.free(previous!!)
            }
            previous = current
            current = animation
            transitionCurrentTime = 0f
            transitionTargetTime = transitionTime
        }
        return animation
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
     * @param transitionTime The time to transition the new animation on top of the currently playing animation (if any).
     * is completed.
     */
    fun queue(
        animation: IAnimation,
        transitionTime: Float = 0.1f,
        offset: Float = 0f,
        duration: Float = -1f,
        loopCount: Int = 1,
        speed: Float = 1f
    ) = queue(obtain(animation, offset, duration, loopCount, speed), transitionTime)

    /** Queue an animation to be applied when the current is finished. If current is continuous it will be synced on next loop.  */
    private fun queue(animation: AnimationAction?, transitionTime: Float): AnimationAction? {
        if (current == null || current!!.loopCount == 0)
            animate(animation, transitionTime)
        else {
            if (queued != null) animationPool.free(queued!!)
            queued = animation
            queuedTransitionTime = transitionTime
            if (current!!.loopCount < 0) current!!.loopCount = 1
        }
        return animation
    }

    /** Apply an action animation on top of the current animation.
     * @param offset The offset in seconds to the start of the animation.
     * @param duration The duration in seconds of the animation (or negative to play till the end of the animation).
     * @param loopCount The number of times to loop the animation, zero to play the animation only once, negative to continuously
     * loop the animation.
     * @param speed The speed at which the animation should be played. Default is 1.0f. A value of 2.0f will play the animation at
     * twice the normal speed, a value of 0.5f will play the animation at half the normal speed, etc. This value can be
     * negative, causing the animation to played in reverse. This value cannot be zero.
     * @param transitionTime The time to transition the new animation on top of the currently playing animation (if any).
     * is completed.
     */
    fun action(
        animation: IAnimation,
        transitionTime: Float = 0.1f,
        offset: Float = 0f,
        duration: Float = -1f,
        loopCount: Int = 1,
        speed: Float = 1f
    ) = action(obtain(animation, offset, duration, loopCount, speed), transitionTime)

    fun action(
        name: String,
        transitionTime: Float = 0.1f,
        offset: Float = 0f,
        duration: Float = -1f,
        loopCount: Int = 1,
        speed: Float = 1f
    ) = action(getAnimation(name), transitionTime, offset, duration, loopCount, speed)

    /** Apply an action animation on top of the current animation.  */
    private fun action(animation: AnimationAction, transitionTime: Float): AnimationAction {
        if (animation.loopCount < 0) throw RuntimeException("An action cannot be continuous")
        if (current == null || current!!.loopCount == 0)
            animate(animation, transitionTime)
        else {
            val toQueue = if (inAction) null else obtain(current!!)
            inAction = false
            animate(animation, transitionTime)
            inAction = true
            if (toQueue != null) queue(toQueue, transitionTime)
        }
        return animation
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
    private fun apply(animation: IAnimation, time: Float, weight: Float) {
        if (!applying) throw RuntimeException("You must call render() before adding an animation")
        applyAnimation(true, weight, animation, time)
    }

    /** End applying multiple animations to the instance and update it to reflect the changes.  */
    private fun end() {
        if (!applying) throw RuntimeException("You must call render() first")
        blendingPosition.entries.forEach {
            it.key.position = it.value
            vec3Pool.free(it.value)
        }
        blendingPosition.clear()

        blendingRotation.entries.forEach {
            it.key.setRotation(it.value.x, it.value.y, it.value.z, it.value.w)
            vec4Pool.free(it.value)
        }
        blendingRotation.clear()

        blendingScale.entries.forEach {
            it.key.scale = it.value
            vec3Pool.free(it.value)
        }
        blendingScale.clear()

        applying = false
    }

    /** Apply a single animation to the Model and update the it to reflect the changes.  */
    private fun applyAnimation(animation: IAnimation, time: Float) {
        if (applying) throw RuntimeException("Call end() first")
        applyAnimation(false, 1f, animation, time)
    }

    /** Apply two animations, blending the second onto to first using weight.  */
    private fun applyAnimations(animation1: IAnimation?, time1: Float, animation2: IAnimation?, time2: Float,
                                weight: Float) {
        if (animation2 == null || weight == 0f)
            applyAnimation(animation1!!, time1)
        else if (animation1 == null || weight == 1f)
            applyAnimation(animation2, time2)
        else if (applying)
            throw RuntimeException("Call end() first")
        else {
            begin()
            apply(animation1, time1, 1f)
            apply(animation2, time2, weight)
            end()
        }
    }

    /** Helper method to apply one animation to either an objectmap for blending or directly to the bones.  */
    private fun applyAnimation(blending: Boolean, alpha: Float, animation: IAnimation, time: Float) {
        val translationTracks = animation.translationTracks
        val rotationTracks = animation.rotationTracks
        val scaleTracks = animation.scaleTracks

        if (blending) {
            // apply blending
            for (i in translationTracks.indices) {
                val track = translationTracks[i]
                val node = nodes[track.nodeIndex]
                val target = tmpPosition
                track.getValueAtTime(time, target)

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
                track.getValueAtTime(time, target)

                val t = blendingRotation[node]
                if (t != null) {
                    if (alpha > 0.999999f) {
                        t.set(target)
                    } else {
                        t.slerp(target, alpha)
                    }
                } else {
                    if (alpha > 0.999999f) {
                        blendingRotation[node] = vec4Pool.get().set(target)
                    } else {
                        val vec = vec4Pool.get()
                        vec.setQuaternion(node.rotation)
                        vec.slerp(target, alpha)
                        blendingRotation[node] = vec
                    }
                }
            }

            for (i in scaleTracks.indices) {
                val track = scaleTracks[i]
                val node = nodes[track.nodeIndex]
                val target = tmpScale
                track.getValueAtTime(time, target)

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
            for (i in translationTracks.indices) {
                val track = translationTracks[i]
                try {
                    track.getValueAtTime(time, tmpPosition)
                    nodes[track.nodeIndex].position = tmpPosition
                } catch (ex: Exception) {
                    LOG.info("expected ${track.nodeIndex}, but size ${nodes.size}")
                }
            }
            for (i in rotationTracks.indices) {
                val track = rotationTracks[i]
                track.getValueAtTime(time, tmpRotation)
                nodes[track.nodeIndex].setRotation(tmpRotation.x, tmpRotation.y, tmpRotation.z, tmpRotation.w)
            }
            for (i in scaleTracks.indices) {
                val track = scaleTracks[i]
                track.getValueAtTime(time, tmpScale)
                nodes[track.nodeIndex].scale = tmpScale
            }
        }

        for (i in nodes.indices) {
            nodes[i].requestTransformUpdate(true)
        }
    }

    private val tmpPosition = vec3()
    private val tmpRotation = vec4()
    private val tmpScale = vec3()
}

fun IEntity.animationPlayer(block: AnimationPlayer.() -> Unit) = component(block)
fun IEntity.animationPlayer() = component<AnimationPlayer>()