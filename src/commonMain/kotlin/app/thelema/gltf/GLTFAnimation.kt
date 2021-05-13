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

package app.thelema.gltf

import app.thelema.anim.*
import app.thelema.data.IFloatData
import app.thelema.ecs.ECS
import app.thelema.ecs.component
import app.thelema.math.IVec
import app.thelema.math.Vec3
import app.thelema.math.Vec4
import app.thelema.math.VecN

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-animation)
 *
 * @author zeganstyl */
class GLTFAnimation(array: IGLTFArray): GLTFArrayElementAdapter(array) {
    override val defaultName: String
        get() = "Animation"

    val animationSamplers = ArrayList<GLTFAnimationSampler>()

    val animation: IAnimation = gltf.animationsEntity.entity("${defaultName}_$elementIndex").component()

    private fun asFloatBuffer(accessorIndex: Int, loaded: (view: IFloatData) -> Unit) {
        val accessor = gltf.accessors[accessorIndex] as GLTFAccessor

        accessor.getOrWaitSetupBuffer { bytes ->
            val view = bytes.floatView()
            view.limit = accessor.size() / 4

            loaded(view)
        }
    }

    override fun initProgress() {
        super.initProgress()
        maxProgress += json.array("channels").size
    }

    override fun readJson() {
        super.readJson()

        if (name != defaultName) animation.entity.name = gltf.animationsEntity.makeChildName(name)

        animationSamplers.clear()
        json.array("samplers") {
            objs {
                animationSamplers.add(GLTFAnimationSampler().apply { readJson(this@objs) })
            }
        }

        // https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-channel
        json.array("channels") {
            objs {
                val animationSampler = animationSamplers[int("sampler")]

                asFloatBuffer(animationSampler.input) { inputData ->
                    asFloatBuffer(animationSampler.output) { outputBuffer ->
                        val inputAccessor = gltf.accessors[animationSampler.input] as GLTFAccessor
                        animation.duration = kotlin.math.max(animation.duration, inputAccessor.max!![0])

                        // https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-target
                        get("target") {
                            val nodeIndex = int("node")

                            val path = string("path")

                            val interpolation = when (animationSampler.interpolation) {
                                "LINEAR" -> AnimInterpolation.Linear
                                "STEP" -> AnimInterpolation.Step
                                "CUBICSPLINE" -> AnimInterpolation.CubicSpline
                                else -> AnimInterpolation.Unknown
                            }

                            when (path) {
                                "translation" -> {
                                    val track = Vec3Track(interpolation, nodeIndex)
                                    val values = track.values
                                    val times = track.times
                                    val elementSize = 3

                                    if (interpolation == AnimInterpolation.CubicSpline) {
                                        val inTangents = track.inTangents
                                        val outTangents = track.outTangents
                                        for (k in 0 until inputData.limit) {
                                            times.add(if (k == 0 && inputData[k] > 0f) 0f else inputData[k])
                                            inTangents.add(vec3(outputBuffer, k * elementSize))
                                            values.add(vec3(outputBuffer, (k+1) * elementSize))
                                            outTangents.add(vec3(outputBuffer, (k+2) * elementSize))
                                        }
                                    } else {
                                        for (k in 0 until inputData.limit) {
                                            times.add(if (k == 0 && inputData[k] > 0f) 0f else inputData[k])
                                            values.add(vec3(outputBuffer, k * elementSize))
                                        }
                                    }

                                    track.calculateDuration()
                                    animation.translationTracks.add(track)
                                }

                                "rotation" -> {
                                    val track = Vec4Track(interpolation, nodeIndex)
                                    val values = track.values
                                    val times = track.times
                                    val elementSize = 4

                                    if (interpolation == AnimInterpolation.CubicSpline) {
                                        val inTangents = track.inTangents
                                        val outTangents = track.outTangents
                                        for (k in 0 until inputData.limit) {
                                            times.add(if (k == 0 && inputData[k] > 0f) 0f else inputData[k])
                                            inTangents.add(vec4(outputBuffer, k * elementSize))
                                            values.add(vec4(outputBuffer, (k+1) * elementSize))
                                            outTangents.add(vec4(outputBuffer, (k+2) * elementSize))
                                        }
                                    } else {
                                        for (k in 0 until inputData.limit) {
                                            times.add(if (k == 0 && inputData[k] > 0f) 0f else inputData[k])
                                            values.add(vec4(outputBuffer, k * elementSize))
                                        }
                                    }

                                    track.calculateDuration()
                                    animation.rotationTracks.add(track)
                                }

                                "scale" -> {
                                    val track = Vec3Track(interpolation, nodeIndex)
                                    val values = track.values
                                    val times = track.times
                                    val elementSize = 3

                                    if (interpolation == AnimInterpolation.CubicSpline) {
                                        val inTangents = track.inTangents
                                        val outTangents = track.outTangents
                                        for (k in 0 until inputData.limit) {
                                            times.add(if (k == 0 && inputData[k] > 0f) 0f else inputData[k])
                                            inTangents.add(vec3(outputBuffer, k * elementSize))
                                            values.add(vec3(outputBuffer, (k+1) * elementSize))
                                            outTangents.add(vec3(outputBuffer, (k+2) * elementSize))
                                        }
                                    } else {
                                        for (k in 0 until inputData.limit) {
                                            times.add(if (k == 0 && inputData[k] > 0f) 0f else inputData[k])
                                            values.add(vec3(outputBuffer, k * 3))
                                        }
                                    }

                                    track.calculateDuration()
                                    animation.scaleTracks.add(track)

                                    // calculation must be for every channel because of asynchronous mode
                                    animation.calculateDuration()
                                }
                            }
                        }

                        currentProgress++
                    }
                }
            }
        }

        ready()
    }

    override fun destroy() {
        animationSamplers.clear()
        animation.destroy()
    }

    companion object {
        fun vec4(src: IFloatData, floatStart: Int = 0) = Vec4(src[floatStart], src[floatStart+1], src[floatStart+2], src[floatStart+3])
        fun vec3(src: IFloatData, floatStart: Int = 0) = Vec3(src[floatStart], src[floatStart+1], src[floatStart+2])
        fun vector(src: IFloatData, count: Int, floatStart: Int = 0): IVec {
            val weightVector = VecN(count)
            for (i in 0 until count) {
                weightVector.values[i] = src[floatStart + i]
            }

            return weightVector
        }
    }
}