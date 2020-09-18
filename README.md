![logo](images/thelema-logo-256.png)
### Thelema Engine

[ ![Download](https://api.bintray.com/packages/zeganstyl/thelema-engine/thelema-lwjgl3/images/download.svg) ](https://bintray.com/zeganstyl/thelema-engine/thelema-lwjgl3/_latestVersion)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)

**Thelema** is 3d graphics engine with high level absctraction from platforms through interfaces. It was based on libGDX sources and completely redesigned.

[Quick Start](https://github.com/zeganstyl/thelema-engine/wiki/Quick-Start)

[Live TeaVM tests](https://zeganstyl.github.io/thelema-teavm-tests/)

[Live Kotlin/JS tests](https://zeganstyl.github.io/thelema-kxjs-demo/)

[Thelema vs LibGDX](https://github.com/zeganstyl/thelema-engine/wiki/Thelema-vs-LibGDX)

[![youtube](images/youtube.png)](https://www.youtube.com/playlist?list=PLS4PI9m5p5MmodmfBNVft1_mUges3x35O)

#### Features
* Shaders
  * Autogenerating shaders by shader nodes
  * Deferred shading
  * Physicaly based rendering (PBR)
  * Emissive materials
  * Tonemapping
  * Bloom
  * SSAO
  * Cascaded shadow mapping
  * Motion blur
* 3D graphics
  * VBO, VAO, Instancing buffers
  * Skinned meshes
  * Lights: directional, point
  * glTF 2.0 loading
* Audio
  * Ogg/Vorbis loading
  * WAV loading
  * Procedural sound generation
* JSON
* Image loading from JPG, PNG, TGA, BMP, PSD, GIF, HDR, PIC
* ODE physics
* Platforms: desktop JVM, HTML5

#### Work in progress
* Thelema Creator - 3d editor for creating scenes, shaders, animations etc.
* HTML5 physics (simple objects such as boxes and spheres already implemented)
* Kotlin/Multiplatform modules
* GUI (it was rewritten to Kotlin from libGDX, but needs redesign)
* Audio interfaces redesign

#### Things that may be implemented in future
* Vulkan API (most likely to be)
* Android (most likely to be)
* WebGPU (when it will be available)
* WebAssembly (Kotlin/Native, but chances are not great)
