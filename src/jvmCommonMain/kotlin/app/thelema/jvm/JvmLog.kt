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

package app.thelema.jvm

import app.thelema.utils.ILog

class JvmLog: ILog {
    override var infoEnabled: Boolean = true
    override var debugEnabled: Boolean = true
    override var errorEnabled: Boolean = true

    override fun info(message: Any?) {
        println(message)
    }

    override fun info(message: String, exception: Throwable?, tag: String) {
        println(message)
    }

    override fun error(message: String, exception: Throwable?, tag: String) {
        System.err.println(message)
    }
}
