package app.thelema.studio.widget.component

import app.thelema.ecs.Component
import app.thelema.res.RES
import app.thelema.studio.KotlinScriptStudio
import app.thelema.studio.KotlinScripting
import app.thelema.studio.Studio
import app.thelema.studio.scriptFile
import app.thelema.ui.TextButton

class KotlinScriptPanel: ComponentPanel<KotlinScriptStudio>("KotlinScript") {
    init {
        content.add(TextButton("execute") {
            onClick {
                component?.execute()
            }
        }).newRow()

        content.add(TextButton("Open directory") {
            onClick {
                KotlinScripting.kotlinDirectory?.also { kotlinDir ->
                    component?.file?.also { Studio.fileChooser.openInFileManager("${kotlinDir.platformPath}/${it.parent().platformPath}") }
                }
            }
        }).newRow()

        content.add(TextButton("Create") {
            onClick {
                KotlinScripting.kotlinDirectory ?: return@onClick
                val component = component ?: return@onClick

                val file = component.file
                var className = component.functionName

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

        component.functionName = className

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