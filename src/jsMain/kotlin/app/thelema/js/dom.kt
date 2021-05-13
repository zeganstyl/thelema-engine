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

package app.thelema.js

import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.DataTransferItem
import org.w3c.dom.events.MouseEvent
import org.w3c.files.Blob
import org.w3c.files.File
import kotlin.js.Promise

open external class MouseEvent: MouseEvent {
    val movementX: Int
    val movementY: Int
}

abstract external class BlobEx: Blob {
    abstract fun arrayBuffer(): Promise<ArrayBuffer>
}

abstract external class FileSystemEntry {
    val fullPath: String
    val name: String
    val isDirectory: Int
    val isFile: Int
}

abstract external class FileSystemFileEntry: FileSystemEntry {
    fun file(successCallback: (file: File) -> Unit)
}

abstract external class DataTransferItemEx: DataTransferItem {
    abstract fun webkitGetAsEntry(): FileSystemEntry
}

abstract external class FileSystemDirectoryReader {
    fun readEntries(callback: (entries: Array<FileSystemEntry>) -> Unit);
}

abstract external class FileSystemDirectoryEntry: FileSystemEntry {
    abstract fun createReader(): FileSystemDirectoryReader
}