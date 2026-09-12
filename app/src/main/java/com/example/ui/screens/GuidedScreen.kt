package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ScanEvent
import com.example.data.model.FindingEntity
import com.example.data.model.ScopeEntity
import com.example.ui.theme.HydraAmber
import com.example.ui.theme.HydraAndroidGreen
import com.example.ui.theme.HydraBgDark
import com.example.ui.theme.HydraBorder
import com.example.ui.theme.HydraBorderActive
import com.example.ui.theme.HydraCyan
import com.example.ui.theme.HydraCyanMuted
import com.example.ui.theme.HydraPurple
import com.example.ui.theme.HydraRed
import com.example.ui.theme.HydraSurface
import com.example.ui.theme.HydraSurfaceVariant
import com.example.ui.theme.HydraTextMuted
import com.example.ui.theme.HydraTextPrimary
import com.example.ui.theme.HydraTextSecondary
import com.example.ui.theme.HydraViolet

data class ModuleInfo(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val primaryColor: Color
)

@Composable
fun GuidedScreen(
    activeScope: ScopeEntity?,
    isScanning: Boolean,
    scanProgress: Float,
    scanStage: String,
    scanLogs: List<ScanEvent.Log>,
    findings: List<FindingEntity>,
    scanError: String?,
    onOpenScopeDialog: () -> Unit,
    onRunScan: (moduleId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val modules = listOf(
        ModuleInfo(
            id = "com.hydranet.wifi_audit",
            title = "Wi-Fi Security Audit",
            category = "Wireless",
            description = "Audit WPS 2.0 exposure, PMKID beacon capture, WPA2/WPA3 handshake & 802.11w PMF protection.",
            icon = Icons.Default.Wifi,
            primaryColor = HydraCyan
        ),
        ModuleInfo(
            id = "com.hydranet.ble_scan",
            title = "Bluetooth LE Exposure",
            category = "Wireless",
            description = "Discover unencrypted GATT services, pairing PIN brute-force surfaces, and tracker beacon exposure.",
            icon = Icons.Default.BluetoothSearching,
            primaryColor = HydraPurple
        ),
        ModuleInfo(
            id = "com.hydranet.device_hardening",
            title = "Device Hardening Check",
            category = "Defense",
            description = "Audit local Android OS parameters, root binaries, SELinux enforcement, and developer debug props.",
            icon = Icons.Default.Shield,
            primaryColor = HydraAndroidGreen
        ),
        ModuleInfo(
            id = "com.hydranet.rogue_ap",
            title = "Rogue AP / Evil Twin Shield",
            category = "Defense",
            description = "Monitor live 802.11 beacons for evil-twin clones, rogue BSSIDs, and sudden signal amplitude anomalies.",
            icon = Icons.Default.WifiTethering,
            primaryColor = HydraAmber
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HydraBgDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HydraNet Brand Header with Logo
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = HydraSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HydraBorder, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(HydraPurple, HydraViolet, HydraCyan)
                                )
                            )
                            .padding(2.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hydranet_logo),
                            contentDescription = "HydraNet Android Edition Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "HYDRANET",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.2.sp,
                                color = HydraCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HydraPurple.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "v0.1-alpha",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HydraPurple
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Mobile Offensive & Defensive Security Engine",
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = HydraTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Scope-Locked • SHA-256 Chained • Legal Refusal",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = HydraAndroidGreen
                        )
                    }
                }
            }
        }

        // Scope Definition Hero Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = HydraSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (activeScope != null && activeScope.authorized) HydraBorderActive else HydraAmber,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (activeScope != null && activeScope.authorized) HydraAndroidGreen else HydraAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DECLARED SCOPE",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp,
                                color = HydraTextPrimary
                            )
                        }

                        Button(
                            onClick = onOpenScopeDialog,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeScope != null) HydraSurfaceVariant else HydraCyan
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_configure_scope")
                        ) {
                            Text(
                                text = if (activeScope != null) "Edit Scope" else "Define Scope",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeScope != null) HydraCyan else HydraBgDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (activeScope != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Target SSID",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = HydraTextMuted
                                )
                                Text(
                                    text = activeScope.ssid,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HydraCyan
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Subnet Range",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = HydraTextMuted
                                )
                                Text(
                                    text = activeScope.ipRanges.ifEmpty { "Local" },
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = HydraTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (activeScope.authorized) HydraAndroidGreen else HydraRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (activeScope.authorized) "Authorized for Penetration Testing (Refusal Lock Permissive)" else "Authorization Not Verified (Refusal Active)",
                                fontSize = 11.sp,
                                color = if (activeScope.authorized) HydraAndroidGreen else HydraRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Text(
                            text = "No active scope declared. Define your target network and confirm ownership or written authorization before testing.",
                            fontSize = 12.sp,
                            color = HydraAmber,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Active Scan Progress / Stream Box
        if (isScanning || scanError != null || scanLogs.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = HydraSurfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (scanError != null) HydraRed else HydraCyan,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isScanning) {
                                    val transition = rememberInfiniteTransition(label = "pulse")
                                    val scale by transition.animateFloat(
                                        initialValue = 0.85f,
                                        targetValue = 1.15f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(800),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "scale"
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Radar,
                                        contentDescription = null,
                                        tint = HydraCyan,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .scale(scale)
                                    )
                                } else if (scanError != null) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = HydraRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = HydraAndroidGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isScanning) "SCAN IN PROGRESS" else if (scanError != null) "SCAN REFUSED" else "SCAN COMPLETED",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (scanError != null) HydraRed else HydraCyan
                                )
                            }

                            Text(
                                text = "${(scanProgress * 100).toInt()}%",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = HydraTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { scanProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (scanError != null) HydraRed else HydraCyan,
                            trackColor = HydraBgDark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (scanError != null) scanError else scanStage,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (scanError != null) HydraRed else HydraTextSecondary
                        )

                        // Live log preview
                        if (scanLogs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HydraBgDark)
                                    .padding(8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    scanLogs.takeLast(3).forEach { log ->
                                        Text(
                                            text = "[${log.timestamp}] [${log.level}] ${log.message}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = when (log.level) {
                                                "ERROR" -> HydraRed
                                                "WARN" -> HydraAmber
                                                else -> HydraAndroidGreen
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Security Modules
        item {
            Text(
                text = "SECURITY ASSESSMENT MODULES",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = HydraCyanMuted
            )
        }

        // 4 Core Modules
        items(modules) { module ->
            ModuleCard(
                module = module,
                isScanning = isScanning,
                onRun = { onRunScan(module.id) }
            )
        }

        // Findings Section Header
        if (findings.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VULNERABILITY FINDINGS (${findings.size})",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = HydraPurple
                    )
                    Text(
                        text = "MITRE ATT&CK MAPPED",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = HydraTextMuted
                    )
                }
            }

            items(findings) { finding ->
                FindingCard(finding = finding)
            }
        }
    }
}

