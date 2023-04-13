package com.glebalekseevjk.common

fun Double.roundToString(n: Int) = "%.${n}f".format(this).replace(Regex("\\,*\\.*0+$"), "")