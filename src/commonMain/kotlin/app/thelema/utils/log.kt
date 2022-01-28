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

package app.thelema.utils

/** @author zeganstyl */
interface ILog {
    var infoEnabled: Boolean
    var debugEnabled: Boolean
    var errorEnabled: Boolean
    var throwExceptionOnError: Boolean

    fun info(message: Boolean) = info(message.toString())
    fun info(message: Int) = info(message.toString())
    fun info(message: Float) = info(message.toString())
    fun info(message: Any?) = info(message?.toString() ?: "null")

    fun info(message: String, exception: Throwable?, tag: String = "Thelema")
    fun debug(message: String, exception: Throwable?, tag: String = "Thelema") {
        if (debugEnabled) info(message, exception, tag)
    }
    fun error(message: String, exception: Throwable?, tag: String = "Thelema")

    fun info(message: String, tag: String = "Thelema") = info(message, null, tag)
    fun debug(message: String, tag: String = "Thelema") = debug(message, null, tag)
    fun error(message: String, tag: String = "Thelema") = error(message, null, tag)
    fun error(message: Any?, tag: String = "Thelema") = error(message.toString(), null, tag)
}

lateinit var LOG: ILog