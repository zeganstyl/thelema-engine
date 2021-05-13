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

package app.thelema.audio


/**
 *
 *
 * A Sound is a short audio clip that can be played numerous times in parallel. It's completely loaded into memory so only load
 * small audio files. Call the [dispose] method when you're done using the Sound.
 *
 *
 *
 *
 * Sound instances are created via a call to [IAL.newSound].
 *
 *
 *
 *
 * Calling the [play] or [play] method will return a long which is an id to that instance of the sound. You
 * can use this id to modify the playback of that sound instance.
 *
 *
 *
 *
 * **Note**: any values provided will not be clamped, it is the developer's responsibility to do so
 *
 *
 * @author badlogicgames@gmail.com, zeganstyl
 */
interface ISound {
    var isMuted: Boolean
        get() = false
        set(value) {}

    /** Plays the sound. If the sound is already playing, it will be played again, concurrently.
     * @param volume the volume in the range `[0,1]`
     * @param pitch the pitch multiplier, 1 == default, >1 == faster, <1 == slower, the value has to be between 0.5 and 2.0
     * @param pan panning in the range -1 (full left) to 1 (full right). 0 is center position.
     * @return the id of the sound instance if successful, or -1 on failure.
     */
    fun play(volume: Float = 1f, pitch: Float = 1f, pan: Float = 0f, loop: Boolean = false): Int

    /** Stops playing all instances of this sound.  */
    fun stop()

    /** Pauses all instances of this sound.  */
    fun pause()

    /** Resumes all paused instances of this sound.  */
    fun resume()

    /** Stops the sound instance with the given id.
     * If the sound is no longer playing, this has no effect.
     * @param soundId the sound id
     */
    fun stop(soundId: Int)

    /** Pauses the sound instance with the given id.
     * If the sound is no longer playing, this has no effect.
     * @param soundId the sound id
     */
    fun pause(soundId: Int)

    /** Resumes the sound instance with the given id.
     * If the sound is not paused, this has no effect.
     * @param soundId the sound id
     */
    fun resume(soundId: Int)

    /** Sets the sound instance with the given id to be looping.
     * If the sound is no longer playing this has no effect.s
     * @param soundId the sound id
     * @param looping whether to loop or not.
     */
    fun setLooping(soundId: Int, looping: Boolean)

    /** Changes the pitch multiplier of the sound instance with the given id.
     * If the sound is no longer playing, this has no effect.
     * @param soundId the sound id
     * @param pitch the pitch multiplier, 1 == default, >1 == faster, <1 == slower, the value has to be between 0.5 and 2.0
     */
    fun setPitch(soundId: Int, pitch: Float)

    /** Changes the volume of the sound instance with the given id.
     * If the sound is no longer playing, this has no effect.
     * @param soundId the sound id
     * @param volume the volume in the range 0 (silent) to 1 (max volume).
     */
    fun setVolume(soundId: Int, volume: Float)

    /** Sets the panning and volume of the sound instance with the given id.
     * If the sound is no longer playing, this has no effect.
     * Note that panning only works for mono sounds, not for stereo sounds!
     * @param soundId the sound id
     * @param pan panning in the range -1 (full left) to 1 (full right). 0 is center position.
     */
    fun setPan(soundId: Int, pan: Float)

    fun destroy()
}
