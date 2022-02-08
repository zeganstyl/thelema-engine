package app.thelema.studio.component

import app.thelema.g2d.Sprite
import app.thelema.img.ITexture2D
import app.thelema.ui.DSKIN
import app.thelema.ui.Scaling
import app.thelema.ui.TextButton
import app.thelema.ui.UIImage

class Texture2DPanel: ComponentPanel<ITexture2D>(ITexture2D::class) {
    val sprite = Sprite()

    override var component: ITexture2D?
        get() = super.component
        set(value) {
            super.component = value
            sprite.texture = value ?: DSKIN.transparentFrameTexture
        }

    init {
        content.add(UIImage(sprite).apply {
            this.scaling = Scaling.fit
        }).growX().height(150f).newRow()

        content.add(TextButton("Gen Mipmaps") {
            onClick {
                component?.also {
                    it.bind()
                    it.generateMipmapsGPU()
                }
            }
        }).newRow()
    }
}
