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

/** A [Tree] node which has an actor and value.
 *
 *
 * A subclass can be used so the generic type parameters don't need to be specified repeatedly.
 * @author Nathan Sweet */
interface ITreeNode {
    var actor: Actor

    var parent: ITreeNode?

    val children: MutableList<ITreeNode>

    var canBeExpandedAnyway: Boolean

    var isExpanded: Boolean

    var isSelectable: Boolean

    /** Sets an icon that will be drawn to the left of the actor.  */
    var icon: Drawable?

    var userObject: Any

    /** Returns the height of the node as calculated for layout. A subclass may override and increase the returned height to
     * create a blank space in the tree above the node, eg for a separator.  */
    var height: Float

    /** Returns the tree this node's actor is currently in, or null. The actor is only in the tree when all of its parent nodes
     * are expanded.  */
    val tree: Tree?

    fun treeNode(text: String): ITreeNode {
        val node = TreeNode(text)
        add(node)
        return node
    }

    fun treeNode(text: String, context: ITreeNode.() -> Unit): ITreeNode = treeNode(text).apply(context)

    fun expand() {
        isExpanded = true
        if (children.size == 0) return
        val tree = tree ?: return
        val children = children
        var actorIndex = actor.zIndex + 1
        var i = 0
        val n = this.children.size
        while (i < n) {
            actorIndex += children[i].addToTree(tree, actorIndex)
            i++
        }
    }

    fun collapse() {
        isExpanded = false
        if (children.size == 0) return
        val tree = tree ?: return
        val children = children
        val actorIndex = actor.zIndex + 1
        var i = 0
        val n = this.children.size
        while (i < n) {
            children[i].removeFromTree(tree, actorIndex)
            i++
        }
    }

    /** Collapses all nodes under and including this node.  */
    fun collapseAll() {
        collapse()

        val children = children
        var i = 0
        val n = children.size
        while (i < n) {
            val node = children[i]
            node.collapseAll()
            i++
        }
    }

    /** Expands all nodes under and including this node.  */
    fun expandAll() {
        expand()

        val children = children
        if (children.size > 0) {
            var i = 0
            val n = children.size
            while (i < n) {
                children[i].expandAll()
                i++
            }
        }
    }

    /** Expands all parent nodes of this node.  */
    fun expandTo() {
        var node: ITreeNode? = parent
        while (node != null) {
            node.expand()
            node = node.parent
        }
    }

    /** Called to add the actor to the tree when the node's parent is expanded.
     * @return The number of node actors added to the tree.
     */
    fun addToTree(tree: Tree, actorIndex: Int): Int {
        tree.addActorAt(actorIndex, actor)
        if (!isExpanded) return 1
        var added = 1
        val children = children
        var i = 0
        val n = this.children.size
        while (i < n) {
            added += children[i].addToTree(tree, actorIndex + added)
            i++
        }
        return added
    }

    /** Called to remove the actor from the tree, eg when the node is removed or the node's parent is collapsed.  */
    fun removeFromTree(tree: Tree, actorIndex: Int) {
        val removeActorAt = tree.removeActorAt(actorIndex, false)
        if (!isExpanded) return
        val children = children
        var i = 0
        val n = this.children.size
        while (i < n) {
            children[i].removeFromTree(tree, actorIndex)
            i++
        }
    }

    fun add(node: ITreeNode) {
        insert(children.size, node)
    }

    fun addAll(nodes: ArrayList<ITreeNode>) {
        var i = 0
        val n = nodes.size
        while (i < n) {
            insert(children.size, nodes[i])
            i++
        }
    }

    fun insert(childIndex: Int, node: ITreeNode) {
        node.parent = this
        children.add(childIndex, node)
        if (!isExpanded) return
        val tree = tree
        if (tree != null) {
            val actorIndex: Int
            actorIndex = when {
                childIndex == 0 -> actor.zIndex + 1
                childIndex < children.size - 1 -> children[childIndex + 1].actor.zIndex
                else -> {
                    val before = children[childIndex - 1]
                    before.actor.zIndex + before.countActors()
                }
            }
            node.addToTree(tree, actorIndex)
        }
    }

    fun countActors(): Int {
        if (!isExpanded) return 1
        var count = 1
        val children = children
        var i = 0
        val n = this.children.size
        while (i < n) {
            count += children[i].countActors()
            i++
        }
        return count
    }

    /** Remove this node from the tree.  */
    fun remove() {
        val tree: Tree? = tree
        if (tree != null) {
            tree.remove(this)
        } else {
            parent?.remove(this)
        }
    }

    /** Remove the specified child node from the tree.  */
    fun remove(node: ITreeNode) {
        if (!children.remove(node)) return
        if (!isExpanded) return
        val tree = tree
        if (tree != null) node.removeFromTree(tree, node.actor.zIndex)
    }

    /** Remove all child nodes from the tree.  */
    fun removeAll() {
        if (isExpanded) {
            val tree = tree
            if (tree != null) {
                val actorIndex = actor.zIndex + 1
                val children = children
                var i = 0
                val n = this.children.size
                while (i < n) {
                    children[i].removeFromTree(tree, actorIndex)
                    i++
                }
            }
        }
        children.clear()
    }

    /** Removes the child node actors from the tree and adds them again. This is useful after changing the order of
     * [getChildren].
     * @see Tree.updateRootNodes
     */
    fun updateChildren() {
        if (!isExpanded) return
        val tree = tree ?: return
        val children = children
        val n = this.children.size
        var actorIndex = actor.zIndex + 1
        for (i in 0 until n) children[i].removeFromTree(tree, actorIndex)
        for (i in 0 until n) actorIndex += children[i].addToTree(tree, actorIndex)
    }

    /** Returns this node or the child node with the specified value, or null.  */
    fun findNode(predicate: (node: ITreeNode) -> Boolean) = Tree.findNode(children, predicate)
}