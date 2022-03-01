/*
 * Copyright 2020-2021 Anton Trushkov
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

package app.thelema.gl

import app.thelema.utils.iterate

interface IVertexLayout {
    val attributes: List<IVertexAttribute>

    fun getAttributeByNameOrNull(name: String): IVertexAttribute?

    /** Define vertex attribute
     * @param size components number */
    fun define(name: String, size: Int, type: Int, normalized: Boolean): IVertexAttribute

    /** Define float vertex attribute
     * @param size components number */
    fun define(name: String, size: Int): IVertexAttribute
}

class VertexLayout: IVertexLayout {
    private val _attributes = ArrayList<IVertexAttribute>()
    override val attributes: List<IVertexAttribute>
        get() = _attributes

    private val _attributesSet = HashMap<String, IVertexAttribute>().apply {
        attributes.iterate { put(it.name, it) }
    }

    init {
        addLayout(this)
    }

    override fun define(name: String, size: Int, type: Int, normalized: Boolean): IVertexAttribute {
        val attribute = VertexAttribute(size, name, type, normalized, _attributes.size)
        _attributes.add(attribute)
        _attributesSet[name] = attribute
        return attribute
    }

    override fun define(name: String, size: Int): IVertexAttribute {
        val attribute = VertexAttribute(size, name, GL_FLOAT, false, _attributes.size)
        _attributes.add(attribute)
        _attributesSet[name] = attribute
        return attribute
    }

    override fun getAttributeByNameOrNull(name: String): IVertexAttribute? = _attributesSet[name]

    companion object {
        private val _layouts = ArrayList<IVertexLayout>()
        val layouts: List<IVertexLayout>
            get() = _layouts

        fun addLayout(layout: IVertexLayout) {
            _layouts.add(layout)
        }

        fun removeLayout(layout: IVertexLayout) {
            _layouts.remove(layout)
        }
    }
}