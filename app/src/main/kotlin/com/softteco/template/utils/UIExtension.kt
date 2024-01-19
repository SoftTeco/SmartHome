package com.softteco.template.utils

import android.graphics.Color
import com.softteco.template.Constants
import java.util.Random

fun generateRandomColor(): Int {
    return Random().let {
        Color.argb(
            Constants.ALPHA_COLOR_VALUE,
            it.nextInt(Constants.BOUND_COLOR_VALUE),
            it.nextInt(Constants.BOUND_COLOR_VALUE),
            it.nextInt(Constants.BOUND_COLOR_VALUE)
        )
    }
}
