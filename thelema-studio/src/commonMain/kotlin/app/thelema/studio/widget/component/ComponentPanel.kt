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
import app.thelema.studio.ScriptFile
import app.thelema.studio.camelCaseToSpaces
import app.thelema.studio.widget.*
import app.thelema.ui.*
import kotlin.reflect.KClass

open class ComponentPanel<T: IEntityComponent>(
    componentName: String,
    defaultSetup: Boolean = true
): Section(camelCaseToSpaces(componentName)) {
    constructor(c: KClass<T>, defaultSetup: Boolean = true): this(ECS.getDescriptor(c.simpleName!!)!!.componentName, defaultSetup)

    open var component: T? = null

    val fieldWidgets = HashMap<String, Actor>()

    open val menuItems: List<MenuItem>
        get() = emptyList()

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
                val field = when (val type = value.type) {
                    PropertyType.Int -> { size = 1; IntField() }
                    PropertyType.Float -> { size = 1; FloatField() }
                    PropertyType.Vec3 -> { size = 2; Vec3Widget() }
                    PropertyType.Vec4 -> { size = 2; Vec4Widget() }
                    PropertyType.Quaternion -> { size = 2; QuaternionWidget() }
                    PropertyType.File -> ProjectFileField()
                    PropertyType.String -> StringField()
                    PropertyType.Bool -> { size = 3; BoolField() }
                    PropertyType.IntEnum -> {
                        size = 1
                        IntEnumField().apply {
                            value as IntEnumPropertyDesc2<IEntityComponent>
                            default = value.defaultValue
                            map = value.values
                        }
                    }
                    PropertyType.StringEnum -> {
                        size = 1
                        StringEnumField().apply {
                            value as StringEnumPropertyDesc2<IEntityComponent>
                            items = value.values
                        }
                    }
                    ScriptFile -> ScriptFileField()
                    is ComponentRefType -> ComponentReferenceField(type.componentName)
                    else -> null
                }
                if (field != null) {
                    val provider = field as PropertyProvider<Any?>
                    provider.get = { component?.getProperty(key) }
                    provider.set = { component?.setProperty(key, it) }
                    val name = camelCaseToSpaces(key).lowercase()
                    when (size) {
                        0 -> addField(name, field)
                        1 -> addField2Rows(name, field)
                        2 -> addSectioned(name, field)
                        3 -> addCheckBox(name, field)
                    }
                }
            }
        }
    }

    fun addField(name: String, widget: Actor) {
        fieldWidgets[name] = widget
        content.add(HBox {
            add(Label(name, alignment = Align.topLeft)).padRight(5f)
            add(widget).growX()
        }).padBottom(10f).growX().newRow()
    }

    fun addCheckBox(name: String, widget: Actor) {
        fieldWidgets[name] = widget
        content.add(HBox {
            add(widget).padRight(10f)
            add(Label(name, alignment = Align.topLeft)).growX()
        }).padBottom(10f).growX().newRow()
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
