package com.glebalekseevjk.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

// TODO: Крайний случай

class Repository(initData: Data = Data()) {
    private val dataState: StateFlow<Data>
        get() = _dataState
    private val _dataState: MutableStateFlow<Data> = MutableStateFlow(initData)
    fun setDataState(data: Data) {
        CoroutineScope(Dispatchers.IO).launch {
            _dataState.emit(data)
        }
    }

    val resultDataState: StateFlow<ResultData>
        get() = _resultDataState
    private val _resultDataState: MutableStateFlow<ResultData> = MutableStateFlow(ResultData())

    init {
        CoroutineScope(Dispatchers.IO).launch {
            _dataState.collect {
                val computationalGridInCylindricalCoordinatesX = mutableListOf<Double>()
                val computationalGridInCylindricalCoordinatesY = mutableListOf<Double>()
                // base
                val alphaStep = 2*Math.PI / (dataState.value.numberOfPartitions)
                for (j in 0 .. dataState.value.numberOfPartitions.toInt()){
                    if (alphaStep * j + alphaStep/2 > Math.PI) continue
                    computationalGridInCylindricalCoordinatesX.add(dataState.value.firstLayerThickness * cos(alphaStep * j + alphaStep/2))
                    computationalGridInCylindricalCoordinatesY.add(dataState.value.firstLayerThickness * sin(alphaStep * j + alphaStep/2))
                }

                var prevDeltaRi = dataState.value.firstLayerThickness
                var prevPrevRi = 0.0
                var prevRi = prevPrevRi + prevDeltaRi

                for (i in 2..Int.MAX_VALUE){
                    val numberOfPartitions = dataState.value.numberOfPartitions + i - 1
                    var prevDeltaRiLocal = prevDeltaRi
                    var deltaRi = prevDeltaRi
                    for (j in 1..Int.MAX_VALUE){
                        val ki = getKi(i, deltaRi, prevRi)
                        val q = getQ(prevRi, ki, prevDeltaRi, prevPrevRi, getAlpha(i - 1))
                        val r = getR(prevRi, ki, prevDeltaRi, prevPrevRi, getAlpha(i - 1))
                        prevDeltaRiLocal = deltaRi
                        deltaRi = getDeltaR(q, r, prevRi)
                        if (conditionDeltaR(deltaRi, prevDeltaRiLocal)) break
                    }
                    val ri = (prevRi + deltaRi)
                    prevPrevRi = prevRi
                    prevRi = ri
                    prevDeltaRi = deltaRi
                    // добавление на график нового слоя
                    val step = Math.PI / (numberOfPartitions)
                    if (prevRi > dataState.value.radius) break
                    for (j in 0..(numberOfPartitions).toInt()){
                        if (step * j + step/2 > Math.PI) continue
                        computationalGridInCylindricalCoordinatesX.add(prevRi * cos(step * j + step/2))
                        computationalGridInCylindricalCoordinatesY.add(prevRi * sin(step * j + step/2))
                    }
                }
                _resultDataState.emit(
                    ResultData(
                        Axis(computationalGridInCylindricalCoordinatesX, computationalGridInCylindricalCoordinatesY)
                    )
                )
            }
        }
    }

    fun getAlpha(i: Int) = 2.0*Math.PI / (dataState.value.numberOfPartitions + i - 1)

    fun getKi(i: Int, deltaRi: Double, prevRi: Double) = (dataState.value.numberOfPartitions + i - 1) *
            deltaRi / (2.0 * Math.PI * (prevRi + deltaRi))

    fun getQ(prevRi: Double, ki: Double, prevDeltaRi: Double, prevPrevRi: Double, prevAlphaI: Double) = (4.0 * (prevRi.pow(2)) +
            3.0 * ki * prevDeltaRi * (2.0 * prevPrevRi + prevDeltaRi) * prevAlphaI) / 9.0

    fun getR(prevRi: Double, ki: Double, prevDeltaRi: Double, prevPrevRi: Double, prevAlphaI: Double) = (16 * (prevRi.pow(3)) -
            9 * ki * prevDeltaRi * (2 * prevPrevRi + prevDeltaRi) * prevRi * prevAlphaI) / 54.0

    fun getDeltaR(q: Double, r: Double, prevRi: Double) = -2.0 * sqrt(q) * cos(1.0/3.0 * acos(r/ sqrt(q.pow(3))) + 2.0/3.0 * Math.PI) - 2.0/3.0 * prevRi

    fun conditionDeltaR(nextIterationDeltaRi: Double, iterationDeltaRi: Double) = EPS >
            ((nextIterationDeltaRi - iterationDeltaRi).absoluteValue / iterationDeltaRi.absoluteValue)

    companion object {
        const val EPS = 0.01
    }
}

data class ResultData(
    val computationalGridInCylindricalCoordinates: Axis<Double, Double> = Axis(), // расчетной сетки в цилиндрических координатах
)

data class Axis<T1, T2>(
    val axisX: List<T1> = emptyList(),
    val axisY: List<T2> = emptyList(),
)

data class Data(
    val numberOfPartitions: Double = 14.0, // число разбиений по углу alpha
    val firstLayerThickness: Double = 10.0, // толщиной первого слоя delta r1
    val radius: Double = 100.0, // радиус
)