package ui.widget.linechart.sourcesignal

import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.glebalekseevjk.common.ui.widget.data.Data
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.legend.FlowLegend
import io.github.koalaplot.core.legend.LegendLocation
import io.github.koalaplot.core.line.DefaultPoint
import io.github.koalaplot.core.line.LineChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.VerticalRotation
import io.github.koalaplot.core.util.rotateVertically
import io.github.koalaplot.core.xychart.*
import io.github.koalaplot.sample.*

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
@Suppress("MagicNumber")
fun CombinedLineChartPlot(data: Data, modifier: Modifier = Modifier, title: String = "", axisYLabel: String = "") {
    ChartLayout(
        modifier = modifier,
        title = { ChartTitle(title) },
        legend = { if (data.hasLegend) Legend(data) },
        legendLocation = LegendLocation.BOTTOM
    ) {
        XYChart(
            xAxisModel = LinearAxisModel(
                if (data.isValid) (data.minX!!*1.1f..data.maxX!!*1.1f)
                else (0.0..1.0),
                minimumMajorTickSpacing = 25.dp,
                minimumMajorTickIncrement = 0.1,
                minorTickCount = 1
            ),
            yAxisModel = LinearAxisModel(
                if (data.isValid) (data.minY!! -10f..data.maxY!!*1.1f)
                else (0.0..1.0),
                minimumMajorTickSpacing = 50.dp,
                minimumMajorTickIncrement = 100.0,
                minorTickCount = 1
            ),
            xAxisLabels = {
                AxisLabel(it.toInt().toString(), Modifier.padding(top = 2.dp))
            },
            xAxisTitle = { AxisTitle("x, м") },
            yAxisLabels = {
                AxisLabel(it.toInt().toString(), Modifier.absolutePadding(right = 2.dp))
            },
            yAxisTitle = {
                AxisTitle(
                    axisYLabel,
                    modifier = Modifier.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
                        .padding(bottom = padding)
                )
            }
        ) {
            data.axisY.forEachIndexed { index, (label, powerList) ->
                chart(
                    data,
                    label,
                    powerList.mapIndexed { i: Int, d: Double ->
                        DefaultPoint(data.axisX[index].second[i], d)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun XYChartScope<Double, Double>.chart(
    sourceSignal: Data,
    caseName: String,
    data: List<DefaultPoint<Double, Double>>,
) {
    LineChart(
        data = data,
        isSpline = false,
        splineMinWeight = 100.0,
        lineStyle = LineStyle(
            brush = SolidColor(sourceSignal.colorMap[caseName] ?: Color.Black),
            strokeWidth = 6.dp,
//            alpha = 0.6f
            alpha = 0.0f
        ),
        symbol = { point ->
            Symbol(
                size = 7.dp,
                shape = RoundedCornerShape(50),
                fillBrush = SolidColor(sourceSignal.colorMap[caseName] ?: Color.Black),
                modifier = Modifier.then(
                    Modifier.hoverableElement {
                        HoverSurface { Text(point.y.toString()) }
                    }
                ),
            )
        }
    )
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun Legend(sourceSignal: Data) {
    val cases = sourceSignal.axisY.map { it.first }.sorted()

    Surface(elevation = 2.dp) {
        FlowLegend(
            itemCount = cases.size,
            symbol = { i ->
                Symbol(
                    modifier = Modifier.size(padding),
                    fillBrush = SolidColor(sourceSignal.colorMap[cases[i]] ?: Color.Black)
                )
            },
            label = { i ->
                Text(cases[i])
            },
            modifier = paddingMod
        )
    }
}
