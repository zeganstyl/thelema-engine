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

class Vec4Widget(): FloatsWidget(), PropertyProvider<IVec4> {
    constructor(block: Vec4Widget.() -> Unit): this() {
        block(this)
    }

    val xField = FloatField()
    val yField = FloatField()
    val zField = FloatField()
    val wField = FloatField()

    var value: IVec4 = defaultVec4

    override var set: (value: IVec4) -> Unit = {}
    override var get: () -> IVec4 = { MATH.Zero3One1 }

    init {
        addFloatField(xField, "X", Color.RED_INT, { value.x }) { value.x = it }
        addFloatField(yField, "Y", Color.GREEN_INT, { value.y }) { value.y = it }
        addFloatField(zField, "Z", Color.CYAN_INT, { value.z }) { value.z = it }
        addFloatField(wField, "W", Color.GRAY_INT, { value.w }) { value.w = it }
    }

    override fun act(delta: Float) {
        super.act(delta)
        value = get()
    }

    companion object {
        val defaultVec4 = Vec4()
    }
}