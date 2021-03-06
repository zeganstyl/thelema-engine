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

import app.thelema.THELEMA
import app.thelema.ecs.ECS
import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.fs.FS
import app.thelema.fs.IFile
import app.thelema.g3d.IScene
import app.thelema.json.IJsonObject
import app.thelema.json.JSON
import app.thelema.res.LoaderAdapter
import app.thelema.res.Resource
import app.thelema.utils.LOG

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0)
 *
 * @author zeganstyl */
class GLTF: IGLTF, LoaderAdapter() {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            value?.component<Resource>()?.loaderOrNull = this
        }

    var source: Any = ""
    var sourceType: GLTFSourceType = GLTFSourceType.Auto
    override var directory: IFile = FS.internal("/")

    override val componentName: String
        get() = "GLTF"

    override var conf = GLTFConf()

    override var generator = ""
    override var version = "2.0"
    override var copyright = ""
    override var minVersion = ""

    override val extensions: MutableList<IGLTFExtension> = ArrayList()

    override val buffers = GLTFArray("buffers", this) { GLTFBuffer(it) }
    override val bufferViews = GLTFArray("bufferViews", this) { GLTFBufferView(it) }
    override val accessors = GLTFArray("accessors", this) { GLTFAccessor(it) }
    override val samplers = GLTFArray("samplers", this) { GLTFSampler(it) }
    override val images = GLTFArray("images", this) { GLTFImage(it) }
    override val textures = GLTFArray("textures", this) { GLTFTexture(it) }
    override val materials: IGLTFArray = GLTFArray("materials", this) { GLTFMaterial(it) }
    override val meshes = GLTFArray("meshes", this) { GLTFMesh(it) }
    override val nodes = GLTFArray("nodes", this) { GLTFNode(it) }
    override val skins = GLTFArray("skins", this) { GLTFSkin(it) }
    override val animations = GLTFArray("animations", this) { GLTFAnimation(it) }
    override val scenes = GLTFArray("scenes", this) { GLTFScene(it) }
    override val cameras = GLTFArray("cameras", this) { GLTFCamera(it) }

    override val meshesEntity: IEntity
        get() = entity.entity(meshes.name).apply { serialize = false }
    override val imagesEntity: IEntity
        get() = entity.entity(images.name).apply { serialize = false }
    override val texturesEntity: IEntity
        get() = entity.entity(textures.name).apply { serialize = false }
    override val materialsEntity: IEntity
        get() = entity.entity(materials.name).apply { serialize = false }
    override val animationsEntity: IEntity
        get() = entity.entity(animations.name).apply { serialize = false }
    override val scenesEntity: IEntity
        get() = entity.entity(scenes.name).apply { serialize = false }

    override val extra: MutableMap<String, Any> = HashMap()

    override var scene: IScene? = null
    override var mainSceneIndex: Int = -1

    private var isLoadingInternal = false
    override val isLoading: Boolean
        get() = isLoadingInternal

    private val glCalls = ArrayList<() -> Unit>()

    private var runOnGLThreadRequestInternal: Boolean = false
    override val runOnGLThreadRequest: Boolean
        get() = runOnGLThreadRequestInternal

    override var loadOnSeparateThread: Boolean
        get() = conf.separateThread
        set(value) {
            conf.separateThread = value
        }

    private var progressInitiated = false

    override val chunks = ArrayList<GLBChunk>()
    override val binChunks = ArrayList<GLBChunk>()

    fun iterateArrays(block: (array: IGLTFArray) -> Unit) {
        block(buffers)
        block(bufferViews)
        block(accessors)
        block(samplers)
        block(images)
        block(textures)
        block(materials)
        block(meshes)
        block(nodes)
        block(skins)
        block(animations)
        block(scenes)
        block(cameras)
    }

    override fun getArray(name: String): IGLTFArray {
        var array: IGLTFArray? = null
        iterateArrays {
            if (it.name == name) {
                array = it
                return@iterateArrays
            }
        }
        return array!!
    }

    override fun initProgress() {
        currentProgress = 0
        maxProgress = 1
        iterateArrays {
            it.initProgress()
            maxProgress += it.maxProgress
        }
    }

    override fun updateProgress() {
        currentProgress = 0

        if (progressInitiated && isLoadingInternal) {
            iterateArrays {
                it.updateProgress()
                //LOG.info("${it.name}: ${it.progress}")
                currentProgress += it.currentProgress
            }

            if (currentProgress == maxProgress - 1) {
                isLoadingInternal = false
                currentProgress++
                notifyLoaded()
            }
        }
    }

    override fun runOnGLThread() {
        super.runOnGLThread()

        for (i in glCalls.indices) {
            glCalls[i].invoke()
        }

        glCalls.clear()
        runOnGLThreadRequestInternal = false
    }

    override fun runGLCall(block: () -> Unit) {
        if (loadOnSeparateThread) {
            glCalls.add(block)
            if (!runOnGLThreadRequestInternal) {
                for (i in listeners.indices) {
                    listeners[i].runOnGLThreadRequested(this)
                }
                runOnGLThreadRequestInternal = true
            }
        } else {
            block()
        }
    }

    override fun load() {
        if (!isLoading) {
            destroy()

            isLoadingInternal = true
            progressInitiated = false

            val file = entity.component<Resource>().file
            source = file
            directory = file.parent()
            if (sourceType == GLTFSourceType.Auto) {
                val ext = file.extension.toLowerCase()
                sourceType = if (ext == "glb" || ext == "vrm") GLTFSourceType.GLBFile else GLTFSourceType.GLTFFile
            }

            when (sourceType) {
                GLTFSourceType.JSON -> {
                    readGLTF(source as IJsonObject)

                    for (i in listeners.indices) {
                        listeners[i].loaded(this)
                    }
                }
                GLTFSourceType.GLTFFile -> {
                    val gltfFile = source as IFile
                    uriInternal = gltfFile.path

                    gltfFile.readText(
                        charset = "UTF8",
                        error = { status ->
                            LOG.info("${gltfFile.path} can't read, status $status")

                            currentProgress = 0
                            isLoadingInternal = false

                            notifyError(status)
                        },
                        ready = { text -> readGLTF(JSON.parseObject(text)) }
                    )
                }
                GLTFSourceType.GLBFile -> {
                    // https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#glb-file-format-specification

                    val glb = source as IFile
                    glb.readBytes { bytes ->
                        val magic = bytes.getInt(0)
                        if (magic != 0x46546C67) {
                            LOG.error("GLB file ${glb.path} has not valid magic constant (${magic.toString(16)} must be 0x46546C67)", IllegalStateException())
                        }

                        version = bytes.getInt(4).toString()

                        var pos = 20
                        var len = bytes.getInt(12)
                        chunks.add(
                            GLBChunk(
                                bytes.byteView(pos, len),
                                bytes.getInt(16)
                            )
                        )

                        while (pos + len < bytes.capacity) {
                            pos += len

                            len = bytes.getInt(pos)
                            val chunkType = bytes.getInt(pos + 4)
                            pos += 8
                            val chunk = GLBChunk(bytes.copy(pos, len), chunkType)
                            chunks.add(chunk)
                            if (chunk.chunkType == GLBChunk.BINType) {
                                binChunks.add(chunk)
                            }
                        }

                        val jsonText = chunks[0].chunkData.toStringUTF8()
                        readGLTF(JSON.parseObject(jsonText))
                    }
                }
                else -> {}
            }
        }
    }

    private fun readGLTF(json: IJsonObject) {
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

        iterateArrays {
            json.array(it.name) {
                it.setJson(this)
            }
        }

        initProgress()

        progressInitiated = true

        iterateArrays {
            json.array(it.name) {
                it.readJson()
            }
        }

        json.int("scene") {
            mainSceneIndex = it
            scene = (scenes.getOrNull(it) as GLTFScene?)?.scene
        }

        var loadedSkinnedNum = 0
        val skinnedNodes = ArrayList<Int>()
        for (i in nodes.indices) {
            val node = nodes[i] as GLTFNode
            if (node.skin >= 0 && node.mesh >= 0) {
                skinnedNodes.add(i)
            }
        }
        for (i in skinnedNodes.indices) {
            val nodeIndex = skinnedNodes[i]
            nodes.getOrWait(nodeIndex) {
                loadedSkinnedNum++
                if (loadedSkinnedNum == skinnedNodes.size) {
                    runGLCall {
                        materials.forEach { (it as GLTFMaterial).buildShaders() }
                    }
                }
            }
        }

        runGLCall {
            if (skinnedNodes.isEmpty()) materials.forEach { (it as GLTFMaterial).buildShaders() }
        }
    }

    override fun saveBuffers() {
        for (i in buffers.indices) {
            //buffers[i].writeBytes()
        }
    }

    override fun writeJson(json: IJsonObject) {
        json.setObj("asset") {
            set("generator", "${THELEMA.name} ${THELEMA.verStr}")
            set("version", version)
            if (copyright.isNotEmpty()) set("copyright", copyright)
            if (minVersion.isNotEmpty()) set("minVersion", minVersion)
        }

        if (mainSceneIndex != -1) json["scene"] = mainSceneIndex

        iterateArrays {
            json.setArray(it.name) {
                it.writeJson()
            }
        }
    }

    override fun destroy() {
        iterateArrays { it.destroy() }
    }

    companion object {
        const val Byte = 5120
        const val UByte = 5121
        const val Short = 5122
        const val UShort = 5123
        const val UInt = 5125
        const val Float = 5126
    }
}
