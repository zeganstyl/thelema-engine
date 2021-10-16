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

class StringEnumPropertyDesc<T: IEntityComponent>(
    override val name: String,
    val values: List<String>,
    val getValue: T.() -> String,
    val setValue: T.(value: String) -> Unit
): IPropertyDescriptor<T, String> {
    override val type: String = PropertyType.StringEnum.propertyTypeName
    override fun setValue(component: T, value: String) {
        if (!values.contains(value)) throw IllegalStateException("StringEnum: value \"$value\" can't be set as enum")
        component.setValue(value)
    }
    override fun getValue(component: T): String = getValue(component)
    override fun default(): String = ""
    override fun readJson(component: T, json: IJsonObject) = setValue(component, json.string(name, default()))
    override fun writeJson(component: T, json: IJsonObject) { json[name] = component.getValue() }
}