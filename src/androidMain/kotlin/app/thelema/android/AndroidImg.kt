package app.thelema.android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.fs.IFile
import app.thelema.gl.GL_LUMINANCE
import app.thelema.gl.GL_RGB
import app.thelema.gl.GL_RGB565
import app.thelema.gl.GL_RGBA
import app.thelema.img.IImageData
import app.thelema.img.IImg
import app.thelema.jvm.data.JvmByteBuffer
import java.io.InputStream
import java.nio.ByteBuffer

class AndroidImg(val app: AndroidApp) : IImg {
    override fun load(
        uri: String,
        mimeType: String,
        out: IImageData,
        location: Int,
        error: (status: Int) -> Unit,
        ready: (img: IImageData) -> Unit
    ): IImageData {
        load(AndroidFile(app.fs, uri, location).inputStream(), out)
        ready(out)
        return out
    }

    override fun load(
        file: IFile,
        mimeType: String,
        out: IImageData,
        error: (status: Int) -> Unit,
        ready: (img: IImageData) -> Unit
    ): IImageData {
        out.name = file.path
        file.readBytes(error = error, ready = { load(it, mimeType, out, error = error, ready = ready) } )
        return out
    }

    private fun loadFromBitmap(bitmap: Bitmap, out: IImageData) {
        when (bitmap.config) {
            Bitmap.Config.ALPHA_8 -> {
                out.glInternalFormat = GL_LUMINANCE
                out.glPixelFormat = GL_LUMINANCE
            }
            Bitmap.Config.ARGB_8888 -> {
                out.glInternalFormat = GL_RGBA
                out.glPixelFormat = GL_RGBA
            }
//            Bitmap.Config.RGBA_F16 -> {
//                out.glInternalFormat = GL_RGBA16F
//                out.glPixelFormat = GL_RGBA
//            }
            Bitmap.Config.RGB_565 -> {
                out.glInternalFormat = GL_RGB565
                out.glPixelFormat = GL_RGB
            }
            else -> throw IllegalArgumentException("Unknown image format")
        }

        out.width = bitmap.width
        out.height = bitmap.height

        val size = bitmap.rowBytes * bitmap.height
        val bytes = DATA.bytes(size)
        val byteBuffer = bytes.sourceObject as ByteBuffer

        bitmap.copyPixelsToBuffer(byteBuffer)
        bytes.rewind()
        out.bytes = JvmByteBuffer(byteBuffer)
    }

    private fun load(stream: InputStream, out: IImageData) {
        loadFromBitmap(BitmapFactory.decodeStream(stream), out)
    }

    override fun load(
        data: IByteData,
        mimeType: String,
        out: IImageData,
        error: (status: Int) -> Unit,
        ready: (img: IImageData) -> Unit
    ): IImageData {
        load(ByteBufferBackedInputStream(data.sourceObject as ByteBuffer), out)
        ready(out)
        return out
    }
}