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

package org.ksdfv.thelema.teavm.json

import org.ksdfv.thelema.json.*
import org.teavm.jso.json.JSON

class TvmJson: IJSON {
    override fun parseObject(text: String): IJsonObject =
        TvmJsonObject(JSON.parse(text) as JsObj)

    override fun parseArray(text: String): IJsonArray =
        TvmJsonArray(JSON.parse(text) as JsArray)

    override fun printArray(context: IJsonArray.() -> Unit): String =
        JSON.stringify(TvmJsonArray().apply(context).array)

    override fun printObject(context: IJsonObject.() -> Unit): String =
        JSON.stringify(TvmJsonObject().apply(context).obj)

    override fun printObject(json: IJsonObjectIO): String =
        JSON.stringify(TvmJsonObject().apply { json.write(this) }.obj)

    override fun printArray(json: IJsonArrayIO): String =
        JSON.stringify(TvmJsonArray().apply { json.write(this) }.array)
}