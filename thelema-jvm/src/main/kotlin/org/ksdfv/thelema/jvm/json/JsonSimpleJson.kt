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

package org.ksdfv.thelema.jvm.json

import com.github.cliftonlabs.json_simple.JsonArray
import com.github.cliftonlabs.json_simple.JsonObject
import com.github.cliftonlabs.json_simple.Jsoner
import org.ksdfv.thelema.json.*

/** @author zeganstyl */
class JsonSimpleJson: IJSON {
    override fun parseObject(text: String): IJsonObject =
        JsonSimpleObject(Jsoner.deserialize(text, JsonObject()))

    override fun parseArray(text: String): IJsonArray =
        JsonSimpleArray(Jsoner.deserialize(text, JsonArray()))

    override fun printArray(context: IJsonArray.() -> Unit): String =
        Jsoner.prettyPrint(JsonSimpleArray().apply(context).source.toJson())

    override fun printObject(context: IJsonObject.() -> Unit): String =
        Jsoner.prettyPrint(JsonSimpleObject().apply(context).source.toJson())

    override fun printObject(json: IJsonObjectIO): String =
        Jsoner.prettyPrint(JsonSimpleObject().apply { json.write(this) }.source.toJson())

    override fun printArray(json: IJsonArrayIO): String =
        Jsoner.prettyPrint(JsonSimpleArray().apply { json.write(this) }.source.toJson())
}