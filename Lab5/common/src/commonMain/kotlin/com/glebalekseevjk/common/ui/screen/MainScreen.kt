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
import com.glebalekseevjk.common.ui.widget.data.GeneralData
import ui.widget.MainWrapper
import ui.widget.linechart.sourcesignal.CombinedLineChartPlot

val repository = Repository()

val modifierTextField = Modifier.width(400.dp).padding(vertical = 5.dp)

fun numberOfPartitionsInputValidator(text: String): Boolean =
    text.toDoubleOrNull() != null && text.toDouble() > 1 && text.toDouble() < 100

fun firstLayerThicknessInputValidator(text: String): Boolean =
    text.toDoubleOrNull() != null && text.toDouble() > 0

fun radiusInputValidator(text: String): Boolean =
    text.toDoubleOrNull() != null && text.toDouble() > 0

@Composable
fun MainScreen() {
    val resultDataState by repository.resultDataState.collectAsState()
    var textFieldNumberOfPartitions by remember { mutableStateOf(TextFieldValue()) }
    var textFieldFirstLayerThickness by remember { mutableStateOf(TextFieldValue()) }
    var textFieldRadius by remember { mutableStateOf(TextFieldValue()) }

    fun setData(data: Data) {
        textFieldNumberOfPartitions = TextFieldValue(data.numberOfPartitions.toString())
        textFieldFirstLayerThickness = TextFieldValue(data.firstLayerThickness.toString())
        textFieldRadius = TextFieldValue(data.radius.toString())
    }

    fun updateData() {
        val data = runCatching {
            Data(
                numberOfPartitions = textFieldNumberOfPartitions.text.toDoubleOrNull() ?: throw RuntimeException(),
                firstLayerThickness = textFieldFirstLayerThickness.text.toDoubleOrNull() ?: throw RuntimeException(),
                radius = textFieldRadius.text.toDoubleOrNull() ?: throw RuntimeException(),
            )
        }
        val value = data.getOrNull()
        if (data.isSuccess
            && numberOfPartitionsInputValidator(textFieldNumberOfPartitions.text)
            && firstLayerThicknessInputValidator(textFieldFirstLayerThickness.text)
            && radiusInputValidator(textFieldRadius.text)
        ) repository.setDataState(value!!)
    }
    setData(Data())
    Column {
        MainWrapper {
            Column(Modifier.padding(top = 15.dp, bottom = 15.dp)) {
                OutlinedTextField(
                    singleLine = true,
                    value = textFieldNumberOfPartitions,
                    label = {
                        Text("Число разбиений по углу alpha")
                    },
                    isError = (!numberOfPartitionsInputValidator(textFieldNumberOfPartitions.text))
                        .also { if (!it) updateData() },
                    onValueChange = {
                        textFieldNumberOfPartitions = it
                    },
                    modifier = modifierTextField
                )
                OutlinedTextField(
                    singleLine = true,
                    value = textFieldFirstLayerThickness,
                    label = {
                        Text("Толщина первого слоя")
                    },
                    isError = (!firstLayerThicknessInputValidator(textFieldFirstLayerThickness.text))
                        .also { if (!it) updateData() },
                    onValueChange = {
                        textFieldFirstLayerThickness = it
                    },
                    modifier = modifierTextField
                )
                OutlinedTextField(
                    singleLine = true,
                    value = textFieldRadius,
                    label = {
                        Text("Радиус")
                    },
                    isError = (!firstLayerThicknessInputValidator(textFieldRadius.text))
                        .also { if (!it) updateData() },
                    onValueChange = {
                        textFieldRadius = it
                    },
                    modifier = modifierTextField
                )
            }

            Column(modifier = Modifier.height(500.dp).width(1000.dp).padding(15.dp)) {
                CombinedLineChartPlot(
                    GeneralData(
                        listOf(
                            Pair(
                                resultDataState.computationalGridInCylindricalCoordinates.axisX,
                                resultDataState.computationalGridInCylindricalCoordinates.axisY,
                            ),
                        ),
                        false
                    ),
                    title = "Расчетная сетка в цилиндрических координатах",
                    axisYLabel = "y, м",
                )
            }

        }
    }
}