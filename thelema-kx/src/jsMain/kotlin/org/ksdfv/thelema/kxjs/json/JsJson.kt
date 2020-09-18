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

package org.ksdfv.thelema.kxjs.json

import org.ksdfv.thelema.json.*
import kotlin.js.JSON

class JsJson: IJSON {
    override fun parseObject(text: String): IJsonObject =
        JsJsonObject(source = JSON.parse(text))

    override fun parseArray(text: String): IJsonArray =
        JsJsonArray(JSON.parse(text) as Array<dynamic>)

    override fun printArray(context: IJsonArray.() -> Unit): String =
        JSON.stringify(JsJsonArray().apply(context).toJSON())

    override fun printObject(context: IJsonObject.() -> Unit): String =
        JSON.stringify(JsJsonObject().apply(context).source)

    override fun printObject(json: IJsonObjectIO): String =
        JSON.stringify(JsJsonObject().apply { json.write(this) }.source)

    override fun printArray(json: IJsonArrayIO): String =
        JSON.stringify(JsJsonArray(json).toJSON())
}