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
import app.thelema.lwjgl3.JvmApp
import app.thelema.lwjgl3.Lwjgl3WindowConf
import org.jetbrains.kotlin.mainKts.COMPILED_SCRIPTS_CACHE_DIR_ENV_VAR
import org.jetbrains.kotlin.mainKts.COMPILED_SCRIPTS_CACHE_DIR_PROPERTY
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.FileSystemDependenciesResolver
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.JvmDependencyFromClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.templates.standard.SimpleScriptTemplate

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
