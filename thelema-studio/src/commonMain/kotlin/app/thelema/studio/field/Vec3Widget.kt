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

import app.thelema.math.IVec3C
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.utils.Color

class Vec3Widget(): FloatsWidget(), PropertyProvider<IVec3C> {
    constructor(block: Vec3Widget.() -> Unit): this() {
        block(this)
    }

    val xField = FloatField()
    val yField = FloatField()
    val zField = FloatField()

    var value: IVec3C = defaultVec3
    private val _value = Vec3()

    override var set: (value: IVec3C) -> Unit = {}
    override var get: () -> IVec3C = { MATH.Zero3 }

    init {
        addFloatField(xField, "X", Color.RED, { value.x }) { set(_value.set(it, value.y, value.z)) }
        addFloatField(yField, "Y", Color.GREEN, { value.y }) { set(_value.set(value.x, it, value.z)) }
        addFloatField(zField, "Z", Color.CYAN, { value.z }) { set(_value.set(value.x, value.y, it)) }
    }

    override fun act(delta: Float) {
        super.act(delta)
        value = get()
    }

    companion object {
        val defaultVec3 = Vec3()
    }
}