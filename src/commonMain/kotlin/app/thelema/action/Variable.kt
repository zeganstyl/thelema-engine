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

import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.IPropertyType
import app.thelema.ecs.PropertyType

class Variable() {
    constructor(block: Variable.() -> Unit): this() { block(this) }
    constructor(type: IPropertyType): this() { this.type = type }

    var componentOrNull: IEntityComponent? = null
    val component: IEntityComponent
        get() = componentOrNull!!

    var value: Any? = null

    var type: IPropertyType = PropertyType.Unknown

    var name: String = ""

    fun setBool(value: Boolean) {
        type = PropertyType.Bool
        this.value = value
    }

    fun setFloat(value: Float) {
        type = PropertyType.Float
        this.value = value
    }

    fun setInt(value: Int) {
        type = PropertyType.Int
        this.value = value
    }

    fun setString(value: String) {
        type = PropertyType.String
        this.value = value
    }

    fun plus(other: Variable): Any? {
        if (other.type == type) {
            when (type) {
                PropertyType.Int -> { return (value as Int) + (other.value as Int) }
                PropertyType.Float -> { return (value as Float) + (other.value as Float) }
                PropertyType.String -> { return (value as String) + (other.value as String) }
            }
        }
        return null
    }

    fun isEqual(other: Variable): Boolean = value == other.value

    companion object {
        fun bool() = Variable(PropertyType.Bool)
        fun string() = Variable(PropertyType.String)
        fun float() = Variable(PropertyType.Float)
        fun int() = Variable(PropertyType.Int)

        val Unknown = Variable()
    }
}