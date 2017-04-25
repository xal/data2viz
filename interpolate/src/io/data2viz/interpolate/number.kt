package io.data2viz.interpolate

fun interpolateNumber(a:Number, b:Number): (Number) -> Number = {t:Number -> a.toDouble() + t.toDouble() * (b.toDouble() - a.toDouble()) }
fun uninterpolate(start:Number, end: Number) :(Number) -> Number = {x -> (x.toDouble() - start.toDouble())/(end.toDouble() - start.toDouble())}
fun identity(a:Double) = a
