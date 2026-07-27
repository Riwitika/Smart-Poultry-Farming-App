package com.smartpoultry.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PieChart(
    data: Map<String, Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum()
    if (total == 0f) {
        Box(
            modifier = modifier.fillMaxWidth().height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No scan data available for analysis",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
        }
        return
    }

    val keys = data.keys.toList()
    val values = data.values.toList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = modifier.size(140.dp)) {
            var startAngle = -90f
            for (i in values.indices) {
                val sweepAngle = (values[i] / total) * 360f
                drawArc(
                    color = colors.getOrElse(i) { Color.Gray },
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )
                startAngle += sweepAngle
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        ) {
            for (i in keys.indices) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colors.getOrElse(i) { Color.Gray })
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${keys[i]} (${String.format("%.1f", (values[i] / total) * 100f)}%)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Float>>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty() || data.all { it.second == 0f }) {
        Box(
            modifier = modifier.fillMaxWidth().height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No scans completed in this period",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
        }
        return
    }

    val maxVal = data.maxOf { it.second }
    val yMax = if (maxVal == 0f) 10f else maxVal * 1.2f // Add top padding

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val width = size.width
        val height = size.height
        val barCount = data.size
        
        // Draw baseline
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, height - 30f),
            end = Offset(width, height - 30f),
            strokeWidth = 2f
        )

        val spacePerBar = width / barCount
        val barWidth = spacePerBar * 0.6f

        for (i in 0 until barCount) {
            val (label, value) = data[i]
            val x = (i * spacePerBar) + (spacePerBar - barWidth) / 2f
            
            // Calculate height of bar relative to max y
            val barHeight = (value / yMax) * (height - 60f)
            val y = height - 30f - barHeight

            // Draw bar rect
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )

            // Draw value label above bar
            if (value > 0f) {
                // Drawing text is easiest via Native Canvas in compose
                drawContext.canvas.nativeCanvas.drawText(
                    value.toInt().toString(),
                    x + barWidth / 2f,
                    y - 8f,
                    android.graphics.Paint().apply {
                        this.setColor(android.graphics.Color.DKGRAY)
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    }
                )
            }

            // Draw baseline label below bar
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x + barWidth / 2f,
                height - 6f,
                android.graphics.Paint().apply {
                    this.setColor(android.graphics.Color.GRAY)
                    textSize = 26f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun LineChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Scan trend empty",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
        }
        return
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val width = size.width
        val height = size.height - 40f
        val pointsCount = data.size

        // Draw baseline (0%) and top line (100%)
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(0f, 0f),
            end = Offset(width, 0f),
            strokeWidth = 1f
        )
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 2f
        )

        // Draw dashed grid lines (50%)
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(0f, height / 2f),
            end = Offset(width, height / 2f),
            strokeWidth = 1f
        )

        val stepX = width / if (pointsCount > 1) (pointsCount - 1) else 1
        val path = Path()

        for (i in 0 until pointsCount) {
            // confidence value (e.g. 80.95). Invert because y goes down.
            val confidenceVal = data[i]
            val x = i * stepX
            val y = height - (confidenceVal / 100f) * height

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Draw point circle
            drawCircle(
                color = color,
                radius = 6f,
                center = Offset(x, y)
            )
        }

        // Draw line connection
        if (pointsCount > 1) {
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 4f)
            )
        }
    }
}
