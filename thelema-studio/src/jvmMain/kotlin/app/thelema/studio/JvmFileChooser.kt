package app.thelema.studio

import app.thelema.fs.IFile
import app.thelema.res.RES
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

class JvmFileChooser: IFileChooser {
    private val chooser: JFileChooser by lazy { JFileChooser() }

    private val projectFilter = object : FileFilter() {
        override fun accept(f: File): Boolean {
            return File(f.absolutePath + "project.thelema").exists()
        }

        override fun getDescription(): String = "Thelema Engine project (project.thelema)"
    }

    override fun openProjectFile(selectedFile: IFile?, ready: (uri: String) -> Unit) {
        val projectFile = RES.file ?: throw IllegalStateException("Project is not opened")
        val dir = RES.absoluteDirectory!!.platformPath
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