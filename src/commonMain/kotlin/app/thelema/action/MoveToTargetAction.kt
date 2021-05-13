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

import app.thelema.ecs.getComponentOrNull
import app.thelema.g3d.node.ITransformNode
import app.thelema.math.IVec3
import app.thelema.math.Vec3

/** @author zeganstyl */
class MoveToTargetAction: ActionAdapter() {
    var speed: Float = 0f

    var target: ITransformNode? = null

    private val tmp: IVec3 = Vec3()
    private val tmp2: IVec3 = Vec3()

    override val componentName: String
        get() = "MoveToTargetAction"

    override var actionData: ActionData = ActionData(0f, null, null)

    override fun update(delta: Float): Float {
        if (isRunning) {
            val target = target
            val node = entityOrNull?.parentEntity?.getComponentOrNull<ActionList>()?.customContext?.getComponentOrNull<ITransformNode>()
            if (target != null && node != null) {
                val speed = speed * delta
                if (target.getGlobalPosition(tmp).dst2(node.getGlobalPosition(tmp2)) > speed * speed) {
                    tmp.set(target.position).sub(node.position).nor().scl(speed)
                    node.position.add(tmp)
                } else {
                    node.position.set(target.position)
                    isRunning = false
                }
                node.requestTransformUpdate()
            } else {
                isRunning = false
            }
        }
        return 0f
    }
}