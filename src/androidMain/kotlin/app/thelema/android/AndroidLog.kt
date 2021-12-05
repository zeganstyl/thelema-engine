package app.thelema.android

import android.util.Log
import app.thelema.utils.ILog

class AndroidLog: ILog {
    override var infoEnabled: Boolean = true
    override var debugEnabled: Boolean = true
    override var errorEnabled: Boolean = true
    override var throwExceptionOnError: Boolean = false

    override fun info(message: String, exception: Throwable?, tag: String) {
        if (infoEnabled) Log.i(tag, message, exception)
    }

    override fun debug(message: String, exception: Throwable?, tag: String) {
        if (debugEnabled) Log.d(tag, message, exception)
    }

    override fun error(message: String, exception: Throwable?, tag: String) {
        if (errorEnabled) Log.e(tag, message, exception)
    }
}