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

package org.ksdfv.thelema.json

import kotlin.native.concurrent.ThreadLocal

/** @author zeganstyl */
@ThreadLocal
object JSON: IJSON {
    lateinit var proxy: IJSON

    override fun parseObject(text: String): IJsonObject = proxy.parseObject(text)

    override fun parseArray(text: String): IJsonArray = proxy.parseArray(text)

    override fun printArray(context: IJsonArray.() -> Unit): String = proxy.printArray(context)

    override fun printObject(context: IJsonObject.() -> Unit): String = proxy.printObject(context)

    override fun printObject(json: IJsonObjectIO): String = proxy.printObject(json)

    override fun printArray(json: IJsonArrayIO): String = proxy.printArray(json)
}