package app.thelema.android

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import app.thelema.audio.*
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile
import java.io.IOException


class AndroidAL(val context: Context, maxSimultaneousSounds: Int = 16): IAudio {
    override fun newAudioDevice(samplingRate: Int, channelsNum: Int): IAudioDevice {
        return app.thelema.android.AndroidAudioDevice(samplingRate, channelsNum)
    }

    override fun newAudioRecorder(samplingRate: Int, isMono: Boolean): IAudioRecorder {
        return app.thelema.android.AndroidAudioRecorder(samplingRate, isMono)
    }

    override fun newSound(file: IFile): ISoundLoader {
        val androidSound: app.thelema.android.AndroidSoundLoader
        if (file.location == FileLocation.Internal) {
            try {
                val descriptor: AssetFileDescriptor = context.assets.openFd(file.path)
                androidSound = app.thelema.android.AndroidSoundLoader(soundPool, soundPool.load(descriptor, 1))
                descriptor.close()
            } catch (ex: IOException) {
                throw IllegalStateException(
                    "Error loading audio file: " + file
                            + "\nNote: Internal audio files must be placed in the assets directory.", ex
                )
            }
        } else {
            try {
                androidSound = app.thelema.android.AndroidSoundLoader(soundPool, soundPool.load(file.path, 1))
            } catch (ex: Exception) {
                throw IllegalStateException("Error loading audio file: $file", ex)
            }
        }
        return androidSound
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
                val music = app.thelema.android.AndroidMusic(mediaPlayer)
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
                val music = app.thelema.android.AndroidMusic(mediaPlayer)
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

            val musicsCopy = ArrayList<app.thelema.android.AndroidMusic>(musics)
            for (music: app.thelema.android.AndroidMusic in musicsCopy) {
                music.destroy()
            }
        }
        soundPool.release()
    }


    private var soundPool: SoundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val audioAttrib: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder().setAudioAttributes(audioAttrib).setMaxStreams(maxSimultaneousSounds).build()
    } else {
        SoundPool(
            maxSimultaneousSounds,
            AudioManager.STREAM_MUSIC,
            0
        ) // srcQuality: the sample-rate converter quality. Currently has no effect. Use 0 for the default.
    }

    private var manager: AudioManager? = null
    private val musics: MutableList<app.thelema.android.AndroidMusic> = ArrayList<app.thelema.android.AndroidMusic>()

    init {
        manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (context is Activity) context.volumeControlStream = AudioManager.STREAM_MUSIC
    }

    fun pause() {
        synchronized(musics) {
            for (music: app.thelema.android.AndroidMusic in musics) {
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