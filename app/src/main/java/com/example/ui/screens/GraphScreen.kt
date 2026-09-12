package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GraphNode
import com.example.ui.theme.HydraAmber
import com.example.ui.theme.HydraAndroidGreen
import com.example.ui.theme.HydraBgDark
import com.example.ui.theme.HydraBorder
import com.example.ui.theme.HydraCyan
import com.example.ui.theme.HydraPurple
import com.example.ui.theme.HydraRed
import com.example.ui.theme.HydraSurface
import com.example.ui.theme.HydraSurfaceVariant
import com.example.ui.theme.HydraTextMuted
import com.example.ui.theme.HydraTextPrimary
import com.example.ui.theme.HydraTextSecondary

@Composable
fun GraphScreen(
    nodes: List<GraphNode>,
    selectedNode: GraphNode?,
    onSelectNode: (GraphNode?) -> Unit,
    onAuditTarget: (target: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HydraBgDark)
            .padding(12.dp)
    ) {
        // Topology Header & Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WIRELESS TOPOLOGY MESH",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = HydraCyan
            )

            // Legend indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LegendItem(color = HydraAndroidGreen, label = "Safe")
                LegendItem(color = HydraAmber, label = "Risk")
                LegendItem(color = HydraRed, label = "Threat")
            }
        }

        // Radar Canvas with interactive nodes
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HydraSurface)
                .border(1.dp, HydraBorder, RoundedCornerShape(12.dp))
        ) {
            val canvasWidth = constraints.maxWidth.toFloat()
            val canvasHeight = constraints.maxHeight.toFloat()
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight * 0.45f

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(nodes) {
                        detectTapGestures { offset ->
                            // Find nearest node within hit radius
                            val hitNode = nodes.find { node ->
                                val nx = node.x * canvasWidth
                                val ny = node.y * canvasHeight
                                val dist = Math.hypot((offset.x - nx).toDouble(), (offset.y - ny).toDouble())
                                dist <= 40.0
                            }
                            onSelectNode(hitNode)
                        }
                    }
            ) {
                // Background radar rings
                val maxRadius = (canvasWidth.coerceAtMost(canvasHeight) * 0.42f)
                for (r in listOf(0.25f, 0.5f, 0.75f, 1.0f)) {
                    drawCircle(
                        color = HydraCyan.copy(alpha = 0.12f),
                        radius = maxRadius * r,
                        center = Offset(centerX, centerY),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    )
                }

                // Crosshairs
                drawLine(
                    color = HydraCyan.copy(alpha = 0.1f),
                    start = Offset(centerX - maxRadius, centerY),
                    end = Offset(centerX + maxRadius, centerY),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = HydraCyan.copy(alpha = 0.1f),
                    start = Offset(centerX, centerY - maxRadius),
                    end = Offset(centerX, centerY + maxRadius),
                    strokeWidth = 1.dp.toPx()
                )

                // Connection Lines from Gateway to other nodes
                val gateway = nodes.find { it.type == "GATEWAY" }
                if (gateway != null) {
                    val gx = gateway.x * canvasWidth
                    val gy = gateway.y * canvasHeight

                    nodes.forEach { node ->
                        if (node.id != gateway.id) {
                            val nx = node.x * canvasWidth
                            val ny = node.y * canvasHeight
                            val lineColor = when (node.severity) {
                                "CRITICAL", "HIGH" -> HydraRed.copy(alpha = 0.4f)
                                "MEDIUM" -> HydraAmber.copy(alpha = 0.4f)
                                else -> HydraCyan.copy(alpha = 0.25f)
                            }
                            drawLine(
                                color = lineColor,
                                start = Offset(gx, gy),
                                end = Offset(nx, ny),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                        }
                    }
                }

                // Draw Nodes on Canvas
                nodes.forEach { node ->
                    val nx = node.x * canvasWidth
                    val ny = node.y * canvasHeight

                    val nodeColor = when (node.severity) {
                        "CRITICAL", "HIGH" -> HydraRed
                        "MEDIUM" -> HydraAmber
                        else -> HydraAndroidGreen
                    }

                    val isSelected = selectedNode?.id == node.id

                    // Halo if selected
                    if (isSelected) {
                        drawCircle(
                            color = nodeColor.copy(alpha = 0.35f),
                            radius = 24.dp.toPx(),
                            center = Offset(nx, ny)
                        )
                    }

                    // Outer node circle
                    drawCircle(
                        color = nodeColor,
                        radius = if (node.type == "GATEWAY") 14.dp.toPx() else 10.dp.toPx(),
                        center = Offset(nx, ny)
                    )

                    // Inner dot
                    drawCircle(
                        color = HydraBgDark,
                        radius = if (node.type == "GATEWAY") 6.dp.toPx() else 4.dp.toPx(),
                        center = Offset(nx, ny)
                    )
                }
            }

            // Interactive Overlaid Node Labels
            nodes.forEach { node ->
                val nx = (node.x * canvasWidth).toInt()
                val ny = (node.y * canvasHeight).toInt()

                Box(
                    modifier = Modifier
                        .padding(
                            start = (nx / density).dp - 30.dp,
                            top = (ny / density).dp + 16.dp
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .background(HydraBgDark.copy(alpha = 0.85f))
                        .border(
                            1.dp,
                            if (selectedNode?.id == node.id) HydraCyan else HydraBorder,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onSelectNode(node) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = node.label.take(16),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = if (selectedNode?.id == node.id) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedNode?.id == node.id) HydraCyan else HydraTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Node Inspection Card
        if (selectedNode != null) {
            val node = selectedNode
            val sevColor = when (node.severity) {
                "CRITICAL", "HIGH" -> HydraRed
                "MEDIUM" -> HydraAmber
                else -> HydraAndroidGreen
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = HydraSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, sevColor, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = node.label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = HydraTextPrimary
                            )
                            Text(
                                text = "TYPE: ${node.type} | RSSI: ${node.rssi} dBm",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = sevColor
                            )
                        }

                        IconButton(
                            onClick = { onSelectNode(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = HydraTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "MAC: ${node.mac} ${if (node.ip.isNotEmpty()) " | IP: ${node.ip}" else ""}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = HydraCyan
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = node.details,
                        fontSize = 11.sp,
                        color = HydraTextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onAuditTarget(node.label) },
                            colors = ButtonDefaults.buttonColors(containerColor = HydraCyan),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_audit_selected_node")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = HydraBgDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Audit Target",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HydraBgDark
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = HydraSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HydraBorder, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = HydraCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tap on any node to inspect MAC parameters, RSSI signal, and security posture.",
                        fontSize = 11.sp,
                        color = HydraTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = HydraTextMuted
        )
    }
}
