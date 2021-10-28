package app.thelema.studio

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent

class ComponentsList(val entity: IEntity): List<IEntityComponent> {
    override val size: Int
        get() = entity.getComponentsCount()

    override fun contains(element: IEntityComponent): Boolean = entity.containsComponent(element)

    override fun containsAll(elements: Collection<IEntityComponent>): Boolean {
        elements.forEach { if (!contains(it)) return false }
        return true
    }

    override fun get(index: Int): IEntityComponent = entity.getComponent(index)

    override fun indexOf(element: IEntityComponent): Int = entity.indexOfComponent(element)

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): Iterator<IEntityComponent> = ListIteratorImpl(0)

    override fun lastIndexOf(element: IEntityComponent): Int = indexOf(element)

    override fun listIterator(): ListIterator<IEntityComponent> = ListIteratorImpl(0)

    override fun listIterator(index: Int): ListIterator<IEntityComponent> = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<IEntityComponent> {
        TODO("Not yet implemented")
    }

    private inner class ListIteratorImpl(index: Int) : ListIterator<IEntityComponent> {
        protected var index = 0

        init {
            if (index < 0 || index > size) {
                throw IndexOutOfBoundsException("index: $index, size: $size")
            }
            this.index = index
        }

        override fun hasNext(): Boolean = index < size

        override fun next(): IEntityComponent {
            if (!hasNext()) throw NoSuchElementException()
            return get(index++)
        }

        override fun hasPrevious(): Boolean = index > 0

        override fun nextIndex(): Int = index

        override fun previous(): IEntityComponent {
            if (!hasPrevious()) throw NoSuchElementException()
            return get(--index)
        }

        override fun previousIndex(): Int = index - 1
    }
}