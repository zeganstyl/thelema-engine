package app.thelema.studio.field

import app.thelema.ui.Actor
import app.thelema.ui.Label
import app.thelema.ui.Table

open class FloatsWidget(): Table() {
    constructor(block: FloatsWidget.() -> Unit): this() {
        block(this)
    }

    fun addField(field: Actor, name: String, color: Int) {
        add(Label(name).apply { this.color = color }).padRight(5f)
        add(field).prefWidth(0f).minWidth(0f).growX().newRow()
    }

    fun addFloatField(field: FloatField, name: String, color: Int, get: () -> Float, set: (value: Float) -> Unit) {
        field.alignment = 0
        field.get = get
        field.set = set
        addField(field, name, color)
    }

    fun addFieldInline(field: Actor) {
        add(field).prefWidth(0f).minWidth(0f).growX().pad(2f)
    }

    fun addFloatFieldInline(field: FloatField, color: Int, get: () -> Float, set: (value: Float) -> Unit) {
        field.alignment = 0
        field.get = get
        field.set = set
        field.textColor = color
        addFieldInline(field)
    }
}
