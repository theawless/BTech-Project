package com.gobbledygook.theawless.srlib.utils

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

object Window {
    fun sine(list: MutableList<Double>) {
        val Q = list.size - 1
        val w = List(Q + 1, { i -> 1.0 + (Q / 2.0) * sin((PI * i) / Q) })
        list.forEachIndexed { i, _ -> list[i] *= w[i] }
    }

    fun hamming(list: MutableList<Double>) {
        val p = list.size - 1
        list.forEachIndexed { i, _ -> list[i] *= 0.54 - (0.46 * cos((2.0 * PI * i) / p)) }
    }
}

object Distance {
    fun tokhura(listA: List<Double>, listB: List<Double>): Double {
        val w = listOf(0.0, 1.0, 3.0, 7.0, 13.0, 19.0, 22.0, 25.0, 33.0, 42.0, 50.0, 56.0, 61.0)
        var d = 0.0
        w.forEachIndexed { i, _ -> d += w[i] * (listA[i] - listB[i]).pow(2) }
        return d
    }
}

object Relation {
    fun energy(list: List<Double>): Double = autoCorrelation(list, 0)[0]

    fun autoCorrelation(list: List<Double>, p: Int): MutableList<Double> {
        val ac = MutableList(p + 1, { _ -> 0.0 })
        for (i in 0..p) {
            for (j in 0 until list.size - i) {
                ac[i] += list[j] * list[j + i]
            }
        }
        return ac
    }

    fun cepstralCoefficients(list: List<Double>): MutableList<Double> {
        val c = MutableList(list.size, { i -> list[i] })
        for (i in 2 until list.size) {
            for (j in 1 until i) {
                c[i] += (j * c[j] * list[i - j]) / i
            }
        }
        return c
    }
}

object Matrix {
    fun durbinSolve(R: List<Double>): MutableList<Double> {
        val p = R.size - 1
        val E = MutableList(p + 1, { _ -> 0.0 })
        val a = MutableList(p + 1, { _ -> MutableList(p + 1, { _ -> 0.0 }) })

        E[0] = R[0]
        a[1][1] = R[1] / R[0]
        E[1] = (1.0 - a[1][1].pow(2)) * E[0]
        for (i in 2..p) {
            a[i][i] = R[i]
            for (j in 1 until i) {
                a[i][i] -= a[i - 1][j] * R[i - j]
            }
            if (E[i - 1] != 0.0) {
                a[i][i] /= E[i - 1]
            }
            for (j in 1 until i) {
                a[i][j] = a[i - 1][j] - a[i][i] * a[i - 1][i - j]
            }
            E[i] = (1.0 - a[i][i].pow(2)) * E[i - 1]
        }
        return a[p]
    }
}