package com.unirfp.ceropapeleo.forms.components

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.drawScrollbar(state: ScrollState): Modifier = drawWithContent {
    drawContent()

    if (state.maxValue > 0) {
        val scrollValue = state.value.toFloat()
        val maxScrollValue = state.maxValue.toFloat()
        val viewportHeight = size.height

        val scrollbarHeight =
            (viewportHeight / (maxScrollValue + viewportHeight)) * viewportHeight

        val scrollbarTop =
            (scrollValue / (maxScrollValue + viewportHeight)) * viewportHeight

        drawRect(
            color = Color.Gray.copy(alpha = 0.6f),
            topLeft = Offset(size.width - 6.dp.toPx(), scrollbarTop),
            size = Size(4.dp.toPx(), scrollbarHeight)
        )
    }
}