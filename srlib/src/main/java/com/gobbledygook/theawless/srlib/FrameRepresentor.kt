package com.gobbledygook.theawless.srlib

import com.gobbledygook.theawless.srlib.utils.Matrix
import com.gobbledygook.theawless.srlib.utils.Relation
import com.gobbledygook.theawless.srlib.utils.SpeechConstants
import com.gobbledygook.theawless.srlib.utils.Window

class FrameRepresentor {
    fun represent(frame: MutableList<Double>): MutableList<Double> {
        Window.hamming(frame)
        val R = Relation.autoCorrelation(frame, SpeechConstants.P_VALUE)
        val A = Matrix.durbinSolve(R)
        val C = Relation.cepstralCoefficients(A)
        Window.sine(C)
        return C
    }
}