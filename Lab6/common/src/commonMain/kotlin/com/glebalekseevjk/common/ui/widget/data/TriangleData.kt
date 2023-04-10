package com.glebalekseevjk.common.ui.widget.data

import com.glebalekseevjk.common.Triangle
import io.github.koalaplot.core.util.generateHueColorPalette

data class TriangleData(
    private val setOfTriangles: List<Triangle>,
    private val axis: List<Pair<List<Double>, List<Double>>>,
    override val hasLegend: Boolean = true
) : Data {
    override val axisX = mutableListOf(
        "Исходный полигон" to mutableListOf<Double>(),
    )
    override val axisY = mutableListOf(
        "Исходный полигон" to mutableListOf<Double>(),
    )

    init {
        axis.forEachIndexed { index, (axisX, axisY) ->
            axisX.forEach {
                this.axisX[index].second.add(it)
            }
            axisY.forEach {
                this.axisY[index].second.add(it)
            }
        }
        setOfTriangles.forEachIndexed { index, triangle ->
            axisX.add(Pair("#$index", mutableListOf(triangle.p1.x, triangle.p2.x, triangle.p3.x, triangle.p1.x)))
            axisY.add(Pair("#$index", mutableListOf(triangle.p1.y, triangle.p2.y, triangle.p3.y, triangle.p1.y)))
        }
    }

    override val minY = axisY.map { it.second.minOrNull() }.filter { it != null }.minOfOrNull { it!! }
    override val maxY = axisY.map { it.second.maxOrNull() }.filter { it != null }.maxOfOrNull { it!! }

    override val minX = axisX.map { it.second.minOrNull() }.filter { it != null }.minOfOrNull { it!! }
    override val maxX = axisX.map { it.second.maxOrNull() }.filter { it != null }.maxOfOrNull { it!! }

    override val colorMap = buildMap {
        val colors = generateHueColorPalette(axisY.size)
        var i = 0
        axisY.forEach {
            put(it.first, colors[i++])
        }
    }

    override val isValid: Boolean
        get() = minX != null && maxX != null && minY != null && maxY != null
}