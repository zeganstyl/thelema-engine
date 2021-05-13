package app.thelema.android

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import app.thelema.audio.IAudioRecorder

/** [IAudioRecorder] implementation for the android system based on AudioRecord
 * @author badlogicgames@gmail.com
 */
class AndroidAudioRecorder(samplingRate: Int, isMono: Boolean) : IAudioRecorder {
    /** the audio track we read samples from  */
    private val recorder: AudioRecord
    override fun dispose() {
        recorder.stop()
        recorder.release()
    }

    override fun read(samples: ShortArray, offset: Int, numSamples: Int) {
        var read = 0
        while (read != numSamples) {
            read += recorder.read(samples, offset + read, numSamples - read)
        }
    }

    init {
        val channelConfig: Int = if (isMono) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO
        val minBufferSize: Int =
            AudioRecord.getMinBufferSize(samplingRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT)
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC, samplingRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize
        )
        if (recorder.state != AudioRecord.STATE_INITIALIZED) throw IllegalStateException("Unable to initialize AudioRecorder.\nDo you have the RECORD_AUDIO permission?")
        recorder.startRecording()
    }
}
