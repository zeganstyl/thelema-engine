package app.thelema.android

import app.thelema.fs.IFile
import app.thelema.fs.IFileSystem

class AndroidFS(val app: AndroidApp): IFileSystem {
    override val externalStoragePath: String
        get() = ""
    override val isExternalStorageAvailable: Boolean
        get() = true
    override val isLocalStorageAvailable: Boolean
        get() = true
    override val localStoragePath: String = app.context.filesDir.path

    override fun file(path: String, location: Int): IFile = AndroidFile(this, path, location)
}
