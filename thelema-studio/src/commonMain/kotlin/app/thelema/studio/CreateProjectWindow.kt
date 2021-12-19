package app.thelema.studio

import app.thelema.fs.FS
import app.thelema.fs.projectFile
import app.thelema.studio.widget.PlainCheckBox
import app.thelema.ui.*
import app.thelema.utils.iterate

class CreateProjectWindow: Window("Create project") {
    val lwjglNatives = ArrayList<Pair<CheckBox, LwjglNativeDependency>>()

    val packageNameRegex = "^[a-z]+(\\.[a-z0-9]+)*\$".toRegex()
    val versionRegex = "^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)\$".toRegex()
    val directoryRegex = "^(.+)\\/([^\\/]+)\$".toRegex()

    val projectName = TextField { validate { it.isNotEmpty() } }
    val packageName = TextField { validate { it.matches(packageNameRegex) } }
    val directory = TextField { validate { it.matches(directoryRegex) } }
    val kotlinVersion = TextField { validate { it.matches(versionRegex) } }
    val lwjglVersion = TextField { validate { it.matches(versionRegex) } }
    var isDirectoryNameSame = true

    val mainAppLauncher = PlainCheckBox().also { it.isChecked = true }
    val scriptTemplate = PlainCheckBox().also { it.isChecked = true }
    val openIntellij = PlainCheckBox()

    init {
        kotlinVersion.text = CreateProjectTool.kotlinVersionDefault
        lwjglVersion.text = CreateProjectTool.lwjglVersionDefault
        packageName.text = "com.test.app"
        projectName.text = "Untitled123"

        projectName.onChanged {
            if (isDirectoryNameSame) directory.text = "${Studio.fileChooser.userHomeDirectory}/IdeaProjects/${projectName.text.lowercase()}"
        }

        directory.listener = object : TextField.TextFieldListener {
            override fun keyTyped(textField: TextField?, c: Char) {
                isDirectoryNameSame = false
            }
        }

        content.apply {
            align = Align.topLeft
            add(Label("Project name:")).growX().newRow()
            add(projectName).growX().newRow()

            add(Label("Package:")).growX().padTop(10f).newRow()
            add(packageName).growX().newRow()

            add(Label("Directory path:")).growX().padTop(10f).newRow()
            add(HBox {
                add(directory).growX()
                add(TextButton("...") {
                    onClick {}
                }).padLeft(5f)
            }).growX().newRow()

            add(HBox {
                align = Align.topLeft

                add(Table {
                    align = Align.topLeft
                    CreateProjectTool.lwjglPlatformsAll.iterate {
                        val box = PlainCheckBox()
                        box.isChecked = it.isDefault
                        lwjglNatives.add(box to it)
                        add(box)
                        add(Label(it.name)).growX().padLeft(10f).newRow()
                    }
                })

                add(VBox {
                    align = Align.topLeft
                    add(Label("Kotlin version:"))
                    add(kotlinVersion).width(100f)
                    add(Label("LWJGL version:")).padTop(10f)
                    add(lwjglVersion).width(100f)
                }).align(Align.topLeft).padLeft(10f)

                add(Table {
                    add(mainAppLauncher)
                    add(Label("App launcher (main)")).growX().padLeft(10f).newRow()
                    add(scriptTemplate)
                    add(Label("Script template")).growX().padLeft(10f).newRow()
                    add(openIntellij)
                    add(Label("Open IntelliJ IDEA")).growX().padLeft(10f).newRow()
                }).align(Align.topLeft).padLeft(10f)

            }).padTop(10f).grow().newRow()

            add(TextButton("Create") {
                onClick {
                    if (projectName.isInputValid &&
                        directory.isInputValid &&
                        packageName.isInputValid &&
                        kotlinVersion.isInputValid &&
                        lwjglVersion.isInputValid) {

                        val dir = FS.absolute(directory.text)
                        dir.mkdirs()

                        val mainClass = if (mainAppLauncher.isChecked) {
                            if (packageName.text.isEmpty()) "MainKt" else "${packageName.text}.MainKt"
                        } else "YourClass"

                        val buildGradle = dir.child("build.gradle.kts")
                        buildGradle.writeText(
                            CreateProjectTool.jvmBuildGradle(
                                mainClass,
                                kotlinVersion.text,
                                lwjglVersion.text,
                                lwjglNatives.mapNotNull { if (it.first.isChecked) it.second else null }
                            )
                        )

                        val settingsGradle = dir.child("settings.gradle.kts")
                        settingsGradle.writeText("""rootProject.name = "${projectName.text}"""")

                        val src = dir.child("src")
                        val main = src.child("main")
                        val resources = main.child("resources")
                        resources.mkdirs()

                        val kotlin = main.child("kotlin").also { it.mkdirs() }

                        val packageDirPath = if (packageName.text.isNotEmpty()) {
                            packageName.text.replace('.', '/')
                        } else {
                            ""
                        }
                        val packageDir = if (packageDirPath.isNotEmpty()) {
                            kotlin.child(packageDirPath).also { it.mkdirs() }
                        } else {
                            kotlin
                        }

                        val scriptsKt = packageDir.child("scripts.kt")

                        val packageString = if (packageName.text.isNotEmpty()) "package ${packageName.text}\n\n" else ""

                        if (scriptTemplate.isChecked) {
                            val scriptName = "NewScene.kt"
                            val script = packageDir.child(scriptName)
                            script.writeText(packageString + CreateProjectTool.defaultSceneScript)
                            scriptsKt.writeText(packageString + CreateProjectTool.defaultInitScripts)
                            Studio.createNewApp(projectFile(if (packageDirPath.isEmpty()) scriptName else "$packageDirPath/$scriptName"))
                        } else {
                            scriptsKt.writeText(packageString + CreateProjectTool.emptyInitScripts)
                            Studio.createNewApp()
                        }

                        if (mainAppLauncher.isChecked) {
                            packageDir.child("main.kt").writeText(
                                packageString + CreateProjectTool.mainAppLauncherDefault(projectName.text, true)
                            )
                        }

                        val app = resources.child("app.thelema")
                        Studio.saveApp(app)
                    }
                }
            })
        }

        pack()
        width = 480f
        height = 480f
        isResizable = true
    }

    override fun show() {
        super.show()
        directory.text = "${Studio.fileChooser.userHomeDirectory}/IdeaProjects/${projectName.text.lowercase()}"
    }
}