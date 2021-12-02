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
import kotlin.reflect.KMutableProperty1

class StringEnumPropertyDesc<T: IEntityComponent>(
    override val name: String,
    val values: List<String>,
    val getValueBlock: T.() -> String,
    val setValueBlock: T.(value: String) -> Unit
): IPropertyDescriptor<T, String> {
    override val type = PropertyType.StringEnum
    override fun setValue(component: T, value: String) {
        if (!values.contains(value)) throw IllegalStateException("StringEnum: value \"$value\" can't be set as enum")
        component.setValueBlock(value)
    }
    override fun getValue(component: T): String = getValueBlock(component)
    override fun default(): String = ""
    override fun readJson(component: T, json: IJsonObject) = setValue(component, json.string(name, default()))
    override fun writeJson(component: T, json: IJsonObject) { json[name] = component.getValueBlock() }
}

class StringEnumPropertyDesc2<T: IEntityComponent>(
    val property: KMutableProperty1<T, String>,
    val values: List<String>,
): IPropertyDescriptor<T, String> {
    override val name: String
        get() = property.name

    override val type = PropertyType.StringEnum
    override fun setValue(component: T, value: String) {
        if (!values.contains(value)) throw IllegalStateException("StringEnum: value \"$value\" can't be set as enum")
        property.set(component, value)
    }
    override fun getValue(component: T): String = property.get(component)
    override fun default(): String = ""
    override fun readJson(component: T, json: IJsonObject) = property.set(component, json.string(name, default()))
    override fun writeJson(component: T, json: IJsonObject) { json[name] = property.get(component) }
}

class IntEnumPropertyDesc2<T: IEntityComponent>(
    val property: KMutableProperty1<T, Int>,
    val values: Map<Int, String>,
    val defaultValue: String
): IPropertyDescriptor<T, Int> {
    override val name: String
        get() = property.name

    override val type = PropertyType.IntEnum
    override fun setValue(component: T, value: Int) {
        if (!values.contains(value)) throw IllegalStateException("StringEnum: value \"$value\" can't be set as enum")
        property.set(component, value)
    }
    override fun getValue(component: T): Int = property.get(component)
    override fun default(): Int = 0
    override fun readJson(component: T, json: IJsonObject) = property.set(component, json.int(name, default()))
    override fun writeJson(component: T, json: IJsonObject) { json[name] = property.get(component) }
}