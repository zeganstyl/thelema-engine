package app.thelema.android

import android.content.res.AssetFileDescriptor
import android.media.SoundPool
import app.thelema.audio.AL
import app.thelema.audio.ISoundLoader
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile
import app.thelema.res.LoaderAdapter
import java.io.IOException
import kotlin.math.abs

class AndroidSoundLoader : LoaderAdapter(), ISoundLoader {
    val soundPool: SoundPool
        get() = (AL as AndroidAudio).soundPool

    val streamIds = ArrayList<Int>(8)

    override val duration: Float
        get() = 0f

    override val componentName: String
        get() = "SoundLoader"

    var soundId: Int = -1

    override fun loadBase(file: IFile) {
        if (file.location == FileLocation.Internal || file.location == FileLocation.Project) {
            try {
                val descriptor: AssetFileDescriptor = (AL as AndroidAudio).context.assets.openFd(file.path)
                soundId = soundPool.load(descriptor, 1)
                descriptor.close()
            } catch (ex: IOException) {
                throw IllegalStateException(
                    "Error loading audio file: " + file
                            + "\nNote: Internal audio files must be placed in the assets directory.", ex
                )
            }
        } else {
            try {
                soundId = soundPool.load(file.path, 1)
            } catch (ex: Exception) {
                throw IllegalStateException("Error loading audio file: $file", ex)
            }
        }
    }

    override fun play(volume: Float, pitch: Float, pan: Float, loop: Boolean): Int {
        if (streamIds.size == 8) streamIds.removeLast()

        var leftVolume = volume
        var rightVolume = volume
        if (pan < 0) {
            rightVolume *= 1 - abs(pan)
        } else if (pan > 0) {
            leftVolume *= 1 - abs(pan)
        }

        val streamId: Int = soundPool.play(soundId, leftVolume, rightVolume, 1, if (loop) -1 else 0, pitch)
        // standardise error code with other backends
        if (streamId == 0) return -1
        streamIds.add(0, streamId)
        return streamId
    }

    override fun stopSound() {
        var i = 0
        val n = streamIds.size
        while (i < n) {
            soundPool.stop(streamIds[i])
            i++
        }
    }

    override fun stopSound(soundId: Int) {
        soundPool.stop(soundId)
    }

    override fun pause() {
        soundPool.autoPause()
    }

    override fun pause(soundId: Int) {
        soundPool.pause(soundId)
    }

    override fun resume() {
        soundPool.autoResume()
    }

    override fun resume(soundId: Int) {
        soundPool.resume(soundId)
    }

    override fun setPitch(soundId: Int, pitch: Float) {
        soundPool.setRate(soundId, pitch)
    }

    override fun setVolume(soundId: Int, volume: Float) {
        soundPool.setVolume(soundId, volume, volume)
    }

    fun loop(volume: Float = 1f): Int {
        if (streamIds.size == 8) streamIds.removeLast()
        val streamId: Int = soundPool.play(soundId, volume, volume, 1, -1, 1f)
        // standardise error code with other backends
        if (streamId == 0) return -1
        streamIds.add(0, streamId)
        return streamId
    }

    override fun setLooping(soundId: Int, looping: Boolean) {
        val streamId = soundId
        soundPool.pause(streamId)
        soundPool.setLoop(streamId, if (looping) -1 else 0)
        soundPool.resume(streamId)
    }

    override fun setPan(soundId: Int, pan: Float) {
        // TODO
//        var leftVolume = volume
//        var rightVolume = volume
//        if (pan < 0) {
//            rightVolume *= 1 - abs(pan)
//        } else if (pan > 0) {
//            leftVolume *= 1 - abs(pan)
//        }
//        soundPool.setVolume(soundId, leftVolume, rightVolume)
    }

    fun play(volume: Float, pitch: Float, pan: Float): Long {
        if (streamIds.size == 8) streamIds.removeLast()
        var leftVolume = volume
        var rightVolume = volume
        if (pan < 0) {
            rightVolume *= 1 - abs(pan)
        } else if (pan > 0) {
            leftVolume *= 1 - abs(pan)
        }
        val streamId: Int = soundPool.play(soundId, leftVolume, rightVolume, 1, 0, pitch)
        // standardise error code with other backends
        if (streamId == 0) return -1
        streamIds.add(0, streamId)
        return streamId.toLong()
    }

    fun loop(volume: Float, pitch: Float, pan: Float): Long {
        if (streamIds.size == 8) streamIds.removeLast()
        var leftVolume = volume
        var rightVolume = volume
        if (pan < 0) {
            rightVolume *= 1 - abs(pan)
        } else if (pan > 0) {
            leftVolume *= 1 - abs(pan)
        }
        val streamId: Int = soundPool.play(soundId, leftVolume, rightVolume, 1, -1, pitch)
        // standardise error code with other backends
        if (streamId == 0) return -1
        streamIds.add(0, streamId)
        return streamId.toLong()
    }

    override fun destroy() {
        soundPool.unload(soundId)
    }
}