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

package org.ksdfv.thelema.fs

/** Indicates how to resolve a path to a file.
 * @author mzechner, Nathan Sweet, zeganstyl
 */
object FileLocation {
    /** Path relative to the root of the classpath. Classpath files are always readonly. */
    const val Classpath = 0
    /** Path relative to the asset directory on Android and to the application's root directory on the desktop. On the desktop,
     * if the file is not found, then the classpath is checked. This enables files to be found when using JWS or applets.
     * Internal files are always readonly.  */
    const val Internal = 1
    /** Path relative to the root of the SD card on Android and to the home directory of the current user on the desktop.  */
    const val External = 2
    /** Path that is a fully qualified, absolute filesystem path. To ensure portability across platforms use absolute files only
     * when absolutely (heh) necessary.  */
    const val Absolute = 3
    /** Path relative to the private files directory on Android and to the application's root directory on the desktop.  */
    const val Local = 4
}