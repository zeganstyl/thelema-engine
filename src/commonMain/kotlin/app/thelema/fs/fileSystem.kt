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

/** Indicates how to resolve a path to a file.
 * @author mzechner, Nathan Sweet, zeganstyl
 */
object FileLocation {
    /** Path relative to the root of the classpath. Classpath files are always readonly. */
    const val Classpath = "Classpath"
    /** Path relative to the asset directory on Android and to the application's root directory on the desktop. On the desktop,
     * if the file is not found, then the classpath is checked. This enables files to be found when using JWS or applets.
     * Internal files are always readonly.  */
    const val Internal = "Internal"
    /** Path relative to the root of the SD card on Android and to the home directory of the current user on the desktop.  */
    const val External = "External"
    /** Path that is a fully qualified, absolute filesystem path. To ensure portability across platforms use absolute files only
     * when absolutely (heh) necessary.  */
    const val Absolute = "Absolute"
    /** Path relative to the private files directory on Android and to the application's root directory on the desktop.  */
    const val Local = "Local"

    // TODO
    /** Files contained in random access memory */
    const val RAM = "RAM"

    const val Project = "Project"

    const val Relative = "Relative"
}

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

    val writeAccess: Boolean
        get() = true

    /** Returns a handle representing a file or directory.
     * @param location Determines how the path is resolved. Use [FileLocation]
     * @throws RuntimeException if the type is classpath or internal and the file does not exist.
     */
    fun file(path: String, location: String): IFile

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

/** File system abstraction layer. It is singleton and you can use it anywhere after initialization.
 * If you want multiple APIs, you may switch between them by setting.
 * By default used JVM file system implementation.
 * @author zeganstyl */
lateinit var FS: IFileSystem

/** Get file from internal location (resources for desktop, assets for Android, same origin for web) */
fun file(path: String): IFile = FS.internal(path)

fun projectFile(path: String): IFile = FS.file(path, FileLocation.Project)

fun file(path: String, location: String) = FS.file(path, location)