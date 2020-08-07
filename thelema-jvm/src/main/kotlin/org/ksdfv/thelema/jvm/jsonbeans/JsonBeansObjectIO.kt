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

package org.ksdfv.thelema.jvm.jsonbeans

import com.esotericsoftware.jsonbeans.*
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO

/** @author zeganstyl */
class JsonBeansObjectIO(val io: IJsonObjectIO): JsonSerializable {
    override fun write(json: Json) {
        io.write(JsonBeansObject(json))
    }

    override fun read(json: Json, jsonData: JsonValue) {
        io.read(JsonBeansObject(json, jsonData))
    }

    companion object {
        fun print(io: IJsonObjectIO): String {
            return Json(OutputType.json).prettyPrint(JsonBeansObjectIO(io))
        }

        /** @param context will be called, if there is not error in json */
        fun parse(text: String, context: JsonBeansObject.() -> Unit): IJsonObject {
            val data = JsonReader().parse(text)!!
            val json = JsonBeansObject(Json(OutputType.json), data)
            context(json)
            return json
        }
    }
}