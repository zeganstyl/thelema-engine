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

import app.thelema.json.IJsonObject
import kotlin.math.abs

/** Lerp some variable.
 * For variable must defined getter [getValue] and setter [setValue].
 * [speed] in seconds.
 * @author zeganstyl */
class LerpFloatToTargetWithSpeed: ActionAdapter() {
    var speed: Float = 0f
    var target: Float = 0f
    var getValue: () -> Float = { 0f }
    var setValue: (newValue: Float) -> Unit = {}

    override val componentName: String
        get() = "LerpFloatToTargetWithSpeed"

    override var actionData: ActionData = ActionData()

    override fun update(delta: Float): Float {
        if (isRunning) {
            val speed = speed * delta
            val current = getValue()
            val diff = target - current
            if (abs(diff) > speed) {
                if (diff > 0) {
                    setValue(current + speed)
                } else {
                    setValue(current - speed)
                }
            } else {
                setValue(target)
                isRunning = false
            }
        }
        return 0f
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)

        speed = json.float("speed", 0f)
        target = json.float("target", 0f)
    }

    override fun writeJson(json: IJsonObject) {
        super.writeJson(json)

        json["speed"] = speed
        json["target"] = target
    }
}