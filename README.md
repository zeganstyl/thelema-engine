![logo](images/thelema-logo-256.png)
### Thelema Engine

[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)
[![Discord](https://img.shields.io/discord/904058648572072038)](https://discord.gg/6j9tBJBE9g)

**Thelema** is multiplatform 3d graphics engine on Kotlin. It was based on libGDX sources and completely redesigned.

#### Download

Enter your credentials to download engine libraries.
[How to get token.](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/zeganstyl/thelema-engine")
        credentials {
            username = "username"
            password = "token"
        }
    }
}
```
```kotlin
dependencies {
    implementation("app.thelema:thelema-jvm:0.6.0")
}
```

[Quick Start](https://github.com/zeganstyl/thelema-engine/wiki/Quick-Start)

[Live Kotlin/JS tests](https://zeganstyl.github.io/thelema-kxjs-demo/)

[Thelema vs LibGDX](https://github.com/zeganstyl/thelema-engine/wiki/Thelema-vs-LibGDX)

[![youtube](images/youtube.png)](https://www.youtube.com/playlist?list=PLS4PI9m5p5MmodmfBNVft1_mUges3x35O)

Thelema uses entities and components to work with scenes.

#### Features
* Shaders
  * Shader nodes (shader graph)
  * Deferred shading
  * Physicaly based rendering (PBR)
  * Bloom, Emissive materials
  * SSAO
  * Cascaded shadow mapping, Soft shadows
  * Motion blur
  * IBL
* 3D graphics
  * VBO, VAO, Instancing buffers
  * Skinned meshes
  * Particle system
  * Lights: directional, point
  * glTF 2.0 loading
* Audio
  * Ogg/Vorbis loading
  * WAV loading
  * Procedural sound generation
* JSON
* Image loading from JPG, PNG, TGA, BMP, PSD, GIF, HDR, PIC
* ODE physics
* Platforms: Desktop JVM, HTML5, Android

![logo](images/screenshot.png)
Thelema Studio - 3D Editor

#### Work in progress
* GUI (redesign)
* 3D Audio API (redesign)
* Navigation mesh
* Vulkan API
* Terrain rendering
