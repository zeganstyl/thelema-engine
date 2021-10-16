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

import app.thelema.anim.AnimationPlayer
import app.thelema.ecs.*
import app.thelema.g3d.*
import app.thelema.g3d.ITransformNode
import app.thelema.gl.IMesh
import app.thelema.math.TransformDataType
import app.thelema.shader.node.VelocityNode
import app.thelema.shader.node.VertexNode
import kotlin.math.max

/** @author zeganstyl */
class GLTFScene(array: IGLTFArray): GLTFArrayElementAdapter(array) {
    val nodes: MutableList<Int> = ArrayList()

    override val defaultName: String
        get() = "Scene"

    val loader = gltf.scenesEntity.entity("${defaultName}_$elementIndex").entityLoader {
        separateThread = false
        file = gltf.directory.child(entity.name + EntityLoader.ext)
    }
    val scene: IEntity = loader.targetEntity

    fun writeEntity(entity: IEntity) {
        if (name.isNotEmpty()) scene.name = name
        entity.component<ITransformNode>()

        val newNodes = HashMap<Int, ITransformNode>()
        val skins = ArrayList<Pair<Int, ITransformNode>>()

        for (i in nodes.indices) {
            entity.addEntity(
                getOrCreateNode(entity, gltf.nodes[nodes[i]] as GLTFNode, newNodes, skins)
            )
        }

        val preparedSkins = HashMap<Int, IEntity>()
        val preparedMeshInstances = HashSet<IEntity>()
        skins.forEach { pair ->
            gltf.skins.getOrWait(pair.first) { gltfSkin ->
                gltfSkin as GLTFSkin

                var skinEntity: IEntity? = null

                val preparedSkin = preparedSkins[pair.first]
                if (preparedSkin == null) {
                    if (gltfSkin.skeleton >= 0) {
                        skinEntity = newNodes[gltfSkin.skeleton]!!.entity
                        gltfSkin.writeEntity(skinEntity, newNodes)
                    }
                    if (skinEntity == null) {
                        skinEntity = Entity()
                        skinEntity.name = gltfSkin.name
                        gltfSkin.writeEntity(skinEntity, newNodes)
                        entity.addEntity(skinEntity)
                    }

                    preparedSkins[pair.first] = skinEntity
                } else {
                    skinEntity = preparedSkin
                }

                if (!preparedMeshInstances.contains(pair.second.entity)) {
                    var meshPrepared = false
                    pair.second.entity.forEachChildEntity {
                        val instance = it.component<IMesh>()
                        val skin = skinEntity.component<IArmature>()
                        instance.armature = skin

                        if (!meshPrepared) {
                            meshPrepared = true
                            instance.inheritedMesh?.apply {
                                armature = skin
                                material?.shader?.apply {
                                    val vertexNode = nodes.first { it is VertexNode } as VertexNode
                                    vertexNode.maxBones = max(skin.bones.size, vertexNode.maxBones)
                                    vertexNode.worldTransformType = TransformDataType.None
                                }

                                if (gltf.conf.setupVelocityShader) {
                                    material?.shaderChannels?.get(ShaderChannel.Velocity)?.apply {
                                        val vertexNode = nodes.first { it is VertexNode } as VertexNode
                                        vertexNode.maxBones = max(skin.bones.size, vertexNode.maxBones)
                                        vertexNode.worldTransformType = TransformDataType.None

                                        val velocityNode = nodes.first { it is VelocityNode } as VelocityNode
                                        velocityNode.maxBones = max(skin.bones.size, velocityNode.maxBones)
                                        velocityNode.worldTransformType = TransformDataType.None
                                    }
                                }

                                if (gltf.conf.setupDepthRendering) {
                                    material?.shaderChannels?.get(ShaderChannel.Depth)?.apply {
                                        val vertexNode = nodes.first { it is VertexNode } as VertexNode
                                        vertexNode.maxBones = max(skin.bones.size, vertexNode.maxBones)
                                        vertexNode.worldTransformType = TransformDataType.None
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (gltf.animations.size > 0) {
            val player = entity.component<AnimationPlayer>()
            player.nodes.clear()
            newNodes.entries.forEach {
                player.nodes.add(it.value)
            }
            player.nodes.sortBy { node -> newNodes.entries.first { it.value == node }.key }
            player.animations.addAll(gltf.animations.map { (it as GLTFAnimation).animation })
        }
    }

    override fun readJson() {
        super.readJson()

        nodes.clear()
        json.array("nodes") {
            forEachInt { nodeIndex -> nodes.add(nodeIndex) }
        }

        writeEntity(scene)

        scene.scene {}

        ready()
    }

    private fun getOrCreateNode(
        sceneEntity: IEntity,
        gltfNode: GLTFNode,
        newNodes: HashMap<Int, ITransformNode>,
        skins: MutableList<Pair<Int, ITransformNode>>
    ): IEntity {
        var newNode: ITransformNode? = newNodes[gltfNode.elementIndex]
        if (newNode == null) {
            val entity = Entity()
            entity.name = gltfNode.name
            if (entity.name.isEmpty()) entity.name = "Node"
            newNode = entity.component()
            newNodes[gltfNode.elementIndex] = newNode
            newNode.position.set(gltfNode.translation)
            newNode.rotation.set(gltfNode.rotation)
            newNode.scale.set(gltfNode.scale)
            newNode.isTransformUpdateRequested = true

            for (i in gltfNode.children.indices) {
                entity.addEntity(
                    getOrCreateNode(sceneEntity, gltf.nodes[gltfNode.children[i]] as GLTFNode, newNodes, skins)
                )
            }

            if (gltfNode.mesh >= 0) {
                gltf.meshes.getOrWaitTyped<GLTFMesh>(gltfNode.mesh) { mesh ->
                    mesh.writeEntity(entity)
                }
            }

            if (gltfNode.skin >= 0) {
                skins.add(Pair(gltfNode.skin, newNode))
            }
        }

        return newNode.entity
    }

    override fun writeJson() {
        super.writeJson()
        if (nodes.isNotEmpty()) json.setInts("nodes", nodes.size) { nodes[it] }
    }

    override fun destroy() {
        //scene.destroy()
    }
}