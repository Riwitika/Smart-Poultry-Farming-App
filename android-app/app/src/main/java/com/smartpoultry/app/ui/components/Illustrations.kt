package com.smartpoultry.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun MascotIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(120.dp)) {
        val w = size.width
        val h = size.height

        // Outer glow circle
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF34D399).copy(alpha = 0.15f), Color.Transparent),
                center = Offset(w / 2, h / 2),
                radius = w / 2
            )
        )

        // Chicken Body (Soft round shape)
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFF1F5F9), Color(0xFFCBD5E1))
            ),
            center = Offset(w / 2, h * 0.55f),
            radius = w * 0.32f
        )

        // Chicken Wing
        val wingPath = Path().apply {
            moveTo(w * 0.3f, h * 0.52f)
            quadraticBezierTo(w * 0.15f, h * 0.6f, w * 0.3f, h * 0.72f)
            quadraticBezierTo(w * 0.45f, h * 0.68f, w * 0.38f, h * 0.55f)
            close()
        }
        drawPath(
            path = wingPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))
            )
        )

        // Comb (Red crown)
        drawCircle(color = Color(0xFFEF4444), center = Offset(w * 0.42f, h * 0.22f), radius = w * 0.08f)
        drawCircle(color = Color(0xFFEF4444), center = Offset(w * 0.5f, h * 0.19f), radius = w * 0.09f)
        drawCircle(color = Color(0xFFEF4444), center = Offset(w * 0.58f, h * 0.22f), radius = w * 0.08f)

        // Beak (Yellow)
        val beakPath = Path().apply {
            moveTo(w * 0.55f, h * 0.42f)
            lineTo(w * 0.68f, h * 0.46f)
            lineTo(w * 0.55f, h * 0.5f)
            close()
        }
        drawPath(path = beakPath, color = Color(0xFFF59E0B))

        // Eyes (Futuristic black with white reflection)
        drawCircle(color = Color(0xFF1E293B), center = Offset(w * 0.46f, h * 0.4f), radius = w * 0.045f)
        drawCircle(color = Color.White, center = Offset(w * 0.45f, h * 0.38f), radius = w * 0.015f)

        // Cheeks (Blushing pink)
        drawCircle(color = Color(0xFFFDA4AF).copy(alpha = 0.7f), center = Offset(w * 0.38f, h * 0.48f), radius = w * 0.04f)

        // Wattle (Red under beak)
        drawCircle(color = Color(0xFFEF4444), center = Offset(w * 0.56f, h * 0.53f), radius = w * 0.04f)
    }
}

@Composable
fun MicroscopeIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(130.dp)) {
        val w = size.width
        val h = size.height

        // Background tech hex/circle details
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF60A5FA).copy(alpha = 0.12f), Color.Transparent),
                center = Offset(w / 2, h / 2),
                radius = w * 0.45f
            )
        )

        // Microscope base
        drawRoundRect(
            color = Color(0xFF475569),
            topLeft = Offset(w * 0.25f, h * 0.78f),
            size = Size(w * 0.5f, h * 0.08f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Arm
        val armPath = Path().apply {
            moveTo(w * 0.65f, h * 0.78f)
            cubicTo(w * 0.75f, h * 0.65f, w * 0.75f, h * 0.45f, w * 0.6f, h * 0.38f)
            lineTo(w * 0.54f, h * 0.44f)
            cubicTo(w * 0.65f, h * 0.5f, w * 0.65f, h * 0.62f, w * 0.58f, h * 0.78f)
            close()
        }
        drawPath(path = armPath, color = Color(0xFF94A3B8))

        // Stage plate
        drawRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(w * 0.2f, h * 0.62f),
            size = Size(w * 0.42f, h * 0.03f)
        )

        // Body tube & eyepiece
        val tubePath = Path().apply {
            moveTo(w * 0.42f, h * 0.32f)
            lineTo(w * 0.54f, h * 0.48f)
            lineTo(w * 0.48f, h * 0.52f)
            lineTo(w * 0.36f, h * 0.36f)
            close()
        }
        drawPath(path = tubePath, color = Color(0xFF64748B))

        // Objective lens pointing down
        drawRect(
            color = Color(0xFF34D399),
            topLeft = Offset(w * 0.38f, h * 0.52f),
            size = Size(w * 0.06f, h * 0.08f)
        )

        // AI Shield element (floating beside)
        drawCircle(
            color = Color(0xFF34D399).copy(alpha = 0.2f),
            center = Offset(w * 0.75f, h * 0.35f),
            radius = w * 0.12f
        )
        drawCircle(
            color = Color(0xFF34D399),
            center = Offset(w * 0.75f, h * 0.35f),
            radius = w * 0.03f
        )
    }
}

