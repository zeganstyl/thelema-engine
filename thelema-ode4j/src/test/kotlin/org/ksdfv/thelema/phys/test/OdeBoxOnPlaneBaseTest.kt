package org.ksdfv.thelema.phys.test

import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.APP
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.lwjgl3.Lwjgl3App
import org.ksdfv.thelema.lwjgl3.Lwjgl3AppConf
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.build.BoxMeshBuilder
import org.ksdfv.thelema.mesh.build.PlaneMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.utils.LOG
import org.ode4j.ode.*

/** [ODE4J demo](https://github.com/tzaeschke/ode4j/blob/master/demo/src/main/java/org/ode4j/demo/DemoBoxstack.java) */
object OdeBoxOnPlaneBaseTest {
    @JvmStatic
    fun main(args: Array<String>) {
        Lwjgl3App(Lwjgl3AppConf(windowWidth = 1280, windowHeight = 720)) {
            OdeConfig.setLibCCDEndabled(true)
            // create world
            OdeHelper.initODE2(0)
            val world = OdeHelper.createWorld()
            val space = OdeHelper.createHashSpace(null)
            val contactGroup = OdeHelper.createJointGroup()
            world.setGravity(0.0,-0.5,0.0)
            world.cfm = 1e-5
            world.autoDisableFlag = true

            world.autoDisableAverageSamplesCount = 10

            world.linearDamping = 0.00001
            world.angularDamping = 0.005
            world.maxAngularSpeed = 200.0

            world.contactMaxCorrectingVel = 0.1
            world.contactSurfaceLayer = 0.001
            OdeHelper.createPlane (space,0.0,1.0,0.0,0.0)

            val body = OdeHelper.createBody(world)
            body.setPosition(0.0, 2.0, 0.0)
            val m = OdeHelper.createMass()
            val boxShape = OdeHelper.createBox(space, 1.0, 1.0, 1.0)
            boxShape.body = body
            m.setBox(5.0, boxShape.lengths)
            body.mass = m

            val nearCallback = DGeom.DNearCallback { data, o1, o2 ->
                // if (o1->body && o2->body) return;

                // exit without doing anything if the two bodies are connected by a joint
                val b1 = o1.body
                val b2 = o2.body
                if (b1!=null && b2!=null && OdeHelper.areConnectedExcluding (b1,b2, DContactJoint::class.java)) return@DNearCallback

                //dContact[] contact=new dContact[MAX_CONTACTS];   // up to MAX_CONTACTS contacts per box-box
                val contacts = DContactBuffer(40)
                contacts.forEach { contact ->
                    contact.surface.mode = OdeConstants.dContactBounce or OdeConstants.dContactSoftCFM
                    contact.surface.mu = OdeConstants.dInfinity
                    contact.surface.mu2 = 0.0
                    contact.surface.bounce = 0.1
                    contact.surface.bounce_vel = 0.1
                    contact.surface.soft_cfm = 0.01
                }
                //	if (int numc = dCollide (o1,o2,MAX_CONTACTS,&contact[0].geom,
                //			sizeof(dContact))) {
                val numc = OdeHelper.collide (o1,o2,40,contacts.geomBuffer)//, sizeof(dContact));
                if (numc!=0) {
                    for (i in 0 until numc) {
                        val c = OdeHelper.createContactJoint (world,contactGroup,contacts.get(i))
                        c.attach(b1,b2)
                    }
                }
            }

            @Language("GLSL")
            val shader = Shader(
                vertCode = """
attribute vec3 aPosition;
attribute vec2 aUV;
varying vec2 uv;
uniform vec3 objPos;
uniform mat4 viewProj;

void main() {
    uv = aUV;
    gl_Position = viewProj * vec4(aPosition + objPos, 1.0);
}""",
                fragCode = """
varying vec2 uv;
void main() {
    gl_FragColor = vec4(uv, 0.0, 1.0);
}"""
            )

            val ground = PlaneMeshBuilder(width = 100f, height = 100f).apply {
                positionName = "aPosition"
                uvName = "aUV"
            }.build()

            val box = BoxMeshBuilder().apply {
                positionName = "aPosition"
                uvName = "aUV"
            }.build()

            ActiveCamera.api = Camera()

            val control = OrbitCameraControl(targetDistance = 20f)
            control.listenToMouse()
            LOG.info(control.help)

            val tmp = Vec3()

            GL.isDepthTestEnabled = true
            GL.glClearColor(0f, 0f, 0f, 1f)
            GL.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                space.collide(null, nearCallback)
                world.quickStep(0.02)
                // remove all contact joints
                contactGroup.empty()

                control.update(APP.deltaTime)
                ActiveCamera.update()

                shader.bind()
                shader["viewProj"] = ActiveCamera.viewProjectionMatrix

                shader["objPos"] = IVec3.Zero
                ground.render(shader)

                val pos = body.position
                tmp.set(pos.get0().toFloat(), pos.get1().toFloat(), pos.get2().toFloat())

                shader["objPos"] = tmp
                box.render(shader)
            }
        }
    }
}