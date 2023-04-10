package com.glebalekseevjk.common.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.glebalekseevjk.common.Data
import com.glebalekseevjk.common.Repository
import com.glebalekseevjk.common.ui.widget.data.TriangleData
import ui.widget.MainWrapper
import ui.widget.linechart.sourcesignal.CombinedLineChartPlot

val repository = Repository()

val modifierTextField = Modifier.width(400.dp).padding(vertical = 5.dp)

fun hInputValidator(text: String): Boolean =
    text.toDoubleOrNull() != null && text.toDouble() >= 0.2 && text.toDouble() < 100

@Composable
fun MainScreen() {
    val resultDataState by repository.resultDataState.collectAsState()
    var textFieldH by remember { mutableStateOf(TextFieldValue()) }

    fun setData(data: Data) {
        textFieldH = TextFieldValue(data.h.toString())
    }

    fun updateData() {
        val data = runCatching {
            Data(
                h = textFieldH.text.toDoubleOrNull() ?: throw RuntimeException(),
            )
        }
        val value = data.getOrNull()
        if (data.isSuccess
            && hInputValidator(textFieldH.text)
        ) repository.setDataState(value!!)
    }
    setData(Data())
    Column {
        MainWrapper {
            Column(Modifier.padding(top = 15.dp, bottom = 15.dp)) {
                OutlinedTextField(
                    singleLine = true,
                    value = textFieldH,
                    label = {
                        Text("h, м")
                    },
                    isError = (!hInputValidator(textFieldH.text))
                        .also { if (!it) updateData() },
                    onValueChange = {
                        textFieldH = it
                    },
                    modifier = modifierTextField
                )
            }

            Column(modifier = Modifier.height(650.dp).width(660.dp).padding(15.dp)) {
                CombinedLineChartPlot(
                    TriangleData(
                        resultDataState.setOfTriangles,
                        listOf(
                            Pair(
                                resultDataState.polygonTriangulation.axisX,
                                resultDataState.polygonTriangulation.axisY
                            )
                        ),
                        false,
                    ),
                    title = "Триангуляция полигона, Вариант 3",
                    axisYLabel = "y, м",
                )
            }
        }
    }
}