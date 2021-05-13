package app.thelema.android

import android.media.SoundPool
import app.thelema.audio.ISound
import kotlin.math.abs

internal class AndroidSound(pool: SoundPool, val soundId: Int) : ISound {
    val soundPool: SoundPool = pool
    val streamIds = ArrayList<Int>(8)

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

    override fun stop() {
        var i = 0
        val n = streamIds.size
        while (i < n) {
            soundPool.stop(streamIds[i])
            i++
        }
    }

    override fun stop(soundId: Int) {
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