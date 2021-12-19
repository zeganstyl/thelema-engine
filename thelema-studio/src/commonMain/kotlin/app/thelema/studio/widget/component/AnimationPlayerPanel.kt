package app.thelema.studio.widget.component

import app.thelema.anim.AnimationPlayer
import app.thelema.anim.IAnimation
import app.thelema.ui.SelectBox
import app.thelema.ui.TextButton

class AnimationPlayerPanel: ComponentPanel<AnimationPlayer>(AnimationPlayer::class) {
    val selectAnim = SelectBox<IAnimation>()

    override var component: AnimationPlayer?
        get() = super.component
        set(value) {
            super.component = value
            selectAnim.items = component?.animations ?: emptyList()
        }

    init {
        selectAnim.setSelected = {
            selectAnim.selectedItem = it
            it?.also {
                component?.setAnimation(it)
            }
        }
        selectAnim.defaultText = "No anim"
        selectAnim.itemToString = { it.entityOrNull?.name ?: "" }
        content.add(selectAnim).newRow()
        content.add(TextButton("Stop") {
            onClick {
                component?.previous = null
                component?.current = null
                component?.queued = null
            }
        })
    }
}