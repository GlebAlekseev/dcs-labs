package com.glebalekseevjk.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

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
                val polygonTriangulationX = mutableListOf<Double>()
                val polygonTriangulationY = mutableListOf<Double>()
                var points = CyclicMutableList(Data.polygon)
                val resultTriangles = mutableListOf<Triangle>()
                var additionalK = 0

                for (_i in 1..100000) {
                    val pointsBackup = CyclicMutableList(points.toList())

                    var minAngle = Double.MAX_VALUE
                    var minAngleIndex = 0
                    val d = dirTest(pointsBackup)

                    for ((index, point) in pointsBackup.withIndex()) {
                        var angle = getAngle(pointsBackup[index - 1], point, pointsBackup[index + 1])
                        if (d.sign != angle.sign) {
                            continue
                        }
                        angle = if (angle < 0) 2 * Math.PI + angle else angle
                        if (angle < minAngle) minAngle = (angle).also { minAngleIndex = index }
                    }

                    val fullV = Vector(pointsBackup[minAngleIndex], pointsBackup[minAngleIndex - 1])
                    val fullW = Vector(pointsBackup[minAngleIndex], pointsBackup[minAngleIndex + 1])

                    val kv = (fullV.module() / dataState.value.h).roundUp() + additionalK
                    val kw = (fullW.module() / dataState.value.h).roundUp() + additionalK
                    val k = (3 * minAngle / Math.PI).roundUp().toInt()

                    val v = fullV / kv
                    val w = fullW / kw

                    val a = pointsBackup[minAngleIndex] + v
                    val b = pointsBackup[minAngleIndex] + w

                    val tmpTriangles = mutableListOf<Triangle>()
                    if (Vector(a, b).module() < dataState.value.h || k == 1) {
                        tmpTriangles.add(Triangle(a, b, pointsBackup[minAngleIndex]))
                        pointsBackup.removeAt(minAngleIndex)
                        val pnts = mutableListOf(a, b)
                        if (pointsBackup.contains(a)) pnts.remove(a)
                        if (pointsBackup.contains(b)) pnts.remove(b)
                        pointsBackup.addAll(minAngleIndex, pnts)
                    } else if (k == 2) {
                        val c = pointsBackup[minAngleIndex] + u(1 / 2.0, v, w, d, minAngle)
                        tmpTriangles.add(Triangle(a, c, pointsBackup[minAngleIndex]))
                        tmpTriangles.add(Triangle(b, c, pointsBackup[minAngleIndex]))
                        pointsBackup.removeAt(minAngleIndex)
                        val pnts = mutableListOf(a, c, b)
                        if (pointsBackup.contains(a)) pnts.remove(a)
                        if (pointsBackup.contains(b)) pnts.remove(b)
                        pointsBackup.addAll(minAngleIndex, pnts)
                    } else if (k == 3) {
                        val e = pointsBackup[minAngleIndex] + u(2 / 3.0, v, w, d, minAngle)
                        val d = pointsBackup[minAngleIndex] + u(1 / 3.0, v, w, d, minAngle)
                        tmpTriangles.add(Triangle(a, d, pointsBackup[minAngleIndex]))
                        tmpTriangles.add(Triangle(e, d, pointsBackup[minAngleIndex]))
                        tmpTriangles.add(Triangle(e, b, pointsBackup[minAngleIndex]))
                        pointsBackup.removeAt(minAngleIndex)
                        val pnts = mutableListOf(a, d, e, b)
                        if (pointsBackup.contains(a)) pnts.remove(a)
                        if (pointsBackup.contains(d)) pnts.remove(d)
                        if (pointsBackup.contains(e)) pnts.remove(e)
                        if (pointsBackup.contains(b)) pnts.remove(b)
                        pointsBackup.addAll(minAngleIndex, pnts)
                    }

                    val newD = dirTest(pointsBackup)
                    if (newD == d && selfTest(pointsBackup)) {
                        points = pointsBackup
                        additionalK = 0
                        tmpTriangles.forEach {
                            resultTriangles.add(it)
                        }
                    } else additionalK++
                    if (points.size <= 3) break
                }

                points.forEach {
                    polygonTriangulationX.add(it.x)
                    polygonTriangulationY.add(it.y)
                }

                _resultDataState.emit(
                    ResultData(
                        Axis(
                            polygonTriangulationX.apply { add(first()) },
                            polygonTriangulationY.apply { add(first()) }),
                        resultTriangles,
                    )
                )
            }
        }
    }

    private fun getAngle(p1: Point, p2: Point, p3: Point): Double {
        val a = (p1.x - p2.x) * (p3.x - p2.x) + (p1.y - p2.y) * (p3.y - p2.y)
        val b = sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2)) * sqrt((p3.x - p2.x).pow(2) + (p3.y - p2.y).pow(2))
        val c = p2.x * (p3.y - p1.y) + p2.y * (p1.x - p3.x) + p3.x * p1.y - p3.y * p1.x
        if (c == 0.0) return Math.PI
        val result = if (c > 0) acos(a / b) else -acos(a / b)
        return result
    }

    private fun dirTest(points: CyclicMutableList<Point>): Double {
        var sum = 0.0
        for ((index, point) in points.withIndex()) {
            sum += nf2(points[0], points[index + 1], point)
        }
        return if (sum >= 0) 1.0 else -1.0
    }

    private fun nf2(a: Point, b: Point, c: Point): Double =
        a.x * (c.y - b.y) + a.y * (b.x - c.x) + c.x * b.y - c.y * b.x

    private fun r(lambda: Double, v: Vector, w: Vector): Double = (1 - lambda) * v.module() + lambda * w.module()

    private fun u(lambda: Double, v: Vector, w: Vector, d: Double, angle: Double): Vector {
        val psi = -lambda * d * angle
        val u = (v / v.module()) * r(lambda, v, w)
        return Vector(u.x * cos(psi) - u.y * sin(psi), u.x * sin(psi) + u.y * cos(psi))
    }

    private fun selfTest(points: CyclicMutableList<Point>): Boolean {
        fun f(i: Int, j: Int) = nf2(points[i], points[i + 1], points[j])
        for (i in 0 until points.size) {
            for (j in i + 2 until points.size - 1) {
                val res1 = ((f(i, j).sign != f(i, j + 1).sign) && (f(j, i).sign != f(j, i + 1).sign))
                val res2 = ((f(i, j) == f(i, j + 1) && f(i, j) == 0.0) && (f(j, i) == f(j, i + 1) && f(j, i) == 0.0))
                if (res1 && res2) {
                    return false
                }
            }
        }
        return true
    }
}

