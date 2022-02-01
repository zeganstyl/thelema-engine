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

package app.thelema.studio

import app.thelema.app.APP
import app.thelema.ecs.IEntityComponent
import app.thelema.lwjgl3.JvmApp
import app.thelema.lwjgl3.Lwjgl3WindowConf
import java.util.*
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvm.JvmDependencyFromClassLoader
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object ThelemaStudioJvm {
    @JvmStatic
    fun main(args: Array<String>) {
        val app = JvmApp(
            Lwjgl3WindowConf {
                title = "Thelema Studio"
                width = 1280
                height = 720
                iconPaths = arrayOf(
                    "thelema-logo-32.png",
                    "thelema-logo-64.png"
                )
                msaaSamples = 4
            }
        )

        app.isEditorMode = true

        IEntityComponent.propertiesLinkingMap = WeakHashMap()

        val host = BasicJvmScriptingHost()

        KotlinScripting.init(
            host,
            JvmDependencyFromClassLoader { app::class.java.classLoader!! },
        )

        Thread {
            println("Scripting engine is warming up...")
            val time = APP.time
            host.eval(
                StringScriptSource("fun main(){}", ""),
                ScriptCompilationConfiguration(),
                null
            )
            println("Scripting engine is ready (${APP.time - time} ms)")
        }.start()

        Studio.fileChooser = JvmFileChooser()

        app.startLoop()
    }
}
