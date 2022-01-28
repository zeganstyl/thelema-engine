package app.thelema.lwjgl3.audio

import app.thelema.audio.ISound
import app.thelema.audio.ISoundLoader
import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNode
import app.thelema.g3d.TransformNodeListener
import org.lwjgl.openal.AL10

class Sound: ISound {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            node = value?.component() ?: TransformNode()
        }

    override val componentName: String
        get() = "Sound"

    override val sourceId: Int = AL10.alGenSources()

    private val nodeListener = object : TransformNodeListener {
        override fun worldMatrixChanged(node: ITransformNode) {
            updatePosition()
        }
    }

    override var soundLoader: ISoundLoader? = null
        set(value) {
            if (field != value) {
                field = value
                value?.onLoaded {
                    if (mustPlay) play()
                }
                value?.load()
            }
        }

    override var mustPlay: Boolean = false
        set(value) {
            field = value
            val state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE)
            if (value && state != AL10.AL_PLAYING) {
                play()
            } else if (!value && state == AL10.AL_PLAYING) {
                AL10.alSourcePause(sourceId)
            }
        }

    override var node: ITransformNode = TransformNode().apply { addListener(nodeListener) }
        set(value) {
            if (field != value) {
                field.removeListener(nodeListener)
                field = value
                updatePosition()
                value.addListener(nodeListener)
            }
        }

    override var pitch: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                AL10.alSourcef(sourceId, AL10.AL_PITCH, value)
            }
        }

    override var gain: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                AL10.alSourcef(sourceId, AL10.AL_GAIN, value)
            }
        }

    override var isLooped: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                AL10.alSourcei(sourceId, AL10.AL_LOOPING, if (value) AL10.AL_TRUE else AL10.AL_FALSE)
            }
        }

    override fun restart() {
        val state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE)
        if (state != AL10.AL_PLAYING) {
            play()
        } else {
            AL10.alSourceStop(sourceId)
            AL10.alSourcePlay(sourceId)
        }
    }

    private fun updatePosition() {
        node.worldPosition.also { AL10.alSource3f(sourceId, AL10.AL_POSITION, it.x, it.y, it.z) }
    }

    private fun play() {
        soundLoader?.also { loader ->
            (loader as SoundLoader)
            AL10.alSourcei(sourceId, AL10.AL_BUFFER, loader.bufferID)
            AL10.alSourcei(sourceId, AL10.AL_LOOPING, if (isLooped) AL10.AL_TRUE else AL10.AL_FALSE)
            AL10.alSourcef(sourceId, AL10.AL_GAIN, gain)
            AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch)
            updatePosition()
            AL10.alSourcePlay(sourceId)
        }
    }

    override fun playSound() {
        mustPlay = true
    }

    override fun pauseSound() {
        mustPlay = false
    }

    override fun stopSound() {
        AL10.alSourceStop(sourceId)
    }

    override fun destroy() {
        soundLoader = null
        AL10.alSourceStop(sourceId)
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, 0)
        AL10.alDeleteSources(sourceId)
    }
}