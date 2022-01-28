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

package app.thelema.lwjgl3.audio

import app.thelema.fs.IFile
import app.thelema.jvm.JvmFile
import app.thelema.jvm.data.JvmByteBuffer
import de.jarnbjo.ogg.BasicStream
import de.jarnbjo.ogg.EndOfOggStreamException
import de.jarnbjo.ogg.LogicalOggStream
import de.jarnbjo.ogg.PhysicalOggStream
import de.jarnbjo.vorbis.VorbisStream
import org.lwjgl.BufferUtils
import org.lwjgl.system.MemoryUtil
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

object OpenALCodecs {
    /** Key - file extension in lowercase, value - sound object builder */
    val sound = HashMap<String, (file: IFile, out: SoundLoader) -> Unit>()

    /** Key - file extension in lowercase, value - music object builder */
    val music = HashMap<String, (audio: OpenAL, file: IFile) -> OpenALMusic>()

    init {
        sound["wav"] = ::wav
        sound["ogg"] = ::ogg

        music["wav"] = { audio, file -> WavMusic(audio, file) }
        music["ogg"] = { audio, file -> OggMusic(audio, file) }
    }

    fun wav(file: IFile, out: SoundLoader) {
        var input: WavInputStream? = null
        try {
            input = WavInputStream(file)
            val buffer = ByteBuffer.wrap(input.readBytes())
            buffer.order(ByteOrder.nativeOrder())
            out.setup(buffer, input.channels, input.sampleRate)
        } catch (ex: IOException) {
            throw RuntimeException("Error reading WAV file: $file", ex)
        } finally {
            input?.close()
        }
    }

    fun ogg(file: IFile, out: SoundLoader) {
        // allocate a buffer for data
        val tmpBuffer = ByteBuffer.allocate(4096)

        // number of data bytes written to the wav file
        var len = 0

        run {
            // open the file args[0] for reading and initialize an Ogg stream
            val os: PhysicalOggStream = BasicStream((file as JvmFile).inputStream())

            // get the first logical Ogg stream from the file
            val los = os.logicalStreams.iterator().next() as LogicalOggStream

            if (los.format !== LogicalOggStream.FORMAT_VORBIS) {
                throw IllegalStateException("Only supported Ogg files with Vorbis content.")
            }

            // create a Vorbis stream from the logical Ogg stream
            val vs = VorbisStream(los)
//                val comments = vs.commentHeader
//                println("Ogg file:       " + file.path)
//                println()
//                println("Encoder vendor: " + comments.vendor)
//                println("Title:          " + comments.title)
//                println("Artist:         " + comments.artist)

            try {
                // read pcm data from the vorbis channel and
                // write the data to the wav file
                while (true) {
                    tmpBuffer.rewind()
                    len += vs.readPcm(tmpBuffer as ByteBuffer, 0, tmpBuffer.limit())
                }
            } catch (e: EndOfOggStreamException) {}

            // close the ogg and vorbis streams
            os.close()
        }

        run {
            val os: PhysicalOggStream = BasicStream((file as JvmFile).inputStream())
            val los = os.logicalStreams.iterator().next() as LogicalOggStream
            val vs = VorbisStream(los)
            val fullBuffer = MemoryUtil.memAlloc(len)

            try {
                // read pcm data from the vorbis channel and
                // write the data to the wav file
                while (true) {
                    vs.readPcm(fullBuffer, 0, len)
                }
            } catch (e: EndOfOggStreamException) {
                //println("completed")
            }
            os.close()

            fullBuffer.rewind()
            var i = 0
            while (i < len) {
                // swap from big endian to little endian
                val tB = fullBuffer[i]
                fullBuffer.put(i, fullBuffer[i + 1])
                fullBuffer.put(i + 1, tB)
                i += 2
            }

            fullBuffer.rewind()

            out.setup(fullBuffer, vs.identificationHeader.channels, vs.identificationHeader.sampleRate)
        }
    }
}