@Composable
fun ModuleCard(
    module: ModuleInfo,
    isScanning: Boolean,
    onRun: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = HydraSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HydraBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(module.primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = module.icon,
                            contentDescription = null,
                            tint = module.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = module.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = HydraTextPrimary
                        )
                        Text(
                            text = module.category.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = module.primaryColor
                        )
                    }
                }

                Button(
                    onClick = onRun,
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = module.primaryColor,
                        disabledContainerColor = HydraSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("btn_run_${module.id}")
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
                            text = "Launch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = HydraBgDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = module.description,
                fontSize = 12.sp,
                color = HydraTextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun FindingCard(finding: FindingEntity) {
    var expanded by remember { mutableStateOf(false) }

    val severityColor = when (finding.severity.uppercase()) {
        "CRITICAL" -> HydraRed
        "HIGH" -> HydraRed.copy(alpha = 0.85f)
        "MEDIUM" -> HydraAmber
        "LOW" -> HydraCyan
        else -> HydraAndroidGreen
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = HydraSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, severityColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(severityColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = finding.severity.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = severityColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (finding.mitreAttack.isNotEmpty()) {
                        Text(
                            text = "MITRE ${finding.mitreAttack}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = HydraPurple
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = HydraTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = finding.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = HydraTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Plain-English Explanation (always visible summary)
            Text(
                text = finding.plainEnglish,
                fontSize = 12.sp,
                color = HydraTextSecondary,
                lineHeight = 16.sp
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = HydraBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "TECHNICAL ANALYSIS:",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = HydraCyan
                    )
                    Text(
                        text = finding.technical,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = HydraTextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(HydraBgDark, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "REMEDIATION RECOMMENDATION:",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = HydraAndroidGreen
                    )
                    Text(
                        text = finding.remediation,
                        fontSize = 12.sp,
                        color = HydraTextSecondary,
                        modifier = Modifier.padding(top = 2.dp),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
