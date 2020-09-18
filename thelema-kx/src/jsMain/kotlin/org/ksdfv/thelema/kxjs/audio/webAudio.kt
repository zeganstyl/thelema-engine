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

package org.ksdfv.thelema.kxjs.audio

import org.khronos.webgl.Float32Array
import org.w3c.dom.HTMLMediaElement
import org.w3c.dom.events.EventTarget

public external abstract class AudioListener {
    var positionX: Float
    var positionY: Float
    var positionZ: Float
    var forwardX: Float
    var forwardY: Float
    var forwardZ: Float
    var upX: Float
    var upY: Float
    var upZ: Float

    fun setOrientation(x: Float, y: Float, z: Float)
    fun setPosition(x: Float, y: Float, z: Float)
}

/** [Web Audio API](https://developer.mozilla.org/en-US/docs/Web/API/AudioBuffer) */
public external abstract class AudioBuffer {
    val sampleRate: Int
    val length: Int
    val duration: Double
    val numberOfChannels: Int

    fun getChannelData(channel: Int): Float32Array
    fun copyFromChannel(destination: Float32Array, channelNumber: Int, startInChannel: Int = definedExternally)
    fun copyToChannel(source: Float32Array, channelNumber: Int, startInChannel: Int = definedExternally)
}

/** @author zeganstyl */
public external abstract class AudioNode: EventTarget {
    val context: AudioContext
    val numberOfInputs: Int
    val numberOfOutputs: Int
    var channelCount: Int
    var channelCountMode: String
    var channelInterpretation: String

    fun connect(destination: AudioNode, outputIndex: Int = definedExternally, inputIndex: Int = definedExternally)
    fun connect(destination: AudioParam, outputIndex: Int = definedExternally, inputIndex: Int = definedExternally)

    fun disconnect(destination: AudioNode, outputIndex: Int = definedExternally, inputIndex: Int = definedExternally)
    fun disconnect(outputIndex: Int)
    fun disconnect()
}

/** [Web Audio API](https://developer.mozilla.org/en-US/docs/Web/API/AudioBufferSourceNode) */
public external abstract class AudioBufferSourceNode: AudioNode {
    var buffer: AudioBuffer?
    var detune: Float
    var loop: Boolean
    var loopStart: Double
    var loopEnd: Double
    var playbackRate: Float
    fun start(`when`: Double = definedExternally, offset: Double = definedExternally, duration: Double = definedExternally)
}

public external abstract class AudioParam {
    val defaultValue: Any?
    val maxValue: Any?
    val minValue: Any?
    var value: Any?

    fun setValueAtTime(value: Float, startTime: Double)
    fun linearRampToValueAtTime(value: Float, endTime: Double)
    fun exponentialRampToValueAtTime(value: Float, endTime: Double)
    fun setTargetAtTime(target: Any, startTime: Double, timeConstant: Double)
    fun setValueCurveAtTime(values: Array<Float>, startTime: Double, duration: Double)
    fun cancelScheduledValues(startTime: Double)
    fun cancelAndHoldAtTime(cancelTime: Double)
}

public external abstract class AudioDestinationNode: AudioNode {
    var maxChannelCount: Long
}

/** [Web Audio API](https://developer.mozilla.org/en-US/docs/Web/API/BaseAudioContext) */
public external abstract class BaseAudioContext: EventTarget {
    val currentTime: Double
    val destination: AudioDestinationNode
    val listener: AudioListener
    val sampleRate: Float
    val state: String

    fun createBuffer(numOfchannels: Int, length: Int, sampleRate: Int): AudioBuffer
    fun createBufferSource(): AudioBufferSourceNode
    fun createMediaElementSource(myMediaElement: HTMLMediaElement): MediaElementAudioSourceNode
    fun createPanner(): PannerNode
    fun createGain(): GainNode
    fun createStereoPanner(): StereoPannerNode
}

public external class AudioContext() : BaseAudioContext {
    val baseLatency: Float
    val outputLatency: Float

    fun close()
}

/** @author zeganstyl */
public external abstract class MediaElementAudioSourceNode: AudioNode

public external abstract class GainNode: AudioNode {
    val gain: AudioParam
}

/** [Web Audio API](https://developer.mozilla.org/en-US/docs/Web/API/PannerNode)
 *
 * @author zeganstyl */
public external abstract class PannerNode: AudioNode {
    var coneInnerAngle: Double
    var coneOuterAngle: Double
    var coneOuterGain: Double
    var distanceModel: String
    var maxDistance: Double
    var orientationX: Float
    var orientationY: Float
    var orientationZ: Float
    var panningModel: String
    var positionX: Float
    var positionY: Float
    var positionZ: Float
    var refDistance: Double
    var rolloffFactor: Double
}

/** [Web Audio API](https://developer.mozilla.org/en-US/docs/Web/API/StereoPannerNode) */
public external abstract class StereoPannerNode: AudioNode {
    val pan: AudioParam
}
