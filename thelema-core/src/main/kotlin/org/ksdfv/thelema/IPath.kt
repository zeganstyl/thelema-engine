/*
 * Copyright 2020 Anton Trushkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ksdfv.thelema

import org.ksdfv.thelema.ext.traverseSafe

/** @author zeganstyl */
interface IPath {
    var name: String

    val childPaths: List<IPath>

    val parentPath: IPath?

    /** It is relative path from root */
    val path: String
        get() {
            val parentPath = parentPath ?: return ""

            var path = parentPath.path
            if (path.isNotEmpty()) path += delimiter
            return path + name
        }

    fun rootPath(): IPath = parentPath?.rootPath() ?: this

    /**
     * Finds relative path from that object to another.
     * For example if path of this is "/obj/obj/this" and path of another is "/obj/another",
     * then this function return "../another".
     * It is like UNIX file naming.
     * @param t object that must be found and relative path to him calculated
     * @param exclude child object that will be skipped
     * @param up if enabled, then function will search all tree, not only children tree
     */
    fun relativePathTo(t: IPath, exclude: IPath? = null, up: Boolean = true): String {
        var p: String? = null

        // traverse direct children
        val childName = childPaths.firstOrNull { it === t }?.name
        if (childName != null) p = childName

        if (p == null) {
            if (t === this) p = toSelf

            if (p == null) {
                // check child branches
                for (j in childPaths.indices) {
                    val it = childPaths[j]

                    if (it !== exclude) {
                        val path = it.relativePathTo(t, null, false)
                        if (path.isNotEmpty() && !path.endsWith(upDelimiter)) {
                            p = "${it.name}$delimiter$path"
                            break
                        }
                    }
                }

                if (p == null) {
                    // check parent tree, exclude this branch
                    p = if (parentPath != null) {
                        if (up) "$upDelimiter${parentPath!!.relativePathTo(t, this, true)}" else ""
                    } else {
                        t.path
                    }
                }
            }
        }

        return p
    }

    /** Get object from children tree by path */
    /** возможно стоит избавится от рекурсии, сделать через path.split() и прогнать через цикл */
    fun byPath(path: String): IPath? = when {
        path.isEmpty() -> null
        path == toSelf -> this
        path.startsWith(upDelimiter) -> parentPath?.byPath(path.substring(3))
        path.contains(delimiter) -> {
            val childName = path.substring(0, path.indexOf(delimiter))
            get(childName)?.byPath(path.substring(childName.length+1))
        }
        else -> get(path)
    }

    operator fun get(name: String) = childPaths.firstOrNull { it.name == name }

    fun traversePathTree(block: (item: IPath) -> Unit) {
        childPaths.traverseSafe {
            block(it)
            it.traversePathTree(block)
        }
    }

    fun firstOrNullFromTree(predicate: (item: IPath) -> Boolean): IPath? {
        val item = childPaths.firstOrNull(predicate)
        if (item != null) return item

        for (i in childPaths.indices) {
            val item2 = childPaths[i].firstOrNullFromTree(predicate)
            if (item2 != null) return item2
        }

        return null
    }

    private fun isSiblingNameAcceptable(newName: String): Boolean {
        val entity = parentPath?.get(newName)
        return entity == null || entity == this
    }

    private fun isChildNameAcceptable(newName: String) = get(newName) == null

    private fun makeName(newName: String, isAcceptable: (newName: String) -> Boolean): String {
        // вычисляем новое имя _1, _2, ...
        var name = newName
        val last = name.length-1
        var i = last
        while(name[i].isDigit() && i > 0) { i-- }
        if(name[i] == '_' && i != last){
            var num = (name.substring(i+1).toInt() + 1)
            val prefix = name.substring(0, i) + "_"
            name = prefix + num

            while(!isAcceptable(name)){
                num++
                name = prefix + num
            }
        }

        return if (isAcceptable(name)) name else makeName("${name}_1", isAcceptable)
    }
    fun makeSiblingName(newName: String = name) = makeName(newName) { isSiblingNameAcceptable(it) && it != name }
    fun makeChildName(newName: String) = makeName(newName) { isChildNameAcceptable(it) }

    companion object {
        const val toSelf = "."
        const val toParent = ".."
        const val delimiter = '/'
        const val upDelimiter = toParent + delimiter
    }
}