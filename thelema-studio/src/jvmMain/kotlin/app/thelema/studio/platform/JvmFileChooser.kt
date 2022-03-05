package app.thelema.studio.platform

import app.thelema.app.APP
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile
import app.thelema.jvm.JvmFile
import app.thelema.lwjgl3.JvmApp
import app.thelema.res.APP_ROOT_FILENAME
import app.thelema.res.RES
import app.thelema.studio.Studio
import app.thelema.studio.ecs.KotlinScripting
import org.lwjgl.util.tinyfd.TinyFileDialogs
import java.awt.Desktop
import java.io.File
import javax.swing.JFileChooser


class JvmFileChooser: IFileChooser {
    override val userHomeDirectory: String
        get() = System.getProperty("user.home") ?: ""

    lateinit var chooser: JFileChooser

    override fun executeCommandInTerminal(directory: IFile, command: List<String>): (out: StringBuilder) -> Boolean {
        val args = ArrayList<String>()
        args.addAll(command)
        val builder = ProcessBuilder(args)
        builder.directory(File(directory.platformPath))

        val process = builder.start()

        val input = process.inputStream
        val errors = process.errorStream

        return { out ->
            val available1 = process.inputStream.available()
            val available2 = process.errorStream.available()
            if (available1 > 0) {
                val array = ByteArray(available1)
                input.read(array)
                String(array).also { out.append(it) }
            }
            if (available2 > 0) {
                val array = ByteArray(available2)
                errors.read(array)
                String(array).also { out.append(it) }
            }
            available1 > 0 || available2 > 0
        }
    }

    override fun openInFileManager(path: String) {
        try {
            if ((APP as JvmApp).isMacOs) {
                Runtime.getRuntime().exec(
                    arrayOf(
                        "/usr/bin/open",
                        path
                    )
                )
            } else {
                Desktop.getDesktop().open(File(path))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Studio.showStatusAlert("Can't open file $path")
        }
    }

    override fun openScriptFile(selectedFile: IFile?, ready: (file: IFile?) -> Unit) {
        val dir = KotlinScripting.kotlinDirectory
        if (dir == null) {
            ready(null)
        } else {
            chooser.isMultiSelectionEnabled = false
            chooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
            chooser.dialogType = JFileChooser.OPEN_DIALOG
            chooser.currentDirectory = File(dir.platformPath)
            chooser.selectedFile = if (selectedFile != null) File(selectedFile.platformPath) else null
            chooser.fileFilter = null
            chooser.requestFocus()
            if (chooser.showDialog(null, null) != JFileChooser.APPROVE_OPTION) return
            val file = chooser.selectedFile
            if (file != null) {
                ready(JvmFile(file, file.relativeTo(File(dir.path)).path, FileLocation.Relative))
            }
        }
    }

    override fun openProjectFile(selectedFile: IFile?, ready: (uri: String) -> Unit) {
        val projectFile = RES.file
        val dir = RES.absoluteDirectory.platformPath
        chooser.isMultiSelectionEnabled = false
        chooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        chooser.dialogType = JFileChooser.OPEN_DIALOG
        chooser.currentDirectory = File(dir)
        chooser.selectedFile = if (selectedFile != null) File(selectedFile.platformPath) else null
        chooser.fileFilter = null
        if (chooser.showDialog(null, null) != JFileChooser.APPROVE_OPTION) return
        val file = chooser.selectedFile
        if (file != null) {
            ready(file.relativeTo(File(projectFile.parent().path)).path)
        }
    }

    override fun openProject(ready: (uri: String) -> Unit) {
        TinyFileDialogs.tinyfd_openFileDialog(
            "Open App",
            "$userHomeDirectory/IdeaProjects/.",
            null,
            APP_ROOT_FILENAME,
            false
        )?.also(ready)
    }

    override fun saveProject(ready: (uri: String) -> Unit) {
        TinyFileDialogs.tinyfd_saveFileDialog(
            "Save App",
            "$userHomeDirectory/IdeaProjects/thelema.app",
            null,
            APP_ROOT_FILENAME
        )?.also(ready)
    }
}
