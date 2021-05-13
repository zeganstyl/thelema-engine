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

import app.thelema.g2d.Batch
import app.thelema.math.*


/** 2D scene graph node that may contain other actors.
 *
 *
 * Actors have a z-order equal to the order they were inserted into the group. Actors inserted later will be drawn on top of
 * actors added earlier. Touch events that hit more than one actor are distributed to topmost actors first.
 * @author mzechner
 * @author Nathan Sweet
 */
open class Group : Actor(), Cullable {
    open val children = ArrayList<Actor>()
    private val childrenTmp = ArrayList<Actor>()
    val worldTransform = Affine2()
    protected val computedTransform = Mat4()
    protected val oldTransform = Mat4()

    /** When true (the default), the Batch is transformed so children are drawn in their parent's coordinate system. This has a
     * performance impact because [Batch.flush] must be done before and after the transform. If the actors in a group are
     * not rotated or scaled, then the transform for the group can be set to false. In this case, each child's position will be
     * offset by the group's position for drawing, causing the children to appear in the correct location even though the Batch has
     * not been transformed.  */
    var isTransform = true

    /**
     * Children completely outside of this rectangle will not be drawn. This is only valid for use with unrotated and unscaled
     * actors.
     * @param cullingArea May be null.
     *
     * @return May be null.
     * @see .setCullingArea
     */
    override var cullingArea: Rectangle? = null

    override var stage: Stage?
        get() = super.stage
        set(value) {
            super.stage = value
            val childrenArray = children
            var i = 0
            val n = children.size
            while (i < n) {
                childrenArray[i].stage = stage // StackOverflowError here means the group is its own ancestor.
                i++
            }
        }

    override fun act(delta: Float) {
        super.act(delta)
        childrenTmp.clear()
        childrenTmp.addAll(children)
        for (i in childrenTmp.indices) {
            childrenTmp[i].act(delta)
        }
        childrenTmp.clear()
    }

    override fun updateTransform(recursive: Boolean) {
        super.updateTransform(recursive)

        if (recursive) {
            for (i in children.indices) {
                children[i].updateTransform(recursive)
            }
        }
    }

    /** Draws the group and its children. The default implementation calls [applyTransform] if needed, then
     * [drawChildren], then [resetTransform] if needed.  */
    override fun draw(batch: Batch, parentAlpha: Float) {
        if (isTransform) applyTransform(batch, computeTransform())
        drawChildren(batch, parentAlpha)
        if (isTransform) resetTransform(batch)
    }

    /** Draws all children. [applyTransform] should be called before and [resetTransform]
     * after this method if [isTransform] is true. If [isTransform] is false
     * these methods don't need to be called, children positions are temporarily offset by the group position when drawn. This
     * method avoids drawing children completely outside the [culling area][setCullingArea], if set.  */
    protected fun drawChildren(batch: Batch, parentAlpha: Float) {
        var parentAlpha = parentAlpha
        parentAlpha *= color.a
        val children = children
        val actors = children
        val cullingArea = cullingArea
        if (cullingArea != null) { // Draw children only if inside culling area.
            val cullLeft = cullingArea.x
            val cullRight = cullLeft + cullingArea.width
            val cullBottom = cullingArea.y
            val cullTop = cullBottom + cullingArea.height
            if (isTransform) {
                var i = 0
                val n = children.size
                while (i < n) {
                    val child = actors[i]
                    if (!child.isVisible) {
                        i++
                        continue
                    }
                    val cx = child.x
                    val cy = child.y
                    if (cx <= cullRight && cy <= cullTop && cx + child.width >= cullLeft && cy + child.height >= cullBottom) {
                        child.draw(batch, parentAlpha)
                    }
                    i++
                }
            } else { // No transform for this group, offset each child.
                val offsetX = x
                val offsetY = y
                x = 0f
                y = 0f
                var i = 0
                val n = children.size
                while (i < n) {
                    val child = actors[i]
                    if (!child.isVisible) {
                        i++
                        continue
                    }
                    val cx = child.x
                    val cy = child.y
                    if (cx <= cullRight && cy <= cullTop && cx + child.width >= cullLeft && cy + child.height >= cullBottom) {
                        child.x = cx + offsetX
                        child.y = cy + offsetY
                        child.draw(batch, parentAlpha)
                        child.x = cx
                        child.y = cy
                    }
                    i++
                }
                x = offsetX
                y = offsetY
            }
        } else { // No culling, draw all children.
            if (isTransform) {
                var i = 0
                val n = children.size
                while (i < n) {
                    val child = actors[i]
                    if (!child.isVisible) {
                        i++
                        continue
                    }
                    child.draw(batch, parentAlpha)
                    i++
                }
            } else { // No transform for this group, offset each child.
                val offsetX = x
                val offsetY = y
                x = 0f
                y = 0f
                var i = 0
                val n = children.size
                while (i < n) {
                    val child = actors[i]
                    if (!child.isVisible) {
                        i++
                        continue
                    }
                    val cx = child.x
                    val cy = child.y
                    child.x = cx + offsetX
                    child.y = cy + offsetY
                    child.draw(batch, parentAlpha)
                    child.x = cx
                    child.y = cy
                    i++
                }
                x = offsetX
                y = offsetY
            }
        }
    }

