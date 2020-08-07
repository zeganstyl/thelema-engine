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
import org.ksdfv.thelema.json.IJsonArrayIO

/** @author zeganstyl */
class JsonBeansArrayIO(val io: IJsonArrayIO): JsonSerializable {
    override fun write(json: Json) {
        io.write(JsonBeansArray(json))
    }

    override fun read(json: Json, jsonData: JsonValue) {
        io.read(JsonBeansArray(json, jsonData))
    }

    companion object {
        fun print(io: IJsonArrayIO): String {
            return Json(OutputType.json).prettyPrint(JsonBeansArrayIO(io))
        }

        /** @param block will be called, if there is not error in json */
        fun parse(text: String, block: JsonBeansArray.() -> Unit) {
            try {
                val data = JsonReader().parse(text)
                if (data != null) {
                    block(JsonBeansArray(Json(OutputType.json), data))
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}