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

/** @author zeganstyl */
object LOG: ILog {
    var api: ILog = DefaultLog()

    override var infoEnabled: Boolean
        get() = api.infoEnabled
        set(value) { api.infoEnabled = value }

    override var debugEnabled: Boolean
        get() = api.debugEnabled
        set(value) { api.debugEnabled = value}

    override var errorEnabled: Boolean
        get() = api.errorEnabled
        set(value) { api.errorEnabled = value }

    override var collectLogs: Boolean
        get() = api.collectLogs
        set(value) { api.collectLogs = value }

    override val logs: MutableList<String>
        get() = api.logs

    override fun info(message: String, exception: Throwable?, tag: String) =
        api.info(message, exception, tag)

    override fun debug(message: String, exception: Throwable?, tag: String) =
        api.debug(message, exception, tag)

    override fun error(message: String, exception: Throwable?, tag: String) =
        api.error(message, exception, tag)

    override fun info(message: String, tag: String) = api.info(message, tag)
    override fun debug(message: String, tag: String) = api.debug(message, tag)
    override fun error(message: String, tag: String) = api.error(message, tag)
}