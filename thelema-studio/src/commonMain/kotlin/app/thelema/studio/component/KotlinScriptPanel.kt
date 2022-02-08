package app.thelema.studio.component

import app.thelema.ecs.Component
import app.thelema.fs.FS
import app.thelema.res.RES
import app.thelema.studio.*
import app.thelema.studio.ecs.KotlinScriptStudio
import app.thelema.studio.ecs.KotlinScripting
import app.thelema.studio.ecs.scriptFile
import app.thelema.ui.InputEvent
import app.thelema.ui.MenuItem
import app.thelema.ui.TextButton

class KotlinScriptPanel: ComponentPanel<KotlinScriptStudio>("KotlinScript") {
    override val menuItems: List<MenuItem> = listOf(
        MenuItem("Open Script in IDEA") {
            onClick(::openInIdea)
        }
    )

    init {
        content.add(TextButton("Open Directory") {
            onClick {
                KotlinScripting.kotlinDirectory?.also { kotlinDir ->
                    component?.file?.also { Studio.fileChooser.openInFileManager("${kotlinDir.platformPath}/${it.parent().platformPath}") }
                }
            }
        }).newRow()

        content.add(TextButton("Open Script in IDEA") {
            onClick(::openInIdea)
        }).newRow()

        content.add(TextButton("Create") {
            onClick {
                KotlinScripting.kotlinDirectory ?: return@onClick
                val component = component ?: return@onClick

                val file = component.file
                var className = component.scriptComponentName

                if (className.isEmpty()) {
                    if (file != null && file.exists()) {
                        className = file.nameWithoutExtension
                        createScript(className)
                    } else {
                        Studio.nameWindow.show {
                            createScript(it)
                        }
                    }
                } else {
                    createScript(className)
                }
            }
        }).newRow()
    }

    private fun openInIdea(event: InputEvent) {
        val component = component ?: return
        val kotlinDir = KotlinScripting.kotlinDirectory
        if (kotlinDir != null && kotlinDir.exists()) {
            val file = component.file
            if (file != null && file.exists()) {
                Studio.fileChooser.executeCommandInTerminal(
                    FS.absolute(Studio.fileChooser.userHomeDirectory),
                    listOf("idea", file.platformPath)
                )
            } else {
                Studio.showStatusAlert("File is not exists: ${file?.platformPath}")
            }
        } else {
            Studio.showStatusAlert("Kotlin directory is not exists: ${kotlinDir?.platformPath}")
        }
    }

    private fun createScript(className: String) {
        val kotlinDir = KotlinScripting.kotlinDirectory ?: return
        val component = component ?: return
        var file = component.file

        if (file == null) {
            val path = "${RES.appPackage.replace('.', '/')}/$className.kt"
            file = kotlinDir.child(path)
            file.parent().mkdirs()
            component.file = scriptFile(path)
        } else {
            if (!file.exists()) file.parent().mkdirs()
        }

        component.scriptComponentName = className

        file.writeText(
            """package ${RES.appPackage}

import ${Component::class.qualifiedName}

class $className: Component() {
    override val componentName: String
        get() = "$className"
    
    // TODO
}
"""
        )
    }
}