    /** Returns the transform for this group's coordinate system.  */
    protected fun computeTransform(): IMat4 {
        val worldTransform = worldTransform
        val originX = originX
        val originY = originY
        worldTransform.setToTRS(x + originX, y + originY, rotation, scaleX, scaleY)
        if (originX != 0f || originY != 0f) worldTransform.translate(-originX, -originY)
        // Find the first parent that transforms.
        var parentGroup = parent
        while (parentGroup != null) {
            if (parentGroup.isTransform) break
            parentGroup = parentGroup.parent
        }
        if (parentGroup != null) worldTransform.preMul(parentGroup.worldTransform)
        computedTransform.set(worldTransform)
        return computedTransform
    }

    /** Set the batch's transformation matrix, often with the result of [computeTransform]. Note this causes the batch to
     * be flushed. [resetTransform] will restore the transform to what it was before this call.  */
    protected fun applyTransform(batch: Batch, transform: IMat4) {
        oldTransform.set(batch.transformMatrix)
        batch.transformMatrix = transform
    }

    /** Restores the batch transform to what it was before [applyTransform]. Note this causes the batch to
     * be flushed.  */
    protected fun resetTransform(batch: Batch) {
        batch.transformMatrix = oldTransform
    }

    override fun hit(x: Float, y: Float, touchable: Boolean): Actor? {
        if (touchable && this.touchable == Touchable.Disabled) return null
        if (!isVisible) return null
        val point = tmp
        val childrenArray = children
        for (i in children.size - 1 downTo 0) {
            val child = childrenArray[i]
            child.parentToLocalCoordinates(point.set(x, y))
            val hit = child.hit(point.x, point.y, touchable)
            if (hit != null) return hit
        }
        return super.hit(x, y, touchable)
    }

    /** Called when actors are added to or removed from the group.  */
    protected open fun childrenChanged() {}

    /** Adds an actor as a child of this group, removing it from its previous parent. If the actor is already a child of this
     * group, no changes are made.  */
    open fun addActor(actor: Actor) {
        if (actor.parent != null) {
            if (actor.parent === this) return
            (actor.parent as Group).removeActor(actor, false)
        }
        children.add(actor)
        actor.parent = this
        actor.stage = stage
        childrenChanged()
    }

    /** Adds an actor as a child of this group at a specific index, removing it from its previous parent. If the actor is already a
     * child of this group, no changes are made.
     * @param index May be greater than the number of children.
     */
    open fun addActorAt(index: Int, actor: Actor) {
        if (actor.parent != null) {
            if (actor.parent === this) return
            (actor.parent as Group).removeActor(actor, false)
        }
        if (index >= children.size) children.add(actor) else children.add(index, actor)
        actor.parent = this
        actor.stage = stage
        childrenChanged()
    }

    /** Adds an actor as a child of this group immediately before another child actor, removing it from its previous parent. If the
     * actor is already a child of this group, no changes are made.  */
    open fun addActorBefore(actorBefore: Actor, actor: Actor) {
        if (actor.parent != null) {
            if (actor.parent === this) return
            (actor.parent as Group).removeActor(actor, false)
        }
        val index = children.indexOf(actorBefore)
        children.add(index, actor)
        actor.parent = this
        actor.stage = stage
        childrenChanged()
    }

