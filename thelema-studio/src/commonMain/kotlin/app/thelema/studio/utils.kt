package app.thelema.studio

fun componentNameView(name: String): String {
    var str = ""
    var prevChar = 'A'
    for (i in name.indices) {
        val c = name[i]
        if (c.isUpperCase() && (prevChar.isLowerCase())) str += ' '
        str += c
        prevChar = c
    }
    return str
}