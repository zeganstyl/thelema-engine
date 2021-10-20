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

package app.thelema.studio.widget.component

import app.thelema.ecs.IEntityComponent
import app.thelema.studio.ComponentPanelProvider
import app.thelema.ui.Align
import app.thelema.ui.ScrollPane
import app.thelema.ui.Table
import app.thelema.ui.VBox

/** Contains all components that exists in entity */
class ComponentsPanel: Table() {
    val componentsListPanel = VBox { align = Align.topLeft }
    val componentsListScroll = ScrollPane(componentsListPanel)

    val componentPanelsCache = HashMap<String, ComponentPanel<IEntityComponent>>()

    init {
        add(componentsListScroll).grow()
        componentsListScroll.fadeScrollBars = false
    }

    fun clearComponents() {
        componentsListPanel.clearChildren()
    }

    fun getOrCreatePanel(componentName: String): ComponentPanel<IEntityComponent> {
        var panel = componentPanelsCache[componentName]
        if (panel == null) {
            panel = ComponentPanelProvider.providers[componentName]?.invoke() ?: ComponentPanel(componentName)
            componentPanelsCache[componentName] = panel
        }
        return panel
    }

    fun setComponent(component: IEntityComponent) {
        val panel = getOrCreatePanel(component.componentName)
        panel.component = component
        componentsListPanel.add(panel).setFillX()
    }
}