@Composable
fun ClipboardIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(120.dp)) {
        val w = size.width
        val h = size.height

        // Outer radial glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF60A5FA).copy(alpha = 0.12f), Color.Transparent),
                center = Offset(w / 2, h / 2),
                radius = w * 0.45f
            )
        )

        // Board back
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
            ),
            topLeft = Offset(w * 0.28f, h * 0.22f),
            size = Size(w * 0.44f, h * 0.62f),
            cornerRadius = CornerRadius(16f, 16f)
        )

        // Board clip
        drawRoundRect(
            color = Color(0xFF64748B),
            topLeft = Offset(w * 0.42f, h * 0.18f),
            size = Size(w * 0.16f, h * 0.08f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Paper
        drawRoundRect(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset(w * 0.33f, h * 0.28f),
            size = Size(w * 0.34f, h * 0.5f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Paper Lines (diagnostic checklist details)
        drawLine(Color(0xFFCBD5E1), Offset(w * 0.38f, h * 0.38f), Offset(w * 0.62f, h * 0.38f), strokeWidth = 3f)
        drawLine(Color(0xFFCBD5E1), Offset(w * 0.38f, h * 0.46f), Offset(w * 0.58f, h * 0.46f), strokeWidth = 3f)
        drawLine(Color(0xFFCBD5E1), Offset(w * 0.38f, h * 0.54f), Offset(w * 0.62f, h * 0.54f), strokeWidth = 3f)

        // Tiny green AI checkmark
        val checkPath = Path().apply {
            moveTo(w * 0.38f, h * 0.66f)
            lineTo(w * 0.42f, h * 0.7f)
            lineTo(w * 0.52f, h * 0.6f)
        }
        drawPath(
            path = checkPath,
            color = Color(0xFF22C55E),
            style = Stroke(width = 4f)
        )
    }
}

@Composable
fun FarmerIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(120.dp)) {
        val w = size.width
        val h = size.height

        // Sunset radial background
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF34D399).copy(alpha = 0.15f), Color.Transparent),
                center = Offset(w / 2, h / 2),
                radius = w * 0.45f
            )
        )

        // Farm Field line details
        val fieldPath = Path().apply {
            moveTo(w * 0.15f, h * 0.72f)
            quadraticBezierTo(w * 0.5f, h * 0.65f, w * 0.85f, h * 0.72f)
            lineTo(w * 0.85f, h * 0.85f)
            lineTo(w * 0.15f, h * 0.85f)
            close()
        }
        drawPath(
            path = fieldPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF047857), Color(0xFF064E3B))
            )
        )

        // Farmer Body Silhouette (Modern, flat clean avatar shape)
        drawCircle(
            color = Color(0xFF334155),
            center = Offset(w / 2, h * 0.44f),
            radius = w * 0.12f
        )
        val chestPath = Path().apply {
            moveTo(w * 0.35f, h * 0.7f)
            quadraticBezierTo(w * 0.35f, h * 0.56f, w * 0.5f, h * 0.56f)
            quadraticBezierTo(w * 0.65f, h * 0.56f, w * 0.65f, h * 0.7f)
            close()
        }
        drawPath(path = chestPath, color = Color(0xFF1E293B))

        // Futuristic straw hat brim
        val hatBrim = Path().apply {
            moveTo(w * 0.28f, h * 0.45f)
            quadraticBezierTo(w * 0.5f, h * 0.38f, w * 0.72f, h * 0.45f)
            quadraticBezierTo(w * 0.5f, h * 0.42f, w * 0.28f, h * 0.45f)
            close()
        }
        drawPath(path = hatBrim, color = Color(0xFFD97706))

        // Hat crown
        val hatCrown = Path().apply {
            moveTo(w * 0.4f, h * 0.4f)
            quadraticBezierTo(w * 0.5f, h * 0.26f, w * 0.6f, h * 0.4f)
            close()
        }
        drawPath(path = hatCrown, color = Color(0xFFB45309))
    }
}
