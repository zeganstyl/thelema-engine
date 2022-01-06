/*
 * Copyright 2020-2021 Anton Trushkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.thelema.js.audio

import kotlinx.browser.document
import app.thelema.audio.IMusic
import app.thelema.audio.ISoundLoader
import app.thelema.utils.Pool
import org.w3c.dom.HTMLAudioElement

/** @author zeganstyl */
class JsSoundLoader(val al: JsAL, val src: String): ISoundLoader, IMusic {
    var pool: Pool<SoundInstance> = Pool {
        val element = document.createElement("audio") as HTMLAudioElement
        val source = al.context.createMediaElementSource(element)
        val gain = al.context.createGain()
        val panner = al.context.createStereoPanner()
        SoundInstance(element, source, gain, panner)
    }

    override var isMuted: Boolean
        get() = super<ISoundLoader>.isMuted
        set(value) {}

    override var isPlaying: Boolean = false
        private set

    override var isLooping: Boolean
        get() = (pool.used.getOrNull(0) ?: pool.get()).element.loop
        set(value) {
            (pool.used.getOrNull(0) ?: pool.get()).element.loop = value
        }

    override var volume: Float = 1f
        set(value) {
            field = value
            for (i in 0 until pool.used.size) {
                pool.used[i].gain.gain.value = value
            }
        }

    override var position: Float = 0f

    override var onCompletionListener: IMusic.OnCompletionListener? = null

    init {
        pool = Pool {
            val element = document.createElement("audio") as HTMLAudioElement
            element.src = src
            element.autoplay = true
            val source = al.context.createMediaElementSource(element)
            val gain = al.context.createGain()
            val panner = al.context.createStereoPanner()
            source.connect(gain)
            gain.connect(panner)
            panner.connect(al.context.destination)
            val instance = SoundInstance(element, source, gain, panner)
            element.onended = { pool.free(instance) }
            instance
        }
    }

    override fun play() {
        (pool.used.getOrNull(0) ?: pool.get()).element.play()
    }

    override fun setPan(pan: Float) {
        (pool.used.getOrNull(0) ?: pool.get()).panner.pan.value = pan
    }

    override fun play(volume: Float, pitch: Float, pan: Float, loop: Boolean): Int {
        val instance = pool.get()
        instance.gain.gain.value = volume
        instance.panner.pan.value = pan
        instance.element.loop = loop
        instance.element.play()
        return pool.used.indexOf(instance)
    }

    override fun stop() {
        val used = pool.used
        for (i in used.indices) {
            val element = used[i].element
            element.pause()
            element.currentTime = 0.0
        }
    }

    override fun pause() {
        val used = pool.used
        for (i in used.indices) {
            val element = used[i].element
            element.pause()
        }
    }

    override fun resume() {
        val used = pool.used
        for (i in used.indices) {
            val element = used[i].element
            element.play()
        }
    }

    override fun stop(soundId: Int) {
        val element = pool.used.getOrNull(soundId)?.element
        if (element != null) {
            element.pause()
            element.currentTime = 0.0
        }
    }

    override fun pause(soundId: Int) {
        pool.used.getOrNull(soundId)?.element?.pause()
    }

    override fun resume(soundId: Int) {
        pool.used.getOrNull(soundId)?.element?.play()
    }

    override fun setLooping(soundId: Int, looping: Boolean) {
        pool.used.getOrNull(soundId)?.element?.loop = looping
    }

    override fun setPitch(soundId: Int, pitch: Float) {}

    override fun setVolume(soundId: Int, volume: Float) {
        pool.used.getOrNull(soundId)?.gain?.gain?.value = volume
    }

    override fun setPan(soundId: Int, pan: Float) {
        pool.used.getOrNull(soundId)?.panner?.pan?.value = pan
    }

    override fun destroy() {
        val used = pool.used
        for (i in used.indices) {
            val instance = used[i]
            instance.gain.disconnect()
            instance.panner.disconnect()
            instance.source.disconnect()
            instance.element.remove()
        }
        pool.clear()
    }
}