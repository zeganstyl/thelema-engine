package app.thelema.android

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import app.thelema.input.BUTTON
import app.thelema.input.ITouch
import app.thelema.input.ITouchListener
import app.thelema.utils.iterate

class AndroidTouch(val app: AndroidApp): ITouch {
    private val _listener = ArrayList<ITouchListener>()
    override val listeners: List<ITouchListener>
        get() = _listener

    var deltaX: Int = 0
    var deltaY: Int = 0

    var isCursorEnabled: Boolean
        get() = true
        set(_) {}

    var x: Float = 0f
    var y: Float = 0f

    private var previousX: Float = 0f
    private var previousY: Float = 0f

    var defaultButton = BUTTON.LEFT

    var lastTouchX: Float = 0f
    var lastTouchY: Float = 0f
    var activePointerId = MotionEvent.INVALID_POINTER_ID
    var dragging = false

    val scaleDetector = ScaleGestureDetector(app.context, object : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale(detector.scaleFactor, detector.focusX, detector.focusY)
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            scaleBegin(detector.scaleFactor, detector.focusX, detector.focusY)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            scaleEnd(detector.scaleFactor, detector.focusX, detector.focusY)
        }
    })

    fun onTouchEvent(e: MotionEvent) {
        val x: Float = e.x * app.view.holder.surfaceFrame.width() / app.view.width
        val y: Float = e.y * app.view.holder.surfaceFrame.height() / app.view.height

        this.x = x
        this.y = y
        val pointer = e.actionIndex

        scaleDetector.onTouchEvent(e)

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

                if (e.pointerCount == 1) {
                    if (dragging) {
                        listeners.iterate { it.dragged(x, y, pointer) }
                    } else {
                        listeners.iterate { it.moved(x, y, pointer) }
                    }
                }
            }
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                lastTouchX = x
                lastTouchY = y
                activePointerId = pointer

                dragging = true

                if (e.pointerCount == 1) {
                    listeners.iterate { it.touchDown(x, y, pointer, defaultButton) }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                dragging = false

                if (e.pointerCount == 1) {
                    listeners.iterate { it.touchUp(x, y, pointer, defaultButton) }
                }
            }
        }

        previousX = x
        previousY = y
    }

    override fun addListener(listener: ITouchListener) {
        _listener.add(listener)
    }

    override fun removeListener(listener: ITouchListener) {
        _listener.remove(listener)
    }

    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int) {
        _listener.iterate { it.touchDown(x, y, pointer, button) }
    }

    override fun touchUp(x: Float, y: Float, pointer: Int, button: Int) {
        _listener.iterate { it.touchUp(x, y, pointer, button) }
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int) {
        _listener.iterate { it.tap(x, y, count, button) }
    }

    override fun longPress(x: Float, y: Float) {
        _listener.iterate { it.longPress(x, y) }
    }

    override fun doubleTap(x: Float, y: Float, button: Int) {
        _listener.iterate { it.doubleTap(x, y, button) }
    }

    override fun scale(factor: Float, focusX: Float, focusY: Float) {
        _listener.iterate { it.scale(factor, focusX, focusY) }
    }

    override fun scaleBegin(factor: Float, focusX: Float, focusY: Float) {
        _listener.iterate { it.scaleBegin(factor, focusX, focusY) }
    }

    override fun scaleEnd(factor: Float, focusX: Float, focusY: Float) {
        _listener.iterate { it.scaleEnd(factor, focusX, focusY) }
    }

    override fun translate() {
        TODO("Not yet implemented")
    }

    override fun dragged(x: Float, y: Float, pointer: Int) {
        _listener.iterate { it.dragged(x, y, pointer) }
    }

    override fun fling(velocityX: Float, velocityY: Float, button: Int) {
        _listener.iterate { it.fling(velocityX, velocityY, button) }
    }
}