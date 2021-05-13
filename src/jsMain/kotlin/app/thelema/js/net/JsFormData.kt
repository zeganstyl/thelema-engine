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

package app.thelema.js.net

import app.thelema.data.IByteData
import app.thelema.fs.IFile
import app.thelema.net.IFormData
import org.w3c.files.Blob
import org.w3c.xhr.FormData

class JsFormData(val form: FormData = FormData()): IFormData {
    override fun set(name: String, value: IByteData, fileName: String) {
        form.set(name, Blob(arrayOf(value.sourceObject)), fileName)
    }

    override fun set(name: String, value: IFile, fileName: String) {
        form.set(name, value.sourceObject as Blob, fileName)
    }

    override fun set(name: String, value: String) {
        form.set(name, value)
    }
}