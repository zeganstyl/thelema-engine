package app.thelema.studio

import app.thelema.fs.IFile

interface IFileChooser {
    fun openProjectFile(selectedFile: IFile?, ready: (uri: String) -> Unit)

    fun openProject(ready: (uri: String) -> Unit)

    fun saveProject(ready: (uri: String) -> Unit)

    fun destroy() {}
}
