package app.thelema.shader.node

class NormalToTBNNode: ShaderNode() {
    override val componentName: String
        get() = "NormalToTBNNode"

    var normal by input(GLSLNode.vertex.normal)

    var worldPosition by input(GLSLNode.vertex.position)

    var uv by input(GLSLNode.uv.uv)

    val tbn = output(GLSLMat3("tbn"))

    override fun declarationFrag(out: StringBuilder) {
        if (normal.isUsed) {
            out.append(tbn.typedRefEnd)
            out.append("""
mat3 cotangent_frame( vec3 N, vec3 p, vec2 uv ) {
    // get edge vectors of the pixel triangle
    vec3 dp1 = dFdx( p );
    vec3 dp2 = dFdy( p );
    vec2 duv1 = dFdx( uv );
    vec2 duv2 = dFdy( uv );

    // solve the linear system
    vec3 dp2perp = cross( dp2, N );
    vec3 dp1perp = cross( N, dp1 );
    vec3 T = dp2perp * duv1.x + dp1perp * duv2.x;
    vec3 B = dp2perp * duv1.y + dp1perp * duv2.y;

    // construct a scale-invariant frame 
    float invmax = inversesqrt( max( dot(T,T), dot(B,B) ) );
    return mat3( T * invmax, B * invmax, N );
}
""")
        }
    }

    override fun executionFrag(out: StringBuilder) {
        if (tbn.isUsed) {
            out.append("${tbn.ref} = cotangent_frame(${normal.ref}, ${worldPosition.asVec3()}, ${uv.asVec2()});\n")
        }
    }
}