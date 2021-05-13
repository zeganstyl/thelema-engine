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

package app.thelema.ui

open class TreeNode(
    actor: Actor
): ITreeNode {
    constructor(text: CharSequence): this(Label(text))

    constructor(actor: Actor, block: TreeNode.() -> Unit): this(actor) { block(this) }

    constructor(text: CharSequence, block: TreeNode.() -> Unit): this(Label(text)) { block(this) }

    override var canBeExpandedAnyway: Boolean = false

    /** Sets an icon that will be drawn to the left of the actor.  */
    override var icon: Drawable? = null

    override var actor: Actor = actor
        set(value) {
            val tree = tree
            if (tree != null) {
                value.remove()
                tree.addActor(value)
            }
            field = value
        }

    override var parent: ITreeNode? = null

    override val children: MutableList<ITreeNode> = ArrayList()

    override var isSelectable = true

    override var isExpanded = false
        set(value) {
            if (field != value) {
                field = value
                if (value) expand() else collapse()
            }
        }

    /** Returns the height of the node as calculated for layout. A subclass may override and increase the returned height to
     * create a blank space in the tree above the node, eg for a separator.  */
    override var height = 0f

    override var userObject: Any = this

    /** Returns the tree this node's actor is currently in, or null. The actor is only in the tree when all of its parent nodes
     * are expanded.  */
    override val tree: Tree?
        get() {
            val parent = actor.parent
            return if (parent is Tree) parent else null
        }
}