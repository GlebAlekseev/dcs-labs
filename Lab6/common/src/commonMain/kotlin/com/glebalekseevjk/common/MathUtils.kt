package com.glebalekseevjk.common

fun Double.roundUp(): Double = if (this - this.toInt() == 0.0) this else this.toInt() + 1.0