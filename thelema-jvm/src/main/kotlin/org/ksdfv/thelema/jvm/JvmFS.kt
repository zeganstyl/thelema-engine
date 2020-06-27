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

package org.ksdfv.thelema.jvm

import org.ksdfv.thelema.fs.IFS
import java.io.File

/** @author zeganstyl */
class JvmFS: IFS {
    override val externalStoragePath: String
        get() = externalPath

    override val isExternalStorageAvailable: Boolean
        get() = true

    override val localStoragePath: String
        get() = localPath

    override val isLocalStorageAvailable: Boolean
        get() = true

    override fun file(path: String, location: Int) = JvmFile(path, location)

    companion object {
        val externalPath = System.getProperty("user.home") + File.separator
        val localPath = File("").absolutePath + File.separator
    }
}