data class Triangle(val p1: Point, val p2: Point, val p3: Point)

data class Point(val x: Double, val y: Double) {
    operator fun plus(v: Vector): Point = Point(x + v.x, y + v.y)
}

data class Vector(val x: Double, val y: Double) {
    constructor(from: Point, to: Point) : this(to.x - from.x, to.y - from.y)

    fun module(): Double = sqrt(x.pow(2) + y.pow(2))
    operator fun div(denominator: Double): Vector = Vector(x / denominator, y / denominator)
    operator fun times(multiplier: Double): Vector = Vector(x * multiplier, y * multiplier)
    operator fun plus(summation: Vector): Vector = Vector(x + summation.x, y + summation.y)
}

class CyclicMutableList<T>(list: List<T>) : ArrayList<T>() {
    init {
        list.forEach {
            this.add(it)
        }
    }

    override operator fun get(index: Int): T =
        if (index < 0) super.get(this.lastIndex - (index.absoluteValue - 1) % this.size) else super.get(index % this.size)

    override operator fun set(index: Int, element: T): T =
        if (index < 0) super.set(
            this.lastIndex - (index.absoluteValue - 1) % this.size,
            element
        ) else super.set(index % this.size, element)

    override fun add(index: Int, element: T) {
        val cyclicIndex = if (index < 0) this.lastIndex - (index.absoluteValue - 1) % this.size else index % this.size
        super.add(cyclicIndex, element)
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val cyclicIndex = if (index < 0) this.lastIndex - (index.absoluteValue - 1) % this.size else index % this.size
        return super.addAll(cyclicIndex, elements)
    }
}

data class ResultData(
    val polygonTriangulation: Axis<Double, Double> = Axis(),
    val setOfTriangles: List<Triangle> = listOf(),
)

data class Axis<T1, T2>(
    val axisX: List<T1> = emptyList(),
    val axisY: List<T2> = emptyList(),
)

data class Data(
    val h: Double = 1.0, // "Шаг дискретизации"
) {
    companion object {
        // 3
        val polygon = listOf(
            Point(3.0, 5.5),
            Point(3.0, 4.5),
            Point(4.0, 3.5),
            Point(6.0, 3.5),
            Point(7.0, 4.5),
            Point(7.0, 6.0),
            Point(6.5, 6.5),
            Point(7.0, 7.0),
            Point(7.0, 8.5),
            Point(6.0, 9.5),
            Point(4.0, 9.5),
            Point(3.0, 8.5),
            Point(3.0, 7.5),
            Point(4.0, 7.5),
            Point(4.0, 8.0),
            Point(4.5, 8.5),
            Point(5.5, 8.5),
            Point(6.0, 8.0),
            Point(6.0, 7.5),
            Point(5.5, 7.0),
            Point(4.5, 7.0),
            Point(4.5, 6.0),
            Point(5.5, 6.0),
            Point(6.0, 5.5),
            Point(6.0, 5.0),
            Point(5.5, 4.5),
            Point(4.5, 4.5),
            Point(4.0, 5.0),
            Point(4.0, 5.5),
        )
    }
}