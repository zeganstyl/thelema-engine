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
import app.thelema.ecs.ECS
import app.thelema.g3d.node.ITransformNode
import app.thelema.phys.PhysicsContext
import kotlin.math.abs

class RotateYAction: ActionAdapter() {
    override val componentName: String
        get() = "RotateYAction"

    override var actionData: ActionData = ActionData()

    /** In radians */
    var angleLength: Float = 0f

    var passed: Float = 0f

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
                val speed = physicsContext.angularVelocity * (if (angleLength < 0f) -delta else delta)
                val diff = angleLength - passed
                if (abs(diff) < abs(speed)) {
                    node.rotation.setQuaternionByAxis(0f, 1f, 0f, node.rotation.getQuaternionAngleAround(0f, 1f, 0f) + diff)
                    passed += diff
                    isRunning = false
                } else {
                    node.rotation.setQuaternionByAxis(0f, 1f, 0f, node.rotation.getQuaternionAngleAround(0f, 1f, 0f) + speed)
                    passed += speed
                }
                node.requestTransformUpdate()

                val animation = physicsContext.rotateAnim
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