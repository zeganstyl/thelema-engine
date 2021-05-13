package app.thelema.android

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import app.thelema.audio.IAudioDevice

/** Implementation of the [AudioDevice] interface for Android using the AudioTrack class. You will need to set the permission
 * android.permission.RECORD_AUDIO in your manifest file.
 * @author mzechner
 */
internal class AndroidAudioDevice(
    samplingRate: Int,
    override val channelsNum: Int
) : IAudioDevice {
    /** the audio track  */
    private val track: AudioTrack

    /** the mighty buffer  */
    private var buffer = ShortArray(1024)

    /** the latency in samples  */
    val latency: Int

    override fun destroy() {
        track.stop()
        track.release()
    }

    override fun writeSamples(samples: ShortArray, offset: Int, numSamples: Int) {
        var writtenSamples: Int = track.write(samples, offset, numSamples)
        while (writtenSamples != numSamples) writtenSamples += track.write(
            samples,
            offset + writtenSamples,
            numSamples - writtenSamples
        )
    }

    override fun writeSamples(samples: FloatArray, offset: Int, numSamples: Int) {
        if (buffer.size < samples.size) buffer = ShortArray(samples.size)
        val bound = offset + numSamples
        var i = offset
        var j = 0
        while (i < bound) {
            var fValue = samples[i]
            if (fValue > 1) fValue = 1f
            if (fValue < -1) fValue = -1f
            val value = (fValue * Short.MAX_VALUE).toInt().toShort()
            buffer[j] = value
            i++
            j++
        }
        var writtenSamples: Int = track.write(buffer, 0, numSamples)
        while (writtenSamples != numSamples) writtenSamples += track.write(
            buffer,
            writtenSamples,
            numSamples - writtenSamples
        )
    }

    override fun setVolume(volume: Float) {
        track.setStereoVolume(volume, volume)
    }

    init {
        val minSize: Int = AudioTrack.getMinBufferSize(
            samplingRate,
            if (channelsNum == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        track = AudioTrack(
            AudioManager.STREAM_MUSIC,
            samplingRate,
            if (channelsNum == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            minSize,
            AudioTrack.MODE_STREAM
        )
        track.play()
        latency = minSize / channelsNum
    }
}