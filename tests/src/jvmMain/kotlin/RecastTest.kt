import app.thelema.anim.AnimationPlayer
import app.thelema.ecs.Entity
import app.thelema.ecs.IEntity
import app.thelema.ecs.componentOrNull
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.light.directionalLight
import app.thelema.g3d.material
import app.thelema.g3d.scene
import app.thelema.g3d.transformNode
import app.thelema.gl.*
import app.thelema.gltf.gltf
import app.thelema.input.IKeyListener
import app.thelema.input.KB
import app.thelema.input.KEY
import app.thelema.lwjgl3.JvmApp
import app.thelema.math.Vec4
import app.thelema.res.RES
import app.thelema.shader.SimpleShader3D
import app.thelema.utils.Color
import org.recast4j.detour.*
import org.recast4j.recast.*
import org.recast4j.recast.geom.InputGeomProvider

fun main() {
    val app = JvmApp {
        width = 1280
        height = 720
        msaaSamples = 4
    }

    ActiveCamera {
        setNearFar(0.1f, 100f)
    }

    KB.addListener(object : IKeyListener {
        override fun keyDown(keycode: Int) {
            if (keycode == KEY.SPACE) println(ActiveCamera.eye)
        }
    })

    Entity {
        val root = this
        makeCurrent()
        orbitCameraControl {
            targetDistance = 10f
        }
        scene()
        entity {
            directionalLight {
                color.setColor(Color.SLATE)
                setDirectionFromPosition(1f, 1f, 1f)
                setupShadowMaps(1024, 1024)
            }
        }

        val geom = GeomProvider()

        RES.gltf("dark_knight/scene.gltf") {
            conf.receiveShadows = true
            onLoaded {
                addEntity(scene.copyDeep().apply {
                    transformNode {
                        setScale(0.02f, 0.02f, 0.02f)
                        requestTransformUpdate()
                    }
                    componentOrNull<AnimationPlayer>()?.setAnimation("Dark_Knight_Bones|Dark_Knight_Walk")
                    componentOrNull<AnimationPlayer>()?.setAnimation("Dark_Knight_Bones|Dark_Knight_Attack", loopCount = 1, speed = 2f)
                })
            }
        }

        RES.gltf("dungeon/dungeon-low-poly-2.gltf") {
            conf.pbrConf = {
                receiveShadows = true
                shadowCascadesNum = 5
            }
            onLoaded {
                addEntity(scene.copyDeep().apply {
                    forEachComponentInBranch { component ->
                        if (component is IMesh) {
                            component.inheritedMesh?.getAttribute("POSITION") {
                                geom.addTrimesh(this, component.indices!!)
                            }
                        }
                    }

                    recast(geom, root)
                })
            }
        }
    }

    app.startLoop()
}

fun IEntity.createLineEntity(path: List<StraightPathItem>) {
    entity {
        mesh {
            primitiveType = GL_LINE_STRIP
            addVertexBuffer {
                addAttribute(3, "POSITION")
                initVertexBuffer(path.size) {
                    path.forEach {
                        putFloat(it.pos[0])
                        putFloat(it.pos[1])
                        putFloat(it.pos[2])
                    }
                }
            }
        }
        material {
            shader = SimpleShader3D { color = Vec4(1f, 0f, 0f, 1f) }
        }
    }
}

inline fun IVertexAttribute.forEachVec3f(block: (x: Float, y: Float, z: Float) -> Unit) {
    var byteOffset = this.byteOffset
    val stride = stride
    val count = count
    for (i in 0 until count) {
        block(getFloat(byteOffset), getFloat(byteOffset + 4), getFloat(byteOffset + 8))
        byteOffset += stride
    }
}

inline fun IIndexBuffer.replaceIndices(block: (index: Int) -> Int) {
    val bytes = bytes
    var byteOffset = 0
    val size = count
    when (indexType) {
        GL_UNSIGNED_BYTE -> {
            for (i in 0 until size) {
                bytes.put(byteOffset, block(bytes[byteOffset].toInt() and 0xFF).toByte())
                byteOffset += 1
            }
        }
        GL_UNSIGNED_SHORT -> {
            for (i in 0 until size) {
                bytes.put()
                bytes.putShort(byteOffset, block(bytes.getUShort(byteOffset)))
                byteOffset += 2
            }
        }
        GL_UNSIGNED_INT -> {
            for (i in 0 until size) {
                bytes.putInt(byteOffset, block(bytes.getInt(byteOffset)))
                byteOffset += 4
            }
        }
    }
}

