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
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.script.experimental.api.*

class KotlinScriptStudio: KotlinScriptAdapter() {
    val code = SourceCodeImp("", "")

    var file: IFile? = null

    override var customMainFunctionName: String = ""
        set(value) {
            field = value
            mainFunction = getMainFunctionName()
        }

    var mainFunction: String = ""

    private var oldFullCode = ""

    fun loadImports(out: MutableList<SourceCode>) {
        code.also { out.add(it) }
        imports.forEach { (it as KotlinScriptStudio).loadImports(out) }
    }

    override fun execute() {
        file?.readText { text ->
            val importsList = ArrayList<SourceCode>()
            imports.forEach { (it as KotlinScriptStudio).loadImports(importsList) }

            val entityPropertyName = "entity" + APP.hashCode().toString(16)

            code.name = entity.name
            code.text = "$text\n$mainFunction($entityPropertyName)"

            runBlocking { KotlinScripting.eval(entity, code, entityPropertyName, importsList) }
        }
    }

    override fun destroy() {
        oldFullCode = ""
        file = null
        code.text = ""
        super.destroy()
    }
}

/** Define File-property (string) */
fun <T: IEntityComponent> ComponentDescriptor<T>.kotlinScriptFile(name: String, get: T.() -> IFile?, set: T.(value: IFile?) -> Unit) = property(object :
    IPropertyDescriptor<T, IFile?> {
    override val name: String = name
    override val type: String = ScriptFile.propertyTypeName
    override fun setValue(component: T, value: IFile?) = set(component, value)
    override fun getValue(component: T): IFile? = get(component)
    override fun default(): IFile? = null
    override fun readJson(component: T, json: IJsonObject) {
        KotlinScripting.kotlinDirectory?.also { kotlinDirectory ->
            if (json.contains("file")) {
                val path = json.string("file")
                set(component, JvmFile(File(kotlinDirectory.platformPath, path), path, FileLocation.Relative))
            } else {
                set(component, null)
            }
        }
    }
    override fun writeJson(component: T, json: IJsonObject) {
        component.get()?.also { json["file"] = it.path }
    }
})

val ScriptFile = PropertyType("thelema/ScriptFile")
