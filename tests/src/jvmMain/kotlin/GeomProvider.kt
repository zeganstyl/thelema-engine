import app.thelema.gl.IIndexBuffer
import app.thelema.gl.IVertexAccessor
import org.recast4j.recast.ConvexVolume
import org.recast4j.recast.geom.InputGeomProvider
import org.recast4j.recast.geom.TriMesh

class GeomProvider : InputGeomProvider {
    val bmin: FloatArray = FloatArray(3) { Float.MAX_VALUE }
    val bmax: FloatArray = FloatArray(3) { Float.MIN_VALUE }
    val volumes: MutableList<ConvexVolume> = ArrayList()

    val trimeshes = ArrayList<TriMesh>()

    fun addTrimesh(vertices: IVertexAccessor, indices: IIndexBuffer): InputGeomProvider {
        val positionsArray = FloatArray(vertices.count * 3)
        val indicesArray = IntArray(indices.count)
        var i = 0
        vertices.forEachVec3f { x, y, z ->
            if (bmin[0] > x) bmin[0] = x
            if (bmin[1] > y) bmin[1] = y
            if (bmin[2] > z) bmin[2] = z

            if (bmax[0] < x) bmax[0] = x
            if (bmax[1] < y) bmax[1] = y
            if (bmax[2] < z) bmax[2] = z

            positionsArray[i] = x
            positionsArray[i+1] = y
            positionsArray[i+2] = z
            i += 3
        }

        i = 0
        indices.forEachIndex { indicesArray[i] = it; i++ }

        trimeshes.add(TriMesh(positionsArray, indicesArray))
        return this
    }

    override fun getMeshBoundsMin(): FloatArray = bmin
    override fun getMeshBoundsMax(): FloatArray = bmax
    override fun convexVolumes(): List<ConvexVolume> = volumes

    override fun meshes(): Iterable<TriMesh> = trimeshes
}
