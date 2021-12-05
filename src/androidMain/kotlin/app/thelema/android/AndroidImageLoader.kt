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
import app.thelema.img.IImage
import app.thelema.img.IImageLoader
import app.thelema.utils.LOG
import java.io.InputStream
import java.nio.ByteBuffer

class AndroidImageLoader(val app: AndroidApp) : IImageLoader {
    override fun load(
        uri: String,
        mimeType: String,
        out: IImage,
        location: String,
        error: (status: Int) -> Unit,
        ready: (img: IImage) -> Unit
    ): IImage {
        load(AndroidFile(app.fs, uri, location).inputStream(), out)
        ready(out)
        return out
    }

    override fun load(
        file: IFile,
        mimeType: String,
        out: IImage,
        error: (status: Int) -> Unit,
        ready: (img: IImage) -> Unit
    ): IImage {
        if (file is AndroidFile) {
            load(file.inputStream(), out)
            ready(out)
        } else {
            file.readBytes(
                error = error,
                ready = {
                    decode(it, mimeType, out, error = error, ready = ready)
                }
            )
        }
        return out
    }

    private fun decodeBitmap(bitmap: Bitmap, out: IImage) {
        when (bitmap.config) {
            Bitmap.Config.ALPHA_8 -> {
                out.internalFormat = GL_LUMINANCE
                out.pixelFormat = GL_LUMINANCE
            }
            Bitmap.Config.ARGB_8888 -> {
                out.internalFormat = GL_RGBA
                out.pixelFormat = GL_RGBA
            }
//            Bitmap.Config.RGBA_F16 -> {
//                out.glInternalFormat = GL_RGBA16F
//                out.glPixelFormat = GL_RGBA
//            }
            Bitmap.Config.RGB_565 -> {
                out.internalFormat = GL_RGB565
                out.pixelFormat = GL_RGB
            }
            else -> throw IllegalArgumentException("Unknown image format ${bitmap.config}")
        }

        out.width = bitmap.width
        out.height = bitmap.height

        val size = bitmap.rowBytes * bitmap.height
        val bytes = DATA.bytes(size)
        val byteBuffer = bytes.sourceObject as ByteBuffer

        bitmap.copyPixelsToBuffer(byteBuffer)
        bytes.rewind()
        out.bytes = bytes
    }

    private fun load(stream: InputStream, out: IImage) {
        val bitmap = BitmapFactory.decodeStream(stream)
        if (bitmap == null) {
            LOG.error("AndroidImageLoader: can't decode image ${out.path}")
        } else {
            decodeBitmap(bitmap, out)
        }
    }

    override fun decode(
        data: IByteData,
        mimeType: String,
        out: IImage,
        error: (status: Int) -> Unit,
        ready: (img: IImage) -> Unit
    ): IImage {
        val array = data.toByteArray()
        decodeBitmap(BitmapFactory.decodeByteArray(array, 0, array.size), out)
        ready(out)
        return out
    }
}