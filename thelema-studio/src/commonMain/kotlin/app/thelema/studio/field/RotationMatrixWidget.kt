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

package app.thelema.studio.field

import app.thelema.math.*
import app.thelema.utils.Color
import kotlin.math.atan2
import kotlin.math.sqrt

class RotationMatrixWidget(): FloatsWidget(), PropertyProvider<IMat3C> {
    constructor(block: RotationMatrixWidget.() -> Unit): this() {
        block(this)
    }

    val xField = FloatField()
    val yField = FloatField()
    val zField = FloatField()

    val defaultValue = Mat3()

    val mat = Mat3()

    var value: IMat3C = defaultValue

    override var set: (value: IMat3C) -> Unit = {}
    override var get: () -> IMat3C = { defaultValue }

    init {
        addFloatField(xField, "X", Color.RED, { xAngle() }) {
            set(mat.set(value).rotateAroundAxis(1f, 0f, 0f, (it - xAngle()) * MATH.degRad))
        }
        addFloatField(yField, "Y", Color.GREEN, { yAngle() }) {
            set(mat.set(value).rotateAroundAxis(0f, 1f, 0f, (it - yAngle()) * MATH.degRad))
        }
        addFloatField(zField, "Z", Color.CYAN, { zAngle() }) {
            set(mat.set(value).rotateAroundAxis(0f, 0f, 1f, (it - zAngle()) * MATH.degRad))
        }
    }

    fun xAngle() = atan2(value.m21, value.m22) * MATH.radDeg

    fun yAngle() = atan2(-value.m20, sqrt(value.m21 * value.m21 + value.m22 * value.m22)) * MATH.radDeg

    fun zAngle() = atan2(value.m10, value.m00) * MATH.radDeg

    override fun act(delta: Float) {
        super.act(delta)
        value = get()
    }

    companion object {
        val defaultVec4 = Vec4()
    }
}
