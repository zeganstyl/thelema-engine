package app.thelema.android

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import app.thelema.audio.*
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile

class AndroidAudio(val context: Context, maxSimultaneousSounds: Int = 16): IAudio {
    override fun newAudioDevice(samplingRate: Int, channelsNum: Int): IAudioDevice {
        return AndroidAudioDevice(samplingRate, channelsNum)
    }

    override fun newAudioRecorder(samplingRate: Int, isMono: Boolean): IAudioRecorder {
        return AndroidAudioRecorder(samplingRate, isMono)
    }

    override fun newMusic(file: IFile): IMusic {
        val mediaPlayer = MediaPlayer()
        return if (file.location == FileLocation.Internal) {
            try {
                val descriptor: AssetFileDescriptor = context.assets.openFd(file.path)
                mediaPlayer.setDataSource(
                    descriptor.fileDescriptor,
                    descriptor.startOffset,
                    descriptor.length
                )
                descriptor.close()
                mediaPlayer.prepare()
                val music = AndroidMusic(mediaPlayer)
                synchronized(musics) { musics.add(music) }
                music
            } catch (ex: Exception) {
                throw IllegalStateException(
                    ("Error loading audio file: " + file
                            + "\nNote: Internal audio files must be placed in the assets directory."), ex
                )
            }
        } else {
            try {
                mediaPlayer.setDataSource(file.path)
                mediaPlayer.prepare()
                val music = AndroidMusic(mediaPlayer)
                synchronized(musics) { musics.add(music) }
                music
            } catch (ex: Exception) {
                throw IllegalStateException("Error loading audio file: $file", ex)
            }
        }
    }

    override fun getVersion(param: Int): String = ""

    override fun update() {}

    override fun destroy() {
        synchronized(musics) {

            val musicsCopy = ArrayList<AndroidMusic>(musics)
            for (music: AndroidMusic in musicsCopy) {
                music.destroy()
            }
        }
        soundPool.release()
    }


    val soundPool: SoundPool = SoundPool.Builder().setAudioAttributes(
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    ).setMaxStreams(maxSimultaneousSounds).build()

    private var manager: AudioManager? = null
    private val musics: MutableList<AndroidMusic> = ArrayList()

    init {
        manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (context is Activity) context.volumeControlStream = AudioManager.STREAM_MUSIC
    }

    fun pause() {
        synchronized(musics) {
            for (music: AndroidMusic in musics) {
                if (music.isPlaying) {
                    music.pause()
                    music.wasPlaying = true
                } else music.wasPlaying = false
            }
        }
        soundPool.autoPause()
    }

    fun resume() {
        synchronized(musics) {
            for (i in musics.indices) {
                if (musics[i].wasPlaying) musics[i].play()
            }
        }
        soundPool.autoResume()
    }
}