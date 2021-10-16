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

import app.thelema.app.*
import app.thelema.audio.AL
import app.thelema.audio.mock.AudioStub
import app.thelema.data.DATA
import app.thelema.ecs.ECS
import app.thelema.fs.FS
import app.thelema.json.JSON
import app.thelema.jvm.data.JvmData
import app.thelema.jvm.json.JsonSimpleJson
import app.thelema.utils.LOG

open class JvmGraphiclessApp: AbstractApp() {
    override val platformType: String
        get() = DesktopApp
    override var clipboardString: String = ""
    override var cursor: Int = Cursor.Arrow
    override var defaultCursor: Int = Cursor.Arrow
    override val time: Long
        get() = System.currentTimeMillis()

    override var width: Int = 0
    override var height: Int = 0

    var running = true

    override fun messageBox(title: String, message: String) {}

    override fun loadPreferences(name: String): String = ""

    override fun savePreferences(name: String, text: String) {}

    override fun destroy() {}

    override fun startLoop() {
        while (running) {
            updateDeltaTime()
            update()
            Thread.sleep(16)
        }
    }

    init {
        ECS.setupDefaultComponents()

        APP = this
        LOG = JvmLog()
        FS = JvmFS()
        JSON = JsonSimpleJson()
        DATA = JvmData()
        //IMG.proxy = STBImg()
    }
}