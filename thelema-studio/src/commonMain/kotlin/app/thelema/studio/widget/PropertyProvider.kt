package app.thelema.studio.widget

/** @param V value type for setter and getter */
interface PropertyProvider<V: Any?> {
    /** Callback, must be setup by external object, set component's property */
    var set: (value: V) -> Unit

    /** Callback, must be setup by external object, get component's property */
    var get: () -> V
}
