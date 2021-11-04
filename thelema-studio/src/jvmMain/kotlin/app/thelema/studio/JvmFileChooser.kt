package app.thelema.studio

import app.thelema.fs.FileLocation
import app.thelema.fs.IFile
import app.thelema.jvm.JvmFile
import app.thelema.res.PROJECT_FILENAME
import app.thelema.res.RES
import java.awt.Component
import java.awt.Dialog
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.filechooser.FileFilter

class JvmFileChooser: IFileChooser {
    private val chooser: JFileChooser by lazy {
        object : JFileChooser() {
            override fun createDialog(parent: Component?): JDialog {
                return super.createDialog(parent).also {
                    it.isAlwaysOnTop = true
                }
            }
        }
    }

    private val projectFilter = object : FileFilter() {
        override fun accept(f: File): Boolean {
            return File(f.absolutePath + PROJECT_FILENAME).exists()
        }

        override fun getDescription(): String = "Thelema Engine project ($PROJECT_FILENAME)"
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
        chooser.isMultiSelectionEnabled = false
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.dialogType = JFileChooser.OPEN_DIALOG
        chooser.selectedFile = null
        chooser.fileFilter = projectFilter
        if (chooser.showDialog(null, null) != JFileChooser.APPROVE_OPTION) return
        val file = chooser.selectedFile
        if (file != null) ready(file.absolutePath)
    }

    override fun saveProject(ready: (uri: String) -> Unit) {
        chooser.isMultiSelectionEnabled = false
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.dialogType = JFileChooser.SAVE_DIALOG
        if (chooser.showDialog(null, null) != JFileChooser.APPROVE_OPTION) return
        val file = chooser.selectedFile
        if (file != null) ready(file.absolutePath)
    }
}