    /** Adds an actor as a child of this group immediately after another child actor, removing it from its previous parent. If the
     * actor is already a child of this group, no changes are made. If `actorAfter` is not in this group, the actor is
     * added as the last child.  */
    open fun addActorAfter(actorAfter: Actor, actor: Actor) {
        if (actor.parent != null) {
            if (actor.parent === this) return
            (actor.parent as Group).removeActor(actor, false)
        }
        val index = children.indexOf(actorAfter)
        if (index == children.size || index == -1) children.add(actor) else children.add(index + 1, actor)
        actor.parent = this
        actor.stage = stage
        childrenChanged()
    }

    /** Removes an actor from this group and unfocuses it. Calls [removeActor] with true.  */
    open fun removeActor(actor: Actor): Boolean {
        return removeActor(actor, true)
    }

    /** Removes an actor from this group. Calls [removeActorAt] with the actor's child index.  */
    open fun removeActor(actor: Actor, unfocus: Boolean): Boolean {
        val index = children.indexOf(actor)
        if (index == -1) return false
        removeActorAt(index, unfocus)
        return true
    }

    /** Removes an actor from this group.
     * @param unfocus If true, [Stage.unfocus] is called.
     * @return the actor removed from this group or null.
     */
    open fun removeActorAt(index: Int, unfocus: Boolean): Actor? {
        return if (index < children.size) {
            val actor = children.removeAt(index)
            if (unfocus) {
                val stage = stage
                stage?.unfocus(actor)
            }
            actor.parent = null
            actor.stage = null
            childrenChanged()
            actor
        } else {
            null
        }
    }

    /** Removes all actors from this group.  */
    open fun clearChildren() {
        val actors = children
        var i = 0
        val n = children.size
        while (i < n) {
            val child = actors[i]
            child.stage = null
            child.parent = null
            i++
        }
        children.clear()
        childrenChanged()
    }

    /** Removes all children, actions, and listeners from this group.  */
    override fun clear() {
        super.clear()
        clearChildren()
    }

    /** Returns the first actor found with the specified name. Note this recursively compares the name of every actor in the
     * group.  */
    open fun <T : Actor> findActor(name: String): T? {
        val children = children
        run {
            var i = 0
            val n = children.size
            while (i < n) {
                if (name == children[i].name) return children[i] as T?
                i++
            }
        }
        var i = 0
        val n = children.size
        while (i < n) {
            val child = children[i]
            if (child is Group) {
                val actor = child.findActor<Actor>(name)
                if (actor != null) return actor as T
            }
            i++
        }
        return null
    }

    override fun containsDeep(actor: Actor?): Boolean {
        if (actor == null) return false
        if (children.contains(actor)) return true
        for (i in children.indices) {
            if (children[i].containsDeep(actor)) {
                return true
            }
        }
        return false
    }

    /** Swaps two actors by index. Returns false if the swap did not occur because the indexes were out of bounds.  */
    fun swapActor(first: Int, second: Int): Boolean {
        val maxIndex = children.size
        if (first < 0 || first >= maxIndex) return false
        if (second < 0 || second >= maxIndex) return false
        val firstEl =  children[first]
        children[first] = children[second]
        children[second] = firstEl
        return true
    }

    /** Swaps two actors. Returns false if the swap did not occur because the actors are not children of this group.  */
    fun swapActor(first: Actor?, second: Actor?): Boolean {
        val firstIndex = children.indexOf(first)
        val secondIndex = children.indexOf(second)
        if (firstIndex == -1 || secondIndex == -1) return false
        val firstEl =  children[firstIndex]
        children[firstIndex] = children[secondIndex]
        children[secondIndex] = firstEl
        return true
    }

    /** Converts coordinates for this group to those of a descendant actor. The descendant does not need to be a direct child.
     * @throws IllegalArgumentException if the specified actor is not a descendant of this group.
     */
    fun localToDescendantCoordinates(descendant: Actor, localCoords: Vec2): Vec2 {
        val parent = descendant.parent ?: throw IllegalArgumentException("Child is not a descendant: $descendant")
        // First convert to the actor's parent coordinates.
        if (parent !== this) localToDescendantCoordinates(parent, localCoords)
        // Then from each parent down to the descendant.
        descendant.parentToLocalCoordinates(localCoords)
        return localCoords
    }

    companion object {
        private val tmp = Vec2()
    }
}
