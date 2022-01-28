package app.thelema.studio

import app.thelema.app.APP
import app.thelema.ecs.ComponentDescriptor
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.IPropertyDescriptor
import app.thelema.ecs.PropertyType
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile
import app.thelema.json.IJsonObject
import app.thelema.jvm.JvmFile
import app.thelema.script.KotlinScriptAdapter
import app.thelema.utils.iterate
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.host.toScriptSource

class KotlinScriptStudio: KotlinScriptAdapter() {
    var file: IFile? = null

    override var functionName: String
        get() = file?.nameWithoutExtension ?: ""
        set(_) {}

    private var oldFullCode = ""

    fun loadImports(out: MutableList<SourceCode>) {
        imports.forEach { (it as KotlinScriptStudio).loadImports(out) }
    }

    // FIXME script imports
    override fun execute() {
        file?.readText { text ->
            val importsList = ArrayList<SourceCode>()
            imports.forEach { (it as KotlinScriptStudio).loadImports(importsList) }

            val entityPropertyName = "entity" + APP.hashCode().toString(16)

            val code = StringScriptSource(
                "$text\n$functionName($entityPropertyName)",
                entity.name
            )

            runBlocking { KotlinScripting.eval(entity, code, entityPropertyName, importsList) }
        }
    }

    override fun destroy() {
        oldFullCode = ""
        file = null
        super.destroy()
    }
}

/** Define File-property (string) */
fun <T: IEntityComponent> ComponentDescriptor<T>.kotlinScriptFile(name: String, get: T.() -> IFile?, set: T.(value: IFile?) -> Unit) = property(object :
    IPropertyDescriptor<T, IFile?> {
    override val name: String = name
    override val type = ScriptFile
    override fun setValue(component: T, value: IFile?) = set(component, value)
    override fun getValue(component: T): IFile? = get(component)
    override fun default(): IFile? = null
    override fun readJson(component: T, json: IJsonObject) {
        KotlinScripting.kotlinDirectory?.also {
            if (json.contains("file")) {
                val path = json.string("file")
                set(component, scriptFile(path))
            } else {
                set(component, null)
            }
        }
    }
    override fun writeJson(component: T, json: IJsonObject) {
        component.get()?.also { json["file"] = it.path }
    }
})

fun scriptFile(path: String) =
    JvmFile(
        File(
            KotlinScripting.kotlinDirectory?.platformPath
                ?: throw IllegalStateException("Kotlin sources directory is not set"), path
        ),
        path,
        FileLocation.Relative
    )

val ScriptFile = PropertyType("thelema/ScriptFile")
