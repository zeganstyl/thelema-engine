package app.thelema.studio

import app.thelema.fs.IFile

interface IFileChooser {
    val userHomeDirectory: String

    val ideaProjectsDirectory: String
        get() = "$userHomeDirectory/IdeaProjects"

    fun executeCommandInTerminal(directory: IFile, command: List<String>): (out: StringBuilder) -> Boolean

    fun openInFileManager(path: String)

    fun openScriptFile(selectedFile: IFile?, ready: (file: IFile?) -> Unit)

    fun openProjectFile(selectedFile: IFile?, ready: (uri: String) -> Unit)

    fun openProject(ready: (uri: String) -> Unit)

    fun saveProject(ready: (uri: String) -> Unit)

    fun destroy() {}
}
