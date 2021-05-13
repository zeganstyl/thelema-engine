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

package app.thelema.action

import app.thelema.anim.AnimationAction
import app.thelema.anim.AnimationPlayer
import app.thelema.g3d.node.ITransformNode
import app.thelema.math.IVec3
import app.thelema.math.Vec3
import app.thelema.phys.PhysicsContext
import kotlin.math.abs

class MoveForwardAction: ActionAdapter() {
    var length: Float = 0f

    var passed: Float = 0f

    private val tmp: IVec3 = Vec3()

    override val componentName: String
        get() = "MoveToTargetAction"

    override var actionData: ActionData = ActionData(0f, null, null)

    var animationTransition: Float = 0.1f
    var animationAction: AnimationAction? = null

    override fun restart() {
        super.restart()
        passed = 0f
    }

    override fun update(delta: Float): Float {
        if (isRunning) {
            val node = getContextComponent<ITransformNode>()
            val physicsContext = getContextComponent<PhysicsContext>()
            if (node != null && physicsContext != null) {
                node.rotation.rotateVec3(tmp.set(0f, 0f, 1f))

                val speed = physicsContext.linearVelocity * delta
                val diff = length - passed
                if (abs(diff) < speed) {
                    node.position.add(tmp.scl(diff))
                    passed += diff
                    isRunning = false
                } else {
                    node.position.add(tmp.scl(speed))
                    passed += speed
                }
                node.requestTransformUpdate(true)

                val animation = physicsContext.moveAnim
                val animationAction = animationAction
                if (animation != null && animationAction == null) {
                    val animationPlayer = getContextComponent<AnimationPlayer>()
                    if (animationPlayer != null) {
                        this.animationAction = animationPlayer.animate(animation, animationTransition, loopCount = -1)
                    }
                }
            } else {
                isRunning = false
            }

            if (animationAction != null && !isRunning) {
                animationAction?.end()
                animationAction = null

                getContextComponent<AnimationPlayer>()?.also { player ->
                    physicsContext?.idleAnim?.also { player.animate(it, animationTransition, loopCount = -1) }
                }
            }
        }
        return 0f
    }
}