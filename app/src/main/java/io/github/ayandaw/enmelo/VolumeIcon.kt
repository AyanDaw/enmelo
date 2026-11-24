package io.github.ayandaw.enmelo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun VolumeIcon(percent: Int, modifier: Modifier = Modifier.size(24.dp), color: Color = Color.White) {
    val arcs = when {
        percent <= 0 -> 0
        percent < 34 -> 1
        percent < 67 -> 2
        else -> 3
    }
    Canvas(modifier) {
        val s = size.width / 24f
        // the outline adds 2 units on every side, so the points sit 2 units inside the shape we want
        val bodyW = 2f * s
        val body = Path().apply {
            moveTo(5f * s, 11f * s); lineTo(7.6f * s, 11f * s); lineTo(10f * s, 7.5f * s)
            lineTo(10f * s, 16.5f * s); lineTo(7.6f * s, 13f * s); lineTo(5f * s, 13f * s); close()
        }
        drawPath(body, color)
        drawPath(body, color, style = Stroke(bodyW, cap = StrokeCap.Round, join = StrokeJoin.Round))
        val w = 1.8f * s
        if (arcs == 0) {
            drawLine(color, Offset(15.5f * s, 9.5f * s), Offset(20.5f * s, 14.5f * s), w, StrokeCap.Round)
            drawLine(color, Offset(20.5f * s, 9.5f * s), Offset(15.5f * s, 14.5f * s), w, StrokeCap.Round)
        }
        for (i in 1..arcs) {
            val r = (1 + i * 3f) * s
            drawArc(
                color, startAngle = -45f, sweepAngle = 90f, useCenter = false,
                topLeft = Offset(12f * s - r, 12f * s - r), size = Size(2 * r, 2 * r),
                style = Stroke(w, cap = StrokeCap.Round)
            )
        }
    }
}