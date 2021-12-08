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


/**
 * [Actor] related utils.
 * @author Kotcrab
 */
object ActorUtils {
    /**
     * Makes sures that actor will be fully visible in stage. If it's necessary actor position will be changed to fit it
     * on screen.
     * @throws IllegalStateException if actor does not belong to any stage.
     */
    fun keepWithinStage(actor: Actor) {
        val stage = actor.hud
                ?: throw IllegalStateException("keepWithinStage cannot be used on Actor that doesn't belong to any stage. ")
        keepWithinStage(actor.hud!!, actor)
    }

    /**
     * Makes sures that actor will be fully visible in stage. If it's necessary actor position will be changed to fit it
     * on screen.
     */
    fun keepWithinStage(headUpDisplay: HeadUpDisplay, actor: Actor) { //taken from scene2d.ui Window
        val camera = headUpDisplay.camera
        val parentWidth = headUpDisplay.width
        val parentHeight = headUpDisplay.height
        if (actor.getX(Align.right) - camera.position.x > parentWidth / 2 / camera.zoom) actor.setPosition(camera.position.x + parentWidth / 2 / camera.zoom, actor.getY(Align.right), Align.right)
        if (actor.getX(Align.left) - camera.position.x < -parentWidth / 2 / camera.zoom) actor.setPosition(camera.position.x - parentWidth / 2 / camera.zoom, actor.getY(Align.left), Align.left)
        if (actor.getY(Align.top) - camera.position.y > parentHeight / 2 / camera.zoom) actor.setPosition(actor.getX(Align.top), camera.position.y + parentHeight / 2 / camera.zoom, Align.top)
        if (actor.getY(Align.bottom) - camera.position.y < -parentHeight / 2 / camera.zoom) actor.setPosition(actor.getX(Align.bottom), camera.position.y - parentHeight / 2 / camera.zoom, Align.bottom)
    }
}
