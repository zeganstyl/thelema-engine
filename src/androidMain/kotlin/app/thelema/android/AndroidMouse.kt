package app.thelema.android

import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import app.thelema.input.IMouse
import app.thelema.input.IMouseListener
import app.thelema.input.MOUSE

class AndroidMouse(val app: AndroidApp): IMouse {
    override var deltaX: Int = 0
    override var deltaY: Int = 0

    override var isCursorEnabled: Boolean
        get() = true
        set(_) {}

    override var x: Int = 0
    override var y: Int = 0

    private var previousX: Float = 0f
    private var previousY: Float = 0f

    val listeners = ArrayList<IMouseListener>()

    var defaultButton = MOUSE.LEFT

    var lastTouchX: Float = 0f
    var lastTouchY: Float = 0f
    var activePointerId = INVALID_POINTER_ID
    var dragging = false

    fun onTouchEvent(e: MotionEvent): Boolean {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        val x: Float = e.x * app.view.holder.surfaceFrame.width() / app.view.width
        val y: Float = e.y * app.view.holder.surfaceFrame.height() / app.view.height

        this.x = x.toInt()
        this.y = y.toInt()
        val pointer = e.actionIndex

        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                deltaX = (x - previousX).toInt()
                deltaY = (y - previousY).toInt()

                e.findPointerIndex(activePointerId)

                // reverse direction of rotation above the mid-line
                if (y > app.view.height * 0.5f) deltaX *= -1

                // reverse direction of rotation to left of the mid-line
                if (x < app.view.width * 0.5f) deltaY *= -1

                lastTouchX = x
                lastTouchY = y

                if (dragging) {
                    for (i in listeners.indices) {
                        listeners[i].dragged(this.x, this.y, pointer)
                    }
                } else {
                    for (i in listeners.indices) {
                        listeners[i].moved(this.x, this.y)
                    }
                }
            }
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                lastTouchX = x
                lastTouchY = y
                activePointerId = pointer

                dragging = true

                for (i in listeners.indices) {
                    listeners[i].buttonDown(defaultButton, this.x, this.y, pointer)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                dragging = false

                for (i in listeners.indices) {
                    listeners[i].buttonUp(defaultButton, this.x, this.y, pointer)
                }
            }
        }

        previousX = x
        previousY = y
        return true
    }

    override fun addListener(listener: IMouseListener) {
        listeners.add(listener)
    }

    override fun getDeltaX(pointer: Int): Int = deltaX

    override fun getDeltaY(pointer: Int): Int = deltaY

    override fun getX(pointer: Int): Int = x

    override fun getY(pointer: Int): Int = y

    override fun isButtonPressed(button: Int): Boolean = false

    override fun removeListener(listener: IMouseListener) {
        listeners.remove(listener)
    }

    override fun reset() {
        listeners.clear()
        x = 0
        y = 0
        deltaX = 0
        deltaY = 0
        previousX = 0f
        previousY = 0f
    }

    override fun setCursorPosition(x: Int, y: Int) {
        this.x = x
        this.y = y
    }
}