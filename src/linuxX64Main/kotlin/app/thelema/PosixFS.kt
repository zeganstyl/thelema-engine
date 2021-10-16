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

package app.thelema

import app.thelema.fs.IFile
import app.thelema.fs.IFileSystem

class PosixFS : IFileSystem {
    override val externalStoragePath: String
        get() = "/"
    override val isExternalStorageAvailable: Boolean
        get() = true
    override val localStoragePath: String
        get() = ""
    override val isLocalStorageAvailable: Boolean
        get() = true

    override fun file(path: String, location: String): IFile = PosixFile(path)
}