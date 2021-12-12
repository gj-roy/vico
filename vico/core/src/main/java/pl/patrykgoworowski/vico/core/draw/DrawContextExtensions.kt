/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.draw

import android.graphics.Canvas

public inline fun DrawContext.withCanvas(block: Canvas.() -> Unit) {
    canvas.block()
}

public fun drawContext(
    canvas: Canvas,
    density: Float,
    fontScale: Float,
    isLtr: Boolean,
    zoom: Float = 1f,
): DrawContext = object : DrawContext {
    override var canvas: Canvas = canvas
    override val density: Float = density
    override val fontScale: Float = fontScale
    override val isLtr: Boolean = isLtr
    override val isHorizontalScrollEnabled: Boolean = false
    override val zoom: Float = zoom

    override fun withOtherCanvas(canvas: Canvas, block: (DrawContext) -> Unit) {
        val originalCanvas = this.canvas
        this.canvas = canvas
        block(this)
        this.canvas = originalCanvas
    }
}