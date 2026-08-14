package com.mi10tpro.aicamera

object AiPipeline {
    enum class Mode { NATURAL, MAX_DETAIL, NIGHT }
    fun frameCount(zoom: Float, mode: Mode) = when (mode) {
        Mode.NIGHT -> if (zoom >= 8f) 12 else 8
        Mode.MAX_DETAIL -> if (zoom >= 8f) 12 else 8
        Mode.NATURAL -> if (zoom >= 8f) 8 else 6
    }
    fun srScale(zoom: Float) = if (zoom >= 8f) 4 else 2
}
