package app.thelema.android

import android.media.MediaPlayer
import app.thelema.audio.IMusic
import app.thelema.gl.GL
import app.thelema.utils.LOG
import java.io.IOException
import kotlin.math.abs

class AndroidMusic internal constructor(private var player: MediaPlayer) :
    IMusic, MediaPlayer.OnCompletionListener {

    private var isPrepared = true
    var wasPlaying = false
    override var volume = 1f
        set(value) {
            player.setVolume(value, value)
        }
    override var onCompletionListener: IMusic.OnCompletionListener? = null

    override fun destroy() {
        try {
            player.release()
        } catch (t: Throwable) {
            LOG.error("error while disposing AndroidMusic instance, non-fatal")
        } finally {
            onCompletionListener = null
        }
    }

    // NOTE: isLooping() can potentially throw an exception and crash the application
    override var isLooping: Boolean
        get() = try {
            player.isLooping
        } catch (e: Exception) {
            // NOTE: isLooping() can potentially throw an exception and crash the application
            e.printStackTrace()
            false
        }
        set(isLooping) {
            player.isLooping = isLooping
        }

    // NOTE: isPlaying() can potentially throw an exception and crash the application
    override val isPlaying: Boolean
        get() = try {
            player.isPlaying
        } catch (e: Exception) {
            // NOTE: isPlaying() can potentially throw an exception and crash the application
            e.printStackTrace()
            false
        }

    override fun pause() {
        try {
            if (player.isPlaying) {
                player.pause()
            }
        } catch (e: Exception) {
            // NOTE: isPlaying() can potentially throw an exception and crash the application
            e.printStackTrace()
        }
        wasPlaying = false
    }

    override fun play() {
        try {
            if (player.isPlaying) return
        } catch (e: Exception) {
            // NOTE: isPlaying() can potentially throw an exception and crash the application
            e.printStackTrace()
            return
        }
        try {
            if (!isPrepared) {
                player.prepare()
                isPrepared = true
            }
            player.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun setPan(pan: Float) {
        var leftVolume = volume
        var rightVolume = volume
        if (pan < 0) {
            rightVolume *= 1 - abs(pan)
        } else if (pan > 0) {
            leftVolume *= 1 - abs(pan)
        }
        player.setVolume(leftVolume, rightVolume)
    }

    override fun stop() {
        if (isPrepared) {
            player.seekTo(0)
        }
        player.stop()
        isPrepared = false
    }

    override var position: Float
        get() = player.currentPosition / 1000f
        set(position) {
            try {
                if (!isPrepared) {
                    player.prepare()
                    isPrepared = true
                }
                player.seekTo((position * 1000).toInt())
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    val duration: Float
        get() = player.duration / 1000f

    override fun onCompletion(mp: MediaPlayer?) {
        if (onCompletionListener != null) {
            GL.call { onCompletionListener?.onCompletion(this@AndroidMusic) }
        }
    }

    init {
        onCompletionListener = null
        this.player.setOnCompletionListener(this)
    }
}