inline fun IIndexBuffer.forEachIndex(block: (index: Int) -> Unit) {
    val bytes = bytes
    var byteOffset = 0
    val size = count
    when (indexType) {
        GL_UNSIGNED_BYTE -> {
            for (i in 0 until size) {
                block(bytes[byteOffset].toInt() and 0xFF)
                byteOffset += 1
            }
        }
        GL_UNSIGNED_SHORT -> {
            for (i in 0 until size) {
                bytes.put()
                block(bytes.getUShort(byteOffset))
                byteOffset += 2
            }
        }
        GL_UNSIGNED_INT -> {
            for (i in 0 until size) {
                block(bytes.getInt(byteOffset))
                byteOffset += 4
            }
        }
    }
}

fun recast(geom: InputGeomProvider, root: IEntity) {
    val cellSize = 0.3f
    val cellHeight = 0.3f
    val agentHeight = 1.0f
    val agentRadius = 1.3f
    val agentMaxClimb = 0.9f
    val agentMaxSlope = 45.0f
    val regionMinSize = 8
    val regionMergeSize = 20
    val edgeMaxLen = 12.0f
    val edgeMaxError = 1.3f
    val vertsPerPoly = 6
    val detailSampleDist = 1.0f
    val detailSampleMaxError = 1.0f

    val cfg = RecastConfig(
        RecastConstants.PartitionType.MONOTONE, cellSize, cellHeight, agentHeight, agentRadius,
        agentMaxClimb, agentMaxSlope, regionMinSize, regionMergeSize, edgeMaxLen, edgeMaxError,
        vertsPerPoly, detailSampleDist, detailSampleMaxError, SampleAreaModifications.AREAMOD_GROUND
    )
    val bcfg = RecastBuilderConfig(cfg, geom.meshBoundsMin, geom.meshBoundsMax)
    val rcResult = RecastBuilder().build(geom, bcfg)
    val polyMesh = rcResult.mesh
    for (i in 0 until polyMesh.npolys) {
        polyMesh.flags[i] = 1
    }
    val meshDetail = rcResult.meshDetail
    val params = NavMeshDataCreateParams()
    params.verts = polyMesh.verts
    params.vertCount = polyMesh.nverts
    params.polys = polyMesh.polys
    params.polyAreas = polyMesh.areas
    params.polyFlags = polyMesh.flags
    params.polyCount = polyMesh.npolys
    params.nvp = polyMesh.nvp
    params.detailMeshes = meshDetail.meshes
    params.detailVerts = meshDetail.verts
    params.detailVertsCount = meshDetail.nverts
    params.detailTris = meshDetail.tris
    params.detailTriCount = meshDetail.ntris
    params.walkableHeight = agentHeight
    params.walkableRadius = agentRadius
    params.walkableClimb = agentMaxClimb
    params.bmin = polyMesh.bmin
    params.bmax = polyMesh.bmax
    params.cs = cellSize
    params.ch = cellHeight
    params.buildBvTree = true

    val meshData = NavMeshBuilder.createNavMeshData(params)

    val navmesh = NavMesh(meshData, vertsPerPoly, 0)
    val query = NavMeshQuery(navmesh)

    val extents = floatArrayOf(10f, 10f, 10f)
    val start = floatArrayOf(5.9905267f,2.8559537f,-4.851427f)
    val end = floatArrayOf(-264.95224f,4.5066824f,-35.08475f)

    val filter: QueryFilter = DefaultQueryFilter()

    val startPoly = query.findNearestPoly(start, extents, filter).result
    val endPoly = query.findNearestPoly(end, extents, filter).result

    val path = query.findPath(startPoly.nearestRef, endPoly.nearestRef, startPoly.nearestPos, endPoly.nearestPos, filter)
    val straightPath = query.findStraightPath(startPoly.nearestPos, endPoly.nearestPos, path.result, Int.MAX_VALUE, 0)

    root.createLineEntity(straightPath.result)
}

fun FloatArray.vec3Str() = "(${get(0)}, ${get(1)}, ${get(2)})"