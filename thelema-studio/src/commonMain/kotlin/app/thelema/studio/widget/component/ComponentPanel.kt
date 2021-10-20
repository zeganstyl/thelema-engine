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

import app.thelema.ecs.*
import app.thelema.studio.widget.*
import app.thelema.ui.*

open class ComponentPanel<T: IEntityComponent>(val componentName: String, defaultSetup: Boolean = true): Section(componentName) {
    open var component: T? = null

    val fieldWidgets = HashMap<String, Actor>()

    init {
        if (defaultSetup) defaultSetup(componentName)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Actor> fieldWidget(name: String) = fieldWidgets[name] as T

    fun <T: Actor> fieldWidget(name: String, block: T.() -> Unit) = fieldWidget<T>(name).apply(block)

    @Suppress("UNCHECKED_CAST")
    fun defaultSetup(componentName: String) {
        ECS.getDescriptor(componentName)?.apply {
            properties.forEach { (key, value) ->
                var size = 1
                val field = when (value.type) {
                    PropertyType.Int.propertyTypeName -> { size = 1; IntField() }
                    PropertyType.Float.propertyTypeName -> { size = 1; FloatField() }
                    PropertyType.Vec3.propertyTypeName -> { size = 2; Vec3Widget() }
                    PropertyType.Vec4.propertyTypeName -> { size = 2; Vec4Widget() }
                    PropertyType.File.propertyTypeName -> ProjectFileField()
                    else -> null
                }
                if (field != null) {
                    val provider = field as PropertyProvider<Any?>
                    provider.get = { component?.getPropertyTyped(key) }
                    provider.set = { component?.setProperty(key, it) }
                    when (size) {
                        0 -> addField(key, field)
                        1 -> addField2Rows(key, field)
                        2 -> addSectioned(key, field)
                    }
                }
            }
        }
    }

    fun addField(name: String, widget: Actor) {
        fieldWidgets[name] = widget
        content.add(HBox {
            add(Label(name, alignment = Align.topLeft)).padRight(5f)
            add(widget).growX().padBottom(10f)
        }).growX().newRow()
    }

    fun addField2Rows(name: String, widget: Actor) {
        fieldWidgets[name] = widget
        content.add(Label(name, alignment = Align.topLeft)).growX().newRow()
        content.add(widget).growX().padBottom(10f).newRow()
    }

    fun addSectioned(name: String, widget: Actor) {
        fieldWidgets[name] = widget
        val section = Section(name)
        section.content.add(widget).growX()
        content.add(section).growX().newRow()
    }

    companion object {
        inline fun <reified T: IEntityComponent> componentName(): String = ECS.getDescriptor<T>()!!.componentName
    }
}