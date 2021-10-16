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

package app.thelema.ecs

import app.thelema.json.IJsonObject

interface IPropertyDescriptor<T: IEntityComponent, V: Any?> {
    val name: String

    val type: String

    val useJsonReadWrite: Boolean
        get() = true

    /** Set property [value] to [component] */
    fun setValue(component: T, value: V)

    /** Get property value from [component] */
    fun getValue(component: T): V

    /** Copy property value from [other] to [component] */
    fun copy(component: T, other: T) = setValue(component, getValue(other))

    /** Get default property value */
    fun default(): V

    /** Read property value from JSON */
    fun readJson(component: T, json: IJsonObject)

    /** Write property value to JSON */
    fun writeJson(component: T, json: IJsonObject)
}