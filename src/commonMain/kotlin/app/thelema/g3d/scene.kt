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

package app.thelema.g3d

import app.thelema.ecs.*
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.ICamera
import app.thelema.g3d.light.ILight
import app.thelema.g3d.particles.IParticleEmitter
import app.thelema.g3d.particles.IParticleSystem
import app.thelema.gl.IRenderable
import app.thelema.input.*
import app.thelema.shader.IRenderingPipeline
import app.thelema.utils.iterate

/** Root object. Main purpose - render all contained objects as one whole
 *
 * @author zeganstyl */
interface IScene: IEntityComponent {
    var activeCamera: ICamera?

    var world: IWorld?

    var renderingPipeline: IRenderingPipeline?

    var frustumCulling: Boolean

    val lights: List<ILight>

    val renderables: List<IRenderable>

    val particleEmitters: List<IParticleEmitter>

    var keyboardHandler: KeyboardHandler?
    var mouseHandler: MouseHandler?

    fun startSimulation()

    fun stopSimulation()

    fun render()

    fun render(shaderChannel: String?)
}

fun IEntity.scene(block: IScene.() -> Unit) = component(block)
fun IEntity.scene() = component<IScene>()

/** See [IScene]
 *
 * @author zeganstyl */
class Scene: IScene {
    override val componentName: String
        get() = "Scene"

    override var entityOrNull: IEntity? = null
        set(value) {
            val oldValue = field
            if (oldValue != value) {
                field = value
                oldValue?.forEachComponent { removedComponentFromBranch(it) }
                oldValue?.forEachEntityInBranch { removedEntityFromBranch(it) }
                value?.forEachEntityInBranch { addedEntityToBranch(it) }
                value?.forEachComponent { addedComponentToBranch(it) }
            }
        }

    override var keyboardHandler: KeyboardHandler? = null
    override var mouseHandler: MouseHandler? = null
    override var activeCamera: ICamera? = null

    override var world: IWorld? = null

    var translucentSorter: Comparator<IRenderable> = Comparator { o1, o2 ->
        val mesh1Priority = o1.translucencyPriority
        val mesh2Priority = o2.translucencyPriority
        when {
            mesh1Priority > mesh2Priority -> 1
            mesh1Priority < mesh2Priority -> -1
            else -> {
                val dst = ActiveCamera.node.worldPosition.dst2(o1.worldPosition) - ActiveCamera.node.worldPosition.dst2(o2.worldPosition)
                if (dst < 0f) -1 else if (dst > 0f) 1 else 0
            }
        }
    }

    // https://stackoverflow.com/questions/40082085/webgl-2-0-occlusion-query
    var frontToBackSorter: Comparator<IRenderable> = Comparator { o1, o2 ->
        val dst = ActiveCamera.node.worldPosition.dst2(o1.worldPosition) - ActiveCamera.node.worldPosition.dst2(o2.worldPosition)
        if (dst < 0f) 1 else if (dst > 0f) -1 else 0
    }

    override val renderables = ArrayList<IRenderable>()

    override val particleEmitters = ArrayList<IParticleEmitter>()
    private val particleSystems = HashSet<IParticleSystem>()

    override val lights = ArrayList<ILight>()

    private val opaque = ArrayList<IRenderable>()
    private val masked = ArrayList<IRenderable>()
    private val translucent = ArrayList<IRenderable>()

    override var renderingPipeline: IRenderingPipeline? = null

    override var frustumCulling: Boolean = true

    private val renderBlock: (channel: String?) -> Unit = { render(it) }

    private val simulationNodes = HashSet<SimulationNode>()

    private val mouseListener = object : IMouseListener {
        override fun buttonDown(button: Int, x: Int, y: Int, pointer: Int) {
            mouseHandler?.buttonDown(button, x, y, pointer)
        }

        override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
            mouseHandler?.buttonUp(button, screenX, screenY, pointer)
        }

        override fun dragged(screenX: Int, screenY: Int, pointer: Int) {
            mouseHandler?.dragged(screenX, screenY, pointer)
        }

        override fun moved(screenX: Int, screenY: Int) {
            mouseHandler?.moved(screenX, screenY)
        }

        override fun scrolled(amount: Int) {
            mouseHandler?.scrolled(amount)
        }

