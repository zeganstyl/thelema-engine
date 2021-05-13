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

/** Provides standard access to the filesystem, classpath, Android SD card, and Android assets directory.
 * @author mzechner, Nathan Sweet, zeganstyl
 */
interface IFileSystem {
    /** Returns the external storage path directory. This is the SD card on Android and the home directory of the current user on
     * the desktop.  */
    val externalStoragePath: String

    /** Returns true if the external storage is ready for file IO. Eg, on Android, the SD card is not available when mounted for use
     * with a PC.  */
    val isExternalStorageAvailable: Boolean

    /** Returns the local storage path directory. This is the private files directory on Android and the directory of the jar on the
     * desktop.  */
    val localStoragePath: String

    /** Returns true if the local storage is ready for file IO.  */
    val isLocalStorageAvailable: Boolean

    /** Returns a handle representing a file or directory.
     * @param location Determines how the path is resolved. Use [FileLocation]
     * @throws RuntimeException if the type is classpath or internal and the file does not exist.
     */
    fun file(path: String, location: Int): IFile

    /** Convenience method that returns a [FileLocation.Classpath] file handle.  */
    fun classpath(path: String): IFile = file(path, FileLocation.Classpath)

    /** Convenience method that returns a [FileLocation.Internal] file handle.  */
    fun internal(path: String): IFile = file(path, FileLocation.Internal)

    /** Convenience method that returns a [FileLocation.External] file handle.  */
    fun external(path: String): IFile = file(path, FileLocation.External)

    /** Convenience method that returns a [FileLocation.Absolute] file handle.  */
    fun absolute(path: String): IFile = file(path, FileLocation.Absolute)

    /** Convenience method that returns a [FileLocation.Local] file handle.  */
    fun local(path: String): IFile = file(path, FileLocation.Local)
}