package com.glebalekseevjk.common.ui.widget.data


interface Data {
    val axisX: List<Pair<String, MutableList<Double>>>
    val axisY: List<Pair<String, MutableList<Double>>>
    val minX: Double?
    val minY: Double?
    val maxX: Double?
    val maxY: Double?
    val colorMap: Map<String, androidx.compose.ui.graphics.Color>
    val isValid: Boolean
    val hasLegend: Boolean
}