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

package app.thelema.jvm.net

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.core.*
import app.thelema.data.IByteData
import app.thelema.fs.IFile
import app.thelema.net.IFormData

class KtorFormData: IFormData {
    val list = ArrayList<PartData>()

    override fun set(name: String, value: IByteData, fileName: String) {
        val partHeaders = HeadersBuilder()
        partHeaders.append(HttpHeaders.ContentDisposition, "form-data; name=$name; filename=$fileName")
        partHeaders.append(HttpHeaders.ContentLength, value.remaining.toString())

        val oldPos = value.position
        val bytes = ByteArray(value.remaining) { value.get() }
        value.position = oldPos

        list.add(PartData.BinaryItem({ ByteReadPacket(bytes) }, {}, partHeaders.build()))
    }

    override fun set(name: String, value: IFile, fileName: String) {
        value.readBytes { set(name, it, fileName) }
    }

    override fun set(name: String, value: String) {
        val partHeaders = HeadersBuilder()
        partHeaders.append(HttpHeaders.ContentDisposition, "form-data; name=$name")
        list.add(PartData.FormItem(value, {}, partHeaders.build()))
    }
}