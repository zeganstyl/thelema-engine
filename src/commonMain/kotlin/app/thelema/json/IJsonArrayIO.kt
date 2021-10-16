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

package app.thelema.json

/** This interface must be implemented by object, which needs to be serialized to/from JSON.
 *
 * @author zeganstyl  */
interface IJsonArrayIO {
    /** @param json JSON-array to read from */
    fun readJson(json: IJsonArray)

    /** @param json JSON-array to write to */
    fun writeJson(json: IJsonArray)
}