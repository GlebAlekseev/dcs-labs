package com.glebalekseevjk.common.ui.widget.data

import io.github.koalaplot.core.util.generateHueColorPalette

data class GeneralData(
    private val axis: List<Pair<List<Double>, List<Double>>>, override val hasLegend: Boolean = true
) : Data {
    override val axisX = listOf(
        "Сетка" to mutableListOf<Double>(),
    )
    override val axisY = listOf(
        "Сетка" to mutableListOf<Double>(),
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