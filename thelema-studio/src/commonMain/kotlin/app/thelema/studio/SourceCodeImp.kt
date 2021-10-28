package app.thelema.studio

import kotlin.script.experimental.api.SourceCode

class SourceCodeImp(override var text: String, override var name: String) : SourceCode {
    override val locationId: String? = null
}
