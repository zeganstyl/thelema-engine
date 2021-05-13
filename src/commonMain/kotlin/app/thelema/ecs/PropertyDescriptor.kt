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

class PropertyDescriptor<T: IEntityComponent, V: Any?>(var name: String) {
    constructor(name: String, block: PropertyDescriptor<T, V>.() -> Unit): this(name) {
        block(this)
    }

    var type: String = PropertyType.Unknown.propertyTypeName

    var set: T.(value: V) -> Unit = {}
    lateinit var get: T.() -> V

    var copy: T.(other: T) -> Unit = {
        set(it.get())
    }

    lateinit var default: () -> V

    var readJson: T.(json: IJsonObject) -> Unit = {}
    var writeJson: T.(json: IJsonObject) -> Unit = {}

    var useJsonReadWrite = true
}
