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

package org.ksdfv.thelema.g3d.gltf

import org.ksdfv.thelema.THELEMA
import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.data.DataByteOrder
import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.g3d.IObject3D
import org.ksdfv.thelema.g3d.IScene
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_LINEAR
import org.ksdfv.thelema.gl.GL_LINEAR_MIPMAP_LINEAR
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.JSON
import org.ksdfv.thelema.net.NET
import org.ksdfv.thelema.utils.AsyncArrayList
import org.ksdfv.thelema.utils.LOG
import org.ksdfv.thelema.utils.LittleEndianInputStream
import org.ksdfv.thelema.utils.ResourceLoader

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0)
 *
 * @author mgsx, zeganstyl */
open class GLTF(
    var source: Any,
    var sourceType: GLTFSourceType,
    override var directory: IFile
): IGLTF, ResourceLoader() {
    constructor(
        file: IFile,
        sourceType: GLTFSourceType =
            if (file.extension.toLowerCase() == "glb") GLTFSourceType.GLBFile else GLTFSourceType.GLTFFile,
        directory: IFile = file.parent()
    ): this(file as Any, sourceType, directory)

    override var conf = GLTFConf()

    override var generator = ""
    override var version = "2.0"
    override var copyright = ""
    override var minVersion = ""

    override val objects = AsyncArrayList<IObject3D>()

    override val buffers = GLTFArray<GLTFBuffer>("buffers")
    override val bufferViews = GLTFArray<GLTFBufferView>("bufferViews")
    override val accessors = GLTFArray<GLTFAccessor>("accessors")
    override val samplers = GLTFArray<GLTFSampler>("samplers")
    override val images = GLTFArray<GLTFImage>("images")
    override val textures = GLTFArray<GLTFTexture>("textures")
    override val materials: IGLTFArray<IGLTFMaterial> = GLTFArray("materials")
    override val meshes = GLTFArray<GLTFMesh>("meshes")
    override val nodes = GLTFArray<GLTFNode>("nodes")
    override val skins = GLTFArray<GLTFSkin>("skins")
    override val animations = GLTFArray<GLTFAnimation>("animations")
    override val scenes = GLTFArray<GLTFScene>("scenes")
    override val cameras = GLTFArray<GLTFCamera>("cameras")

    override val arrays: MutableList<IGLTFArray<*>> = arrayListOf(
        buffers,
        bufferViews,
        accessors,
        samplers,
        images,
        textures,
        materials,
        meshes,
        nodes,
        skins,
        animations,
        scenes,
        cameras
    )

    override val extra: MutableMap<String, Any> = HashMap()

    override var scene: IScene? = null
    override var mainSceneIndex: Int = -1

    override var isLoading = false
    override var isLoaded = false

    override var loadedElements = 0
    override var maxNumLoadingElements = 0

    override var name: String = ""

    override fun createMaterial(): IGLTFMaterial =
        GLTFMaterial(gltf = this, elementIndex = materials.size)

    override fun load(response: (status: Int) -> Unit) {
        when (sourceType) {
            GLTFSourceType.JSON -> {
                read(source as IJsonObject)
                response(NET.OK)
            }
            GLTFSourceType.LittleEndianInputStream -> load(source as LittleEndianInputStream)
            GLTFSourceType.GLBFile -> load(LittleEndianInputStream((source as IFile).read()))
            GLTFSourceType.GLTFFile -> {
                val gltfFile = source as IFile
                name = gltfFile.path
                gltfFile.readText("UTF8") { status, obj ->
                    if (NET.isSuccess(status)) {
                        read(JSON.parseObject(obj))
                        response(status)
                    } else {
                        LOG.info("${gltfFile.path} can't read, status $status")
                    }
                }
            }
            else -> {}
        }
    }

    fun createArrayElement(arrayName: String, elementIndex: Int): IGLTFArrayElement {
        return when (arrayName) {
            "buffers" -> GLTFBuffer(gltf = this@GLTF, elementIndex = elementIndex)
            else -> throw IllegalArgumentException("glTF: array name is unknown: $arrayName")
        }
    }

    /** @param json root node in file */
    override fun read(json: IJsonObject) {
        if (!isLoading) {
            isLoaded = false
            isLoading = true

            destroy()

            // https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-asset
            json.get("asset") {
                string("copyright") { copyright = it }
                string("generator") { generator = it }
                version = string("version")
                string("minVersion") { minVersion = it }
            }

            if (json.contains("extensionsRequired")) {
                LOG.info("glTF extensions are not supported yet")
            }

            json.array(buffers.name) {
                objs {
                    val buffer = createArrayElement(buffers.name, buffers.size)
                    buffer.read(this)
                }
            }

            json.array("bufferViews") {
                objs {
                    val bufferView = GLTFBufferView(gltf = this@GLTF, elementIndex = bufferViews.size)
                    bufferViews.add(bufferView)
                    bufferView.read(this)
                }
            }

            json.array("accessors") {
                objs {
                    val accessor = GLTFAccessor(gltf = this@GLTF, elementIndex = accessors.size)
                    accessors.add(accessor)
                    accessor.read(this)
                }
            }

            json.array("samplers") {
                objs {
                    val sampler = GLTFSampler(gltf = this@GLTF, elementIndex = samplers.size)
                    samplers.add(sampler)
                    sampler.read(this)
                }
            }

            json.array("images") {
                objs {
                    val image = GLTFImage(gltf = this@GLTF, elementIndex = images.size)
                    images.add(image)
                    image.read(this)
                }
            }

            json.array("textures") {
                objs {
                    val texture = GLTFTexture(gltf = this@GLTF, elementIndex = textures.size)
                    textures.add(texture)
                    texture.read(this)
                }
            }

            json.array("materials") {
                objs {
                    val material = createMaterial()
                    materials.add(material)
                    material.read(this)
                }
            }

            json.array("meshes") {
                objs {
                    val mesh = GLTFMesh(gltf = this@GLTF, elementIndex = meshes.size)
                    meshes.add(mesh)
                    mesh.read(this)
                }
            }

            json.array("cameras") {
                objs {
                    val camera = GLTFCamera(gltf = this@GLTF, elementIndex = cameras.size)
                    cameras.add(camera)
                    camera.read(this)
                }
            }

            json.array("nodes") {
                objs {
                    val node = GLTFNode(gltf = this@GLTF, elementIndex = nodes.size)
                    nodes.add(node)
                    node.read(this)
                }
            }

            json.array("skins") {
                objs {
                    val skin = GLTFSkin(gltf = this@GLTF, elementIndex = this@GLTF.skins.size)
                    skins.add(skin)
                    skin.read(this)
                }
            }

            json.array("animations") {
                objs {
                    val anim = GLTFAnimation(gltf = this@GLTF, elementIndex = animations.size)
                    animations.add(anim)
                    anim.read(this)
                }
            }

            json.array("scenes") {
                objs {
                    val scene = GLTFScene(gltf = this@GLTF, elementIndex = scenes.size)
                    scenes.add(scene)
                    scene.read(this)
                }
            }

            json.int("scene") {
                mainSceneIndex = it
                scene = scenes.getOrNull(it)?.scene
                data = scene
            }

            if (conf.separateThread) {
                GL.call {
                    glCalls()
                    //deleteDataAfterGPULoading()
                }
            } else {
                glCalls()
                //deleteDataAfterGPULoading()
            }

            isLoading = false
            isLoaded = true
        }
    }

    /** Delete buffers after loading them to GPU */
    protected open fun deleteDataAfterGPULoading() {
        if (!conf.saveFileBuffersInMem) {
            val buffers = buffers
            for (i in buffers.indices) {
                buffers.getOrWait(i) {
                    DATA.destroyBytes(it.bytes)
                    it.bytes = DATA.nullBuffer
                }
            }
        }

        if (!conf.saveTexturesInMem) {
            val images = images
            for (i in images.indices) {
                images.getOrWait(i) {
                    DATA.destroyBytes(it.image.bytes)
                    it.image.bytes = DATA.nullBuffer
                }
            }
        }

        if (!conf.saveMeshesInMem) {
            for (i in meshes.indices) {
                val primitives = meshes[i].primitives
                for (j in primitives.indices) {
                    primitives.getOrWait(j) {
                        val vertices = it.mesh.vertices
                        if (vertices != null) {
                            DATA.destroyBytes(vertices.bytes)
                            vertices.bytes = DATA.nullBuffer
                        }

                        val indices = it.mesh.indices
                        if (indices != null) {
                            DATA.destroyBytes(indices.bytes)
                            indices.bytes = DATA.nullBuffer
                        }
                    }
                }
            }
        }
    }

    override fun saveBuffers() {
        for (i in buffers.indices) {
            buffers[i].writeBytes()
        }
    }

    protected open fun glCalls() {
        for (i in meshes.indices) {
            val primitives = meshes[i].primitives
            for (j in primitives.indices) {
                primitives.getOrWait(j) {
                    it.mesh.vertices?.initGpuObjects()
                    it.mesh.indices?.initGpuObjects()
                }
            }
        }

        materials.forEach {
            it.material.shaderChannels.values.forEach { shader -> shader.build() }
        }
    }

    override fun write(json: IJsonObject) {
        json.set("asset") {
            set("generator", "${THELEMA.name} ${THELEMA.verStr}")
            set("version", version)
            if (copyright.isNotEmpty()) set("copyright", copyright)
            if (minVersion.isNotEmpty()) set("minVersion", minVersion)
        }

        if (mainSceneIndex != -1) json["scene"] = mainSceneIndex

        if (buffers.isNotEmpty()) {
            json.setArray("buffers") {
                for (i in buffers.indices) {
                    add(buffers[i])
                }
            }
        }

        if (bufferViews.isNotEmpty()) {
            json.setArray("bufferViews") {
                for (i in bufferViews.indices) {
                    add(bufferViews[i])
                }
            }
        }

        if (accessors.isNotEmpty()) {
            json.setArray("accessors") {
                for (i in accessors.indices) {
                    add(accessors[i])
                }
            }
        }

        if (samplers.isNotEmpty()) {
            json.setArray("samplers") {
                for (i in samplers.indices) {
                    add(samplers[i])
                }
            }
        }

        if (images.isNotEmpty()) {
            json.setArray("images") {
                for (i in images.indices) {
                    add(images[i])
                }
            }
        }

        if (textures.isNotEmpty()) {
            json.setArray("textures") {
                for (i in textures.indices) {
                    add(textures[i])
                }
            }
        }

        if (materials.isNotEmpty()) {
            json.setArray("materials") {
                for (i in materials.indices) {
                    add(materials[i])
                }
            }
        }

        if (meshes.isNotEmpty()) {
            json.setArray("meshes") {
                for (i in meshes.indices) {
                    add(meshes[i])
                }
            }
        }

        if (cameras.isNotEmpty()) {
            json.setArray("cameras") {
                for (i in cameras.indices) {
                    add(cameras[i])
                }
            }
        }

        if (nodes.isNotEmpty()) {
            json.setArray("nodes") {
                for (i in nodes.indices) {
                    add(nodes[i])
                }
            }
        }

        if (skins.isNotEmpty()) {
            json.setArray("skins") {
                for (i in skins.indices) {
                    add(skins[i])
                }
            }
        }

        if (animations.isNotEmpty()) {
            json.setArray("animations") {
                for (i in animations.indices) {
                    add(animations[i])
                }
            }
        }

        if (scenes.isNotEmpty()) {
            json.setArray("scenes") {
                for (i in scenes.indices) {
                    add(scenes[i])
                }
            }
        }
    }

    fun load(streamLittleEndian: LittleEndianInputStream): GLTF {
        val magic = streamLittleEndian.readInt().toLong() // & 0xFFFFFFFFL;
        if (magic != 0x46546C67L) throw RuntimeException("bad magic")
        val version = streamLittleEndian.readInt()
        if (version != 2) throw RuntimeException("bad version")
        val length = streamLittleEndian.readInt().toLong()// & 0xFFFFFFFFL;

        var jsonData: String? = null
        var i = 12
        while (i < length) {
            val chunkLen = streamLittleEndian.readInt()
            val chunkType = streamLittleEndian.readInt()
            i += 8            // chunkLen % 4;
            if (chunkType == 0x4E4F534A) {
                val data = ByteArray(chunkLen)
                streamLittleEndian.read(data, 0, chunkLen)
                jsonData = String(data)
            } else if (chunkType == 0x004E4942) {
                val bufferData = GLTFBuffer(gltf = this, elementIndex = buffers.size, bytes = DATA.bytes(chunkLen))
                bufferData.bytes.order = DataByteOrder.LittleEndian
                for (j in 0 until chunkLen) bufferData.bytes.put(streamLittleEndian.readByte()) // TODO optimize with stream copy utils ?
                //StreamUtils.copyStream(stream, bufferData, chunkLen);

                buffers.add(bufferData)
            } else {
                LOG.info("skip buffer type $chunkType")
                if (chunkLen > 0) {
                    streamLittleEndian.skip(chunkLen.toLong())
                }
            }
            i += chunkLen
        }

        if (jsonData != null) read(JSON.parseObject(jsonData))
        return this
    }

    override fun destroy() {
        scenes.clear()

        buffers.clear()
        bufferViews.clear()
        accessors.clear()
        samplers.clear()

//        for (i in 0 until images.size) {
//            images[i].destroy()
//        }
//        images.clear()

        if (conf.separateThread) {
            GL.call {
                for (i in 0 until textures.size) {
                    textures[i].destroy()
                }
            }
        } else {
            for (i in 0 until textures.size) {
                textures[i].destroy()
            }
        }

        textures.clear()

        for (i in 0 until materials.size) {
            materials[i].material.shaderChannels.clear()
        }
        materials.clear()

        for (i in 0 until meshes.size) {
            meshes[i].primitives.clear()
        }
        meshes.clear()

//        for (i in 0 until nodes.size) {
//            nodes[i].clear()
//        }
//        nodes.clear()

        this.skins.clear()

        objects.forEach { it.clear() }
        objects.clear()

//        for (i in 0 until animations.size) {
//            animations[i].clear()
//        }
//        animations.clear()

        cameras.clear()

        data = null
    }

    companion object {
        const val Byte = 5120
        const val UByte = 5121
        const val Short = 5122
        const val UShort = 5123
        const val UInt = 5125
        const val Float = 5126

        var DefaultTextureMinFilter = GL_LINEAR_MIPMAP_LINEAR
        var DefaultTextureMagFilter = GL_LINEAR
    }
}
