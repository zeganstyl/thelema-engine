/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.audio

/**
 *
 *
 * A Music instance represents a streamed audio file. The interface supports pausing, resuming
 * and so on. When you are done with using the Music instance you have to dispose it via the [destroy] method.
 *
 * Music instances are created via [AL.newMusic].
 *
 * **Note**: any values provided will not be clamped, it is the developer's responsibility to do so
 *
 * @author mzechner
 */
interface IMusic {
    /** Starts the play back of the music stream. In case the stream was paused this will resume the play back. In case the music
     * stream is finished playing this will restart the play back.  */
    fun play()

    /** Pauses the play back. If the music stream has not been started yet or has finished playing a call to this method will be
     * ignored.  */
    fun pause()

    /** Stops a playing or paused Music instance. Next time play() is invoked the Music will start from the beginning.  */
    fun stop()

    /** @return whether this music stream is playing
     */
    val isPlaying: Boolean

    /** Sets whether the music stream is looping.
     * This can be called at any time, whether the stream is playing. */
    var isLooping: Boolean

    /** Sets the volume of this music stream.
     * The volume must be given in the range `[0,1]` with 0 being silent and 1 being the maximum volume. */
    var volume: Float

    /** Sets the panning and volume of this music stream.
     * @param pan panning in the range -1 (full left) to 1 (full right). 0 is center position.
     */
    fun setPan(pan: Float)

    /** Returns the playback position in seconds.  */
    /** Set the playback position in seconds.  */
    var position: Float

    /** Needs to be called when the Music is no longer needed.  */
    fun destroy()

    /** Register a callback to be invoked when the end of a music stream has been reached during playback. */
    var onCompletionListener: OnCompletionListener?

    /** Interface definition for a callback to be invoked when playback of a music stream has completed.  */
    interface OnCompletionListener {
        /** Called when the end of a media source is reached during playback.
         *
         * @param music the Music that reached the end of the file
         */
        fun onCompletion(music: IMusic?)
    }
}
