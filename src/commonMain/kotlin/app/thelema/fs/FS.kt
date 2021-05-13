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

package app.thelema.fs

import kotlin.native.concurrent.ThreadLocal

/** File system abstraction layer. It is singleton and you can use it anywhere after initialization.
 * If you want multiple APIs, you may switch between them by setting.
 * By default used JVM file system implementation.
 * @author zeganstyl */
@ThreadLocal
object FS: IFileSystem {
    const val ReadAccessFlag = 1
    const val WriteAccessFlag = 2

    lateinit var proxy: IFileSystem

    override val externalStoragePath: String
        get() = proxy.externalStoragePath
    override val isExternalStorageAvailable: Boolean
        get() = proxy.isExternalStorageAvailable
    override val localStoragePath: String
        get() = proxy.localStoragePath
    override val isLocalStorageAvailable: Boolean
        get() = proxy.isLocalStorageAvailable

    override fun file(path: String, location: Int): IFile = proxy.file(path, location)
}