        override fun cursorEnabledChanged(oldValue: Boolean, newValue: Boolean) {
            mouseHandler?.cursorEnabledChanged(oldValue, newValue)
        }
    }

    private val keyboardListener = object : IKeyListener {
        override fun keyDown(keycode: Int) {
            keyboardHandler?.keyDown(keycode)
        }

        override fun keyUp(keycode: Int) {
            keyboardHandler?.keyUp(keycode)
        }

        override fun keyTyped(character: Char) {
            keyboardHandler?.keyTyped(character)
        }
    }

    override fun startSimulation() {
        MOUSE.addListener(mouseListener)
        KB.addListener(keyboardListener)
        simulationNodes.forEach { it.startSimulation() }
    }

    override fun stopSimulation() {
        MOUSE.removeListener(mouseListener)
        KB.removeListener(keyboardListener)
        simulationNodes.forEach { it.stopSimulation() }
    }

    override fun addedEntityToBranch(entity: IEntity) {
        entity.forEachComponent { addedComponentToBranch(it) }
        entity.forEachChildEntity { child ->
            child.forEachComponentInBranch { addedComponentToBranch(it) }
        }
    }

    override fun removedEntityFromBranch(entity: IEntity) {
        entity.forEachComponent { removedComponentFromBranch(it) }
        entity.forEachChildEntity { child ->
            child.forEachComponentInBranch { removedComponentFromBranch(it) }
        }
    }

    override fun addedComponentToBranch(component: IEntityComponent) {
        if (component is IRenderable) if (!renderables.contains(component)) renderables.add(component)
        if (component is ILight) if (!lights.contains(component)) lights.add(component)
        if (component is SimulationNode) simulationNodes.add(component)
        if (component is IParticleEmitter) if (!particleEmitters.contains(component)) particleEmitters.add(component)
    }

    override fun removedComponentFromBranch(component: IEntityComponent) {
        if (component is IRenderable) renderables.remove(component)
        if (component is ILight) lights.remove(component)
        if (component is SimulationNode) simulationNodes.remove(component)
        if (component is IParticleEmitter) particleEmitters.remove(component)
    }

    override fun addedSiblingComponent(component: IEntityComponent) {
        addedComponentToBranch(component)
    }

    override fun removedSiblingComponent(component: IEntityComponent) {
        removedComponentFromBranch(component)
    }

    override fun render() {
        val oldCamera = ActiveCamera
        val activeCamera = activeCamera
        if (activeCamera != null) ActiveCamera = activeCamera

        for (i in lights.indices) {
            val light = lights[i]
            if (light.isShadowEnabled) light.renderShadowMaps(this)
        }

        val renderingPipeline = renderingPipeline
        if (renderingPipeline != null) {
            renderingPipeline.render(renderBlock)
        } else {
            render(null)
        }

        if (activeCamera != null) ActiveCamera = oldCamera
    }

    override fun render(shaderChannel: String?) {
        opaque.clear()
        masked.clear()
        translucent.clear()

        for (i in renderables.indices) {
            val renderable = renderables[i]
            if (frustumCulling && renderable.visibleInFrustum(ActiveCamera.frustum)) {
                when (renderable.alphaMode) {
                    Blending.BLEND -> translucent.add(renderable)
                    Blending.MASK -> masked.add(renderable)
                    else -> opaque.add(renderable)
                }
            }
        }

        if (opaque.size > 0) {
            // render near objects first, so that far objects can discarded with depth test
            opaque.sortedWith(frontToBackSorter)
            opaque.iterate { it.render(this, shaderChannel) }
        }

        if (masked.size > 0) {
            masked.iterate { it.render(this, shaderChannel) }
        }

        if (translucent.size > 0) {
            translucent.sortWith(translucentSorter)
            translucent.iterate { it.render(this, shaderChannel) }
        }

        particleSystems.clear()
        particleEmitters.iterate {
            it.particleSystem?.also { system ->
                if (particleSystems.add(system)) {
                    system.mesh?.render(this, shaderChannel)
                }
            }
        }
    }

    override fun destroy() {
        super.destroy()
        renderables.clear()
        lights.clear()
        opaque.clear()
        masked.clear()
        translucent.clear()
        stopSimulation()
        simulationNodes.clear()
        renderingPipeline = null
        world = null
        keyboardHandler = null
        mouseHandler = null
        activeCamera = null
    }
}