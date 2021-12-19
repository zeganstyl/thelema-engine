package app.thelema.ecs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntityPaths {
    init {
        if (!ECS.allDescriptors.containsKey(TestComp.Name)) ECS.descriptor { TestComp() }
    }

    @Test
    fun makePathToComponent() {
        Entity {
            val path = "child/child2:${TestComp.Name}"
            val component = makePathToComponent(path)
            assertEquals(path, component.path)
            assertTrue(component is TestComp)
        }
    }
}