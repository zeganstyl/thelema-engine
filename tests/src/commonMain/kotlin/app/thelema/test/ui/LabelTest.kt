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

package app.thelema.test.ui

import app.thelema.app.APP
import app.thelema.app.AppListener
import app.thelema.test.Test
import app.thelema.ui.Label
import app.thelema.ui.HeadUpDisplay

class LabelTest: Test {
    override fun testMain() {
        val label = Label("""
Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. In nisl nisi scelerisque eu. Eu lobortis elementum nibh tellus molestie nunc non blandit massa. Vestibulum lorem sed risus ultricies tristique nulla aliquet. Bibendum ut tristique et egestas quis ipsum. Sit amet venenatis urna cursus eget nunc scelerisque. Nulla facilisi etiam dignissim diam quis enim lobortis. Est lorem ipsum dolor sit. Tempor id eu nisl nunc. Pellentesque pulvinar pellentesque habitant morbi.

Consectetur libero id faucibus nisl tincidunt eget nullam non. Quisque non tellus orci ac. Nunc sed blandit libero volutpat sed cras. Ipsum suspendisse ultrices gravida dictum fusce ut. Ac auctor augue mauris augue. Non tellus orci ac auctor. Leo in vitae turpis massa sed. Ullamcorper eget nulla facilisi etiam dignissim diam quis enim lobortis. Elit ut aliquam purus sit amet luctus venenatis. Dui vivamus arcu felis bibendum ut tristique et egestas quis. Rhoncus est pellentesque elit ullamcorper dignissim cras tincidunt. Turpis egestas maecenas pharetra convallis posuere morbi leo urna molestie. Volutpat consequat mauris nunc congue nisi vitae. In eu mi bibendum neque egestas congue quisque. Diam maecenas sed enim ut sem viverra aliquet. At consectetur lorem donec massa sapien. Mi quis hendrerit dolor magna eget est.

Urna id volutpat lacus laoreet. Eget nulla facilisi etiam dignissim diam. Morbi enim nunc faucibus a pellentesque. Adipiscing vitae proin sagittis nisl rhoncus mattis rhoncus urna neque. Elit ut aliquam purus sit amet luctus venenatis. Non nisi est sit amet facilisis magna. Phasellus egestas tellus rutrum tellus pellentesque eu. In hac habitasse platea dictumst vestibulum rhoncus. At lectus urna duis convallis convallis tellus id interdum. Consequat mauris nunc congue nisi vitae. Risus pretium quam vulputate dignissim suspendisse in est.

Amet purus gravida quis blandit turpis cursus in hac. Lobortis elementum nibh tellus molestie. Aliquam ut porttitor leo a diam sollicitudin tempor. Quis hendrerit dolor magna eget est lorem ipsum. Augue neque gravida in fermentum et sollicitudin ac orci. Orci a scelerisque purus semper eget duis at tellus at. Nisl purus in mollis nunc sed. Viverra tellus in hac habitasse platea dictumst vestibulum. Vitae aliquet nec ullamcorper sit amet risus nullam eget. Risus pretium quam vulputate dignissim suspendisse in est ante in. Fermentum odio eu feugiat pretium nibh ipsum. Elit duis tristique sollicitudin nibh sit amet. Volutpat consequat mauris nunc congue nisi vitae suscipit tellus mauris. Porta lorem mollis aliquam ut porttitor. Euismod lacinia at quis risus sed vulputate odio. Id diam vel quam elementum pulvinar etiam non quam. Enim sed faucibus turpis in. Urna neque viverra justo nec ultrices dui sapien eget mi.

Convallis aenean et tortor at. Vivamus at augue eget arcu dictum varius. Amet consectetur adipiscing elit duis tristique sollicitudin nibh sit. Suspendisse in est ante in. Arcu cursus euismod quis viverra nibh cras. Quis blandit turpis cursus in hac habitasse platea dictumst. Facilisis mauris sit amet massa vitae tortor condimentum lacinia quis. Id neque aliquam vestibulum morbi blandit cursus. Odio ut enim blandit volutpat. Fames ac turpis egestas sed tempus urna et. Id neque aliquam vestibulum morbi blandit cursus risus. Blandit turpis cursus in hac habitasse platea. Commodo odio aenean sed adipiscing diam donec adipiscing tristique risus. In massa tempor nec feugiat nisl pretium. Velit dignissim sodales ut eu sem integer. Vestibulum sed arcu non odio.
""")

        label.setWrap(true)
        label.fillParent = true

        val stage = HeadUpDisplay {
            addActor(label)
        }

        APP.onRender = {
            stage.update()
            stage.render()
        }
    }
}