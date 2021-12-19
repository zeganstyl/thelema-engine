package app.thelema.ecs

class ComponentNotFoundException(componentName: String): Throwable("Can't find component type: $componentName")