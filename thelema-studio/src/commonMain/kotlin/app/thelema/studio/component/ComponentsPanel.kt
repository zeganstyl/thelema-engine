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

package app.thelema.studio.component

import app.thelema.ecs.EntityListener
import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.forEachComponent
import app.thelema.g2d.Batch
import app.thelema.studio.SKIN
import app.thelema.studio.Studio
import app.thelema.ui.*

/** Contains all components that exists in entity */
class ComponentsPanel: Table() {
    val componentsListPanel = VBox { align = Align.topLeft }
    val componentsListScroll = ScrollPane(componentsListPanel, style = SKIN.scrollEmpty)

    var entity: IEntity? = null
        set(value) {
            val oldValue = field
            if (oldValue != value) {
                field?.removeEntityListener(entityListener)
                field = value
                value?.addEntityListener(entityListener)
                updateComponentsListRequest = true
            }
        }

    val entityListener = object : EntityListener {
        override fun addedComponent(component: IEntityComponent) {
            updateComponentsListRequest = true
        }

        override fun removedComponent(component: IEntityComponent) {
            updateComponentsListRequest = true
        }
    }

    var updateComponentsListRequest = false

    val entityPathLabel = Label()

    val editEntity = TextButton("Edit Entity") {
        onClick {
            Studio.entityWindow.entity = entity
            hud?.also { Studio.entityWindow.show(it) }
        }
    }

    init {
        editEntity.isVisible = false

        entityPathLabel.setWrap(true)
        add(entityPathLabel).pad(10f).growX().newRow()
        add(HBox { add(editEntity) }).growX().newRow()
        add(componentsListScroll).grow()
        componentsListScroll.fadeScrollBars = false
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (updateComponentsListRequest) {
            updateComponentsListRequest = false
            clearComponents()
            entity?.forEachComponent { setComponent(it) }
            entityPathLabel.text = entity?.path ?: ""
            editEntity.isVisible = entity != null
        }

        super.draw(batch, parentAlpha)
    }

    fun clearComponents() {
        componentsListPanel.clearChildren()
    }

    fun setComponent(component: IEntityComponent) {
        componentsListPanel.add(ComponentPanelProvider.getOrCreatePanel(component)).setFillX()
    }
}