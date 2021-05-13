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

package app.thelema.ecs

import app.thelema.action.IAction
import app.thelema.anim.AnimationPlayer
import app.thelema.g3d.IArmature
import app.thelema.g3d.IScene
import app.thelema.g3d.Scene
import app.thelema.g3d.light.DirectionalLight
import app.thelema.g3d.node.ITransformNode

class DefaultEntitySystem: IEntitySystem {
    override val systemName: String
        get() = "Default"

    private val armatures = ArrayList<IArmature>()
    private val lights = ArrayList<DirectionalLight>()
    private val nodes = ArrayList<ITransformNode>()
    private val animationPlayers = ArrayList<AnimationPlayer>()

    private var idCounter = 1
    private val syncMap = HashMap<Int, Any>()

    fun newSyncId(sync: Any): Int {
        val id = idCounter
        syncMap[id] = sync
        idCounter++
        return id
    }

    override fun render(entity: IEntity) {
        val scene = entity.getComponentOrNull<IScene>()
        if (scene != null) {
            for (i in lights.indices) {
                lights[i].renderShadowMaps(scene)
            }
        }

        entity.getComponentOrNull<IScene>()?.render()
    }

    override fun update(entity: IEntity, delta: Float) {
        entity.getComponentOrNull<IAction>()?.update(delta)
        entity.getComponentOrNull<AnimationPlayer>()?.update(delta)

        armatures.clear()
        lights.clear()
        nodes.clear()
        animationPlayers.clear()

        entity.forEachComponentInBranch {
            when (it) {
                is IArmature -> armatures.add(it)
                is ITransformNode -> nodes.add(it)
                is DirectionalLight -> lights.add(it)
                is AnimationPlayer -> animationPlayers.add(it)
            }
        }

        for (i in animationPlayers.indices) {
            animationPlayers[i].update(delta)
        }

        for (i in armatures.indices) {
            armatures[i].preUpdateBoneMatrices()
        }

        for (i in nodes.indices) {
            val node = nodes[i]
            if (node.isTransformUpdateRequested) node.updateTransform()
        }

        for (i in armatures.indices) {
            armatures[i].updateBoneMatrices()
        }

        for (i in lights.indices) {
            lights[i].updateDirection()
        }
    }
}