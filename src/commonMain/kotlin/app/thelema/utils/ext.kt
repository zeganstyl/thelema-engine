package app.thelema.utils

inline fun <T> List<T>.iterate(block: (item: T) -> Unit) {
    for (i in indices) {
        block(get(i))
    }
}

inline fun <T> Array<T>.iterate(block: (item: T) -> Unit) {
    for (i in indices) {
        block(get(i))
    }
}
