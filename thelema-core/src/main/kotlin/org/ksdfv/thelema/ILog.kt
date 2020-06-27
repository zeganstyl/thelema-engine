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

package org.ksdfv.thelema

/** @author zeganstyl */
interface ILog {
    var infoEnabled: Boolean
    var debugEnabled: Boolean
    var errorEnabled: Boolean

    fun info(message: String, exception: Throwable?, tag: String = "MyTag")
    fun debug(message: String, exception: Throwable?, tag: String = "MyTag")
    fun error(message: String, exception: Throwable?, tag: String = "MyTag")

    fun info(message: String, tag: String = "MyTag") = info(message, null, tag)
    fun debug(message: String, tag: String = "MyTag") = debug(message, null, tag)
    fun error(message: String, tag: String = "MyTag") = error(message, null, tag)
}