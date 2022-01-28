package app.thelema.studio

import app.thelema.ecs.Entity
import app.thelema.ecs.IEntity
import app.thelema.fs.IFile
import app.thelema.json.IJsonObject
import app.thelema.json.JSON
import app.thelema.res.RES
import app.thelema.utils.LOG
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.BasicScriptingHost

object KotlinScripting {
    val defaultDependencies = ArrayList<ScriptDependency>()

    var hostOrNull: BasicScriptingHost? = null
    val host: BasicScriptingHost
        get() = hostOrNull!!

    var kotlinDirectory: IFile? = null

    var bakedScriptsPackage = ""

    val cachedScripts = HashMap<String, CompiledScript>()

    fun init(host: BasicScriptingHost, vararg dependencies: ScriptDependency) {
        hostOrNull = host
        defaultDependencies.addAll(dependencies)
    }

    suspend fun compile(code: SourceCode, entityPropertyName: String, imports: List<SourceCode>): CompiledScript? {
        val result = host.compiler.invoke(
            code,
            ScriptCompilationConfiguration {
                dependencies(defaultDependencies)
                importScripts.put(imports)
                providedProperties.put(mapOf(entityPropertyName to KotlinType(IEntity::class)))
            }
        )

        when (result) {
            is ResultWithDiagnostics.Success -> {
                return result.value
            }
            else -> {
                result.reports.forEach { LOG.error(it.render()) }
            }
        }

        return null
    }

    suspend fun eval(entity: IEntity, code: SourceCode, entityPropertyName: String, imports: List<SourceCode>) {
        var fullCode = ""
        imports.forEach { fullCode += it.text }
        fullCode += code.text

        var script = cachedScripts[fullCode]
        if (script == null) {
            script = compile(code, entityPropertyName, imports)
            if (script != null) cachedScripts[fullCode] = script
        }

        if (script != null) {
            host.evaluator.invoke(
                script,
                ScriptEvaluationConfiguration {
                    providedProperties.put(mapOf(entityPropertyName to entity))
                }
            )
        }
    }

    private fun traverseKotlinScriptComponents(entity: IJsonObject, out: MutableList<KotlinScriptStudio>) {
        entity.obj("components") {
            obj("KotlinScript") {
                val script = KotlinScriptStudio()
                script.readJson(this)
                out.add(script)
            }
        }
        entity.forEachObject("children") {
            traverseKotlinScriptComponents(this, out)
        }
    }

    private fun traverseEntities(file: IFile, out: MutableList<KotlinScriptStudio>) {
        if (file.isDirectory) {
            file.list().forEach { traverseEntities(it, out) }
        } else {
            if (file.extension == "entity" || file.extension == "thelema") {
                file.readText {
                    traverseKotlinScriptComponents(JSON.parseObject(it), out)
                }
            }
        }
    }

    fun bakeScripts() {
        val list = ArrayList<KotlinScriptStudio>()
        traverseEntities(RES.absoluteDirectory, list)
        var functionMapFill = ""
        val hashSet = HashSet<KotlinScriptStudio>()
        list.forEach { if (it.file != null) hashSet.add(it) }
        hashSet.forEach { script ->
            val file = script.file!!
            val scriptPackage = file.parent().path.replace('/', '.')
            functionMapFill += if (scriptPackage.isNotEmpty() && scriptPackage != RES.appPackage) {
                val functionPath = scriptPackage + '.' + script.functionName
                "    \"${script.functionName}\" to { $functionPath(it) },\n"
            } else {
                val functionPath = script.functionName
                "    \"$functionPath\" to { $functionPath(it) },\n"
            }
        }
        kotlinDirectory?.also { kotlinDir ->
            val path = RES.appPackage.replace('.', '/')
            kotlinDir.child(path).child("scripts.kt").writeText(
                (if (bakedScriptsPackage.isEmpty()) "" else "package $bakedScriptsPackage\n\n") +
                        """${if (RES.appPackage.isEmpty()) "" else "package ${RES.appPackage}\n\n"}import app.thelema.script.setScriptMap

fun initScripts() = setScriptMap(
$functionMapFill)
""")
        }
    }
}