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

package org.ksdfv.thelema.utils

import org.ksdfv.thelema.kx.ThreadLocal

/** @author zeganstyl */
@ThreadLocal
object LOG: ILog {
    lateinit var proxy: ILog

    override var infoEnabled: Boolean
        get() = proxy.infoEnabled
        set(value) { proxy.infoEnabled = value }

    override var debugEnabled: Boolean
        get() = proxy.debugEnabled
        set(value) { proxy.debugEnabled = value }

    override var errorEnabled: Boolean
        get() = proxy.errorEnabled
        set(value) { proxy.errorEnabled = value }

    override fun info(message: String, exception: Throwable?, tag: String) =
        proxy.info(message, exception, tag)

    override fun debug(message: String, exception: Throwable?, tag: String) =
        proxy.debug(message, exception, tag)

    override fun error(message: String, exception: Throwable?, tag: String) =
        proxy.error(message, exception, tag)
}