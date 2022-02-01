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

package app.thelema.studio.widget

import app.thelema.math.IVec4
import app.thelema.math.MATH
import app.thelema.math.Vec4
import app.thelema.utils.Color

class QuaternionWidget(): FloatsWidget(), PropertyProvider<IVec4> {
    constructor(block: QuaternionWidget.() -> Unit): this() {
        block(this)
    }

    val xField = FloatField()
    val yField = FloatField()
    val zField = FloatField()

    var value: IVec4 = defaultVec4

    override var set: (value: IVec4) -> Unit = {}
    override var get: () -> IVec4 = { defaultVec4 }

    init {
        addFloatField(xField, "X", Color.RED, { value.getQuaternionAngleAround(MATH.X) * MATH.radDeg }) {
            value.rotateQuaternionByX(it * MATH.degRad - value.getQuaternionAngleAround(MATH.X))
            set(value)
        }
        addFloatField(yField, "Y", Color.GREEN, { value.getQuaternionAngleAround(MATH.Y) * MATH.radDeg }) {
            value.rotateQuaternionByY(it * MATH.degRad - value.getQuaternionAngleAround(MATH.Y))
            set(value)
        }
        addFloatField(zField, "Z", Color.CYAN, { value.getQuaternionAngleAround(MATH.Z) * MATH.radDeg }) {
            value.rotateQuaternionByZ(it * MATH.degRad - value.getQuaternionAngleAround(MATH.Z))
            set(value)
        }
    }

    override fun act(delta: Float) {
        super.act(delta)
        value = get()
    }

    companion object {
        val defaultVec4 = Vec4()
    }
}
