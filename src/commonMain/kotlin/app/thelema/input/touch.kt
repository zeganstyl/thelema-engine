package app.thelema.input

interface ITouch: ITouchListener {
    val listeners: List<ITouchListener>

    fun addListener(listener: ITouchListener)

    fun removeListener(listener: ITouchListener)
}

var TOUCH: ITouch = object : ITouch {
    override val listeners: List<ITouchListener> = emptyList()

    override fun addListener(listener: ITouchListener) {}
    override fun removeListener(listener: ITouchListener) {}
}

interface ITouchListener {
    fun touchDown(x: Float, y: Float, pointer: Int, button: Int) {}
    fun touchUp(x: Float, y: Float, pointer: Int, button: Int) {}
    fun tap(x: Float, y: Float, count: Int, button: Int) {}
    fun longPress(x: Float, y: Float) {}
    fun doubleTap(x: Float, y: Float, button: Int) {}
    fun scale(factor: Float, focusX: Float, focusY: Float) {}
    fun scaleBegin(factor: Float, focusX: Float, focusY: Float) {}
    fun scaleEnd(factor: Float, focusX: Float, focusY: Float) {}
    fun translate() {}
    fun dragged(x: Float, y: Float, pointer: Int) {}
    fun moved(x: Float, y: Float, pointer: Int) {}
    fun fling(velocityX: Float, velocityY: Float, button: Int) {}
}