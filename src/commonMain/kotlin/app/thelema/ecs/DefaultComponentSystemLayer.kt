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
import app.thelema.g3d.IArmature
import app.thelema.g3d.IScene
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.ITransformNode
import app.thelema.phys.IRigidBodyPhysicsWorld
import app.thelema.utils.iterate

class DefaultComponentSystemLayer: IComponentSystemLayer, RebuildListener {
    private val armatures = ArrayList<IArmature>()
    private val nodes = ArrayList<ITransformNode>()
    private val scenes = ArrayList<IScene>(1)
    private val physicsWorlds = ArrayList<IRigidBodyPhysicsWorld>(1)
    private val actions = ArrayList<IAction>()

    private val componentsCanBeRebuild = ArrayList<ComponentCanBeRebuild>()
    private val componentsCanBeRebuildSet = HashSet<ComponentCanBeRebuild>()
    private val updatableComponents = ArrayList<UpdatableComponent>()
    private val updatableComponentsCached = ArrayList<UpdatableComponent>()

    private var idCounter = 1
    private val syncMap = HashMap<Int, Any>()

    private val entityListener = object : EntityListener {
        override fun addedComponent(component: IEntityComponent) {
            if (component is IScene) scenes.add(component)
            if (component is IRigidBodyPhysicsWorld) physicsWorlds.add(component)
            if (component is IAction) actions.add(component)
            if (component is UpdatableComponent) updatableComponents.add(component)
        }

        override fun removedComponent(component: IEntityComponent) {
            if (component is IScene) scenes.remove(component)
            if (component is IRigidBodyPhysicsWorld) physicsWorlds.remove(component)
            if (component is IAction) actions.remove(component)
            if (component is UpdatableComponent) updatableComponents.remove(component)
        }

        override fun addedComponentToBranch(component: IEntityComponent) {
            if (component is IArmature) armatures.add(component)
            if (component is ITransformNode) nodes.add(component)
            if (component is ComponentCanBeRebuild) {
                if (component.rebuildComponentRequested) componentsCanBeRebuild.add(component)
                if (component.rebuildListener == null) component.rebuildListener = this@DefaultComponentSystemLayer
            }
            if (component is UpdatableComponent) updatableComponents.add(component)
        }

        override fun removedComponentFromBranch(component: IEntityComponent) {
            if (component is IArmature) armatures.remove(component)
            if (component is ITransformNode) nodes.remove(component)
            if (component is ComponentCanBeRebuild) {
                if (component.rebuildComponentRequested) componentsCanBeRebuild.remove(component)
                if (component.rebuildListener == this@DefaultComponentSystemLayer) component.rebuildListener = null
            }
            if (component is UpdatableComponent) updatableComponents.remove(component)
        }
    }

    override fun requestRebuild(component: ComponentCanBeRebuild) {
        if (componentsCanBeRebuildSet.add(component)) componentsCanBeRebuild.add(component)
    }

    override fun cancelRebuild(component: ComponentCanBeRebuild) {
        if (componentsCanBeRebuildSet.remove(component)) componentsCanBeRebuild.remove(component)
    }

    override fun addedScene(entity: IEntity) {
        entity.forEachComponent { entityListener.addedComponent(it) }
        entity.forEachComponentInBranch { entityListener.addedComponentToBranch(it) }
        entity.addEntityListener(entityListener)
    }

    override fun removedScene(entity: IEntity) {
        entity.forEachComponent { entityListener.removedComponent(it) }
        entity.forEachComponentInBranch { entityListener.removedComponentFromBranch(it) }
        entity.removeEntityListener(entityListener)
    }

    fun newSyncId(sync: Any): Int {
        val id = idCounter
        syncMap[id] = sync
        idCounter++
        return id
    }

    override fun render(shaderChannel: String?) {
        if (shaderChannel == null) {
            scenes.iterate { it.render() }
        } else {
            scenes.iterate { it.render(shaderChannel) }
        }
    }

    override fun update(delta: Float) {
        ActiveCamera.updatePreviousTransform()

        nodes.iterate { it.updatePreviousMatrix() }
        armatures.iterate { it.updatePreviousBoneMatrices() }

        actions.iterate { it.update(delta) }

        armatures.iterate { it.preUpdateBoneMatrices() }
        nodes.iterate { if (it.isTransformUpdateRequested) it.updateTransform() }

        updatableComponentsCached.clear()
        updatableComponentsCached.addAll(updatableComponents)
        updatableComponentsCached.iterate { it.updateComponent(delta) }

        if (ActiveCamera.isCameraUpdateRequested) ActiveCamera.updateCamera()

        armatures.iterate { it.updateBoneMatrices() }

        if (componentsCanBeRebuild.isNotEmpty()) {
            componentsCanBeRebuild.iterate { it.rebuildComponent() }
            componentsCanBeRebuild.clear()
            componentsCanBeRebuildSet.clear()
        }

        physicsWorlds.iterate { it.step(delta) }
    }
}
