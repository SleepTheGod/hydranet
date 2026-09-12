package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AuditEntryEntity
import com.example.data.model.ScopeEntity
import com.example.data.model.VaultItemEntity
import com.example.engine.ChainVerificationResult
import com.example.ui.DoctorCheck
import com.example.ui.theme.HydraAmber
import com.example.ui.theme.HydraAndroidGreen
import com.example.ui.theme.HydraBgDark
import com.example.ui.theme.HydraBorder
import com.example.ui.theme.HydraBorderActive
import com.example.ui.theme.HydraCyan
import com.example.ui.theme.HydraPurple
import com.example.ui.theme.HydraRed
import com.example.ui.theme.HydraSurface
import com.example.ui.theme.HydraSurfaceVariant
import com.example.ui.theme.HydraTextMuted
import com.example.ui.theme.HydraTextPrimary
import com.example.ui.theme.HydraTextSecondary

@Composable
fun ScopeDialog(
    currentScope: ScopeEntity?,
    onDismiss: () -> Unit,
    onSave: (ssid: String, bssids: String, ipRanges: String, authorized: Boolean) -> Unit,
    onLearnMore: () -> Unit
) {
    var ssid by remember { mutableStateOf(currentScope?.ssid ?: "LabNetwork_Secure") }
    var bssids by remember { mutableStateOf(currentScope?.bssids ?: "DE:AD:BE:EF:01:23") }
    var ipRanges by remember { mutableStateOf(currentScope?.ipRanges ?: "192.168.1.0/24") }
    var authorized by remember { mutableStateOf(currentScope?.authorized ?: false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HydraSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = HydraCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.define_scope),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = HydraTextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "HydraNet enforces strict cryptographic boundaries. The built-in Refusal Engine blocks all scan actions outside this scope.",
                    fontSize = 13.sp,
                    color = HydraTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = ssid,
                    onValueChange = { ssid = it },
                    label = { Text("Network SSID (e.g. Corp_WiFi)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_ssid"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HydraCyan,
                        unfocusedBorderColor = HydraBorder,
                        focusedTextColor = HydraTextPrimary,
                        unfocusedTextColor = HydraTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bssids,
                    onValueChange = { bssids = it },
                    label = { Text("Target BSSIDs (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_bssids"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HydraPurple,
                        unfocusedBorderColor = HydraBorder,
                        focusedTextColor = HydraTextPrimary,
                        unfocusedTextColor = HydraTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = ipRanges,
                    onValueChange = { ipRanges = it },
                    label = { Text("Target Subnet / CIDR (e.g. 192.168.1.0/24)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_ip_ranges"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HydraCyan,
                        unfocusedBorderColor = HydraBorder,
                        focusedTextColor = HydraTextPrimary,
                        unfocusedTextColor = HydraTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mandatory Authorization Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (authorized) HydraAndroidGreen.copy(alpha = 0.1f) else HydraSurfaceVariant)
                        .border(
                            1.dp,
                            if (authorized) HydraAndroidGreen else HydraBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { authorized = !authorized }
                        .padding(8.dp)
                ) {
                    Checkbox(
                        checked = authorized,
                        onCheckedChange = { authorized = it },
                        modifier = Modifier.testTag("checkbox_authorization"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = HydraAndroidGreen,
                            checkmarkColor = HydraBgDark,
                            uncheckedColor = HydraTextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.auth_checkbox),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (authorized) HydraTextPrimary else HydraTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onLearnMore,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = stringResource(R.string.learn_authorized),
                        fontSize = 11.sp,
                        color = HydraCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = HydraRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ssid.isBlank()) {
                        errorText = "SSID is required."
                    } else if (!authorized) {
                        errorText = "You must confirm written authorization to proceed."
                    } else {
                        onSave(ssid, bssids, ipRanges, authorized)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HydraCyan),
                modifier = Modifier.testTag("btn_save_scope")
            ) {
                Text(
                    text = stringResource(R.string.continue_btn),
                    color = HydraBgDark,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel_btn),
                    color = HydraTextMuted
                )
            }
        }
    )
}

@Composable
fun ConsentInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HydraSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = HydraCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Authorization & Legal Scope",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = HydraTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                LazyColumn {
                    item {
                        Text(
                            text = "What does 'Authorized' mean?",
                            fontWeight = FontWeight.Bold,
                            color = HydraCyan,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Security testing against computing assets or networks without unambiguous, prior written authorization from the system owner is a criminal offense under the Computer Fraud and Abuse Act (CFAA 18 U.S.C. 1030 in the US), the Computer Misuse Act 1990 (UK), and equivalent legislation worldwide.",
                            fontSize = 12.sp,
                            color = HydraTextSecondary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "HydraNet Safety Principles",
                            fontWeight = FontWeight.Bold,
                            color = HydraPurple,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Scope Boundary Lock: Scanning any target outside declared networks is automatically refused by the kernel/daemon refusal engine.\n" +
                                    "• Non-Repudiation: Every action is sealed in a tamper-evident SHA-256 hash chain.\n" +
                                    "• Hard Blocklist: Government (.gov) and military (.mil) TLDs, as well as critical infrastructure subnets, are permanently denied by the firmware refusal engine.",
                            fontSize = 12.sp,
                            color = HydraTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = HydraCyan)
            ) {
                Text(
                    text = "I Understand",
                    color = HydraBgDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun PanicWipeDialog(
    onDismiss: () -> Unit,
    onConfirmWipe: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HydraSurface,
        icon = {
            Icon(
                imageVector = Icons.Default.Dangerous,
                contentDescription = null,
                tint = HydraRed,
                modifier = Modifier.size(44.dp)
            )
        },
        title = {
            Text(
                text = "EMERGENCY DATA PURGE",
                fontWeight = FontWeight.Black,
                color = HydraRed,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column {
                Text(
                    text = "This will irreversibly overwrite and destroy all local data:",
                    color = HydraTextPrimary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Cryptographic Audit Log & SHA-256 Hashes\n" +
                            "• Encrypted Loot Vault & Capture Records\n" +
                            "• Active Scopes & Findings Database\n" +
                            "• Temporary Packet Dumps & Memory Caches",
                    color = HydraTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Zero-overwrite will be performed immediately.",
                    color = HydraAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmWipe,
                colors = ButtonDefaults.buttonColors(containerColor = HydraRed),
                modifier = Modifier.testTag("btn_confirm_panic_wipe")
            ) {
                Text("PURGE ALL DATA", color = Color.White, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = HydraTextMuted)
            }
        }
    )
}

@Composable
fun AuditLogDialog(
    auditEntries: List<AuditEntryEntity>,
    verification: ChainVerificationResult?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HydraSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = HydraPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Audit Hash-Chain",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HydraTextPrimary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                // Verification status banner
                if (verification != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (verification.isValid) HydraAndroidGreen.copy(alpha = 0.15f) else HydraRed.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (verification.isValid) HydraAndroidGreen else HydraRed,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (verification.isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (verification.isValid) HydraAndroidGreen else HydraRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (verification.isValid) "INTEGRITY VERIFIED" else "TAMPER DETECTED",
                                    fontWeight = FontWeight.Bold,
                                    color = if (verification.isValid) HydraAndroidGreen else HydraRed,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = verification.message,
                                fontSize = 11.sp,
                                color = HydraTextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            if (verification.merkleRoot.isNotEmpty()) {
                                Text(
                                    text = "Merkle Root: ${verification.merkleRoot.take(16)}...${verification.merkleRoot.takeLast(8)}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = HydraCyan,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (auditEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No audit log entries recorded yet.",
                            color = HydraTextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(auditEntries.reversed()) { entry ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HydraBgDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, HydraBorder, RoundedCornerShape(8.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = entry.action,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = HydraCyan,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = entry.timestamp.takeLast(9).replace("Z", ""),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = HydraTextMuted
                                        )
                                    }
                                    Text(
                                        text = "Target: ${entry.target.ifEmpty { "System" }} | Result: ${entry.result}",
                                        fontSize = 11.sp,
                                        color = HydraTextPrimary,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                    Text(
                                        text = "Hash: ${entry.entryHash.take(12)}...${entry.entryHash.takeLast(8)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = HydraPurple
                                    )
                                    if (entry.prevHash.isNotEmpty()) {
                                        Text(
                                            text = "Prev: ${entry.prevHash.take(12)}...${entry.prevHash.takeLast(8)}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = HydraTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = HydraPurple)
            ) {
                Text("Close", color = Color.White)
            }
        }
    )
}

@Composable
fun VaultDialog(
    vaultItems: List<VaultItemEntity>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HydraSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = HydraAndroidGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Encrypted Loot Vault",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = HydraTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                Text(
                    text = "Artifacts encrypted at rest using ChaCha20-Poly1305 / AES keys.",
                    fontSize = 12.sp,
                    color = HydraTextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                if (vaultItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loot vault is currently empty.\nCompleted scan reports and captures are saved here.",
                            color = HydraTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(vaultItems) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HydraBgDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, HydraBorder, RoundedCornerShape(8.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.Bold,
                                            color = HydraAndroidGreen,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = item.category,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = HydraCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "SHA256: ${item.sha256Checksum.take(16)}...${item.sha256Checksum.takeLast(8)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = HydraTextMuted
                                    )
                                    Text(
                                        text = "Tags: ${item.tags.ifEmpty { "general" }}",
                                        fontSize = 10.sp,
                                        color = HydraTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = HydraAndroidGreen)
            ) {
                Text("Close", color = HydraBgDark, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun DoctorDialog(
    checks: List<DoctorCheck>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HydraSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.linearGradient(listOf(HydraPurple, HydraCyan)))
                        .padding(1.5.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.hydranet_logo),
                        contentDescription = "HydraNet Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(7.dp))
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "HydraNet Diagnostics",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = HydraTextPrimary
                    )
                    Text(
                        text = "System Doctor & Integrity",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = HydraCyan
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Text(
                    text = "Self-check of daemon integrity, system parameters, and isolation layers:",
                    fontSize = 12.sp,
                    color = HydraTextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(checks) { check ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = HydraBgDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, HydraBorder, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (check.status) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (check.status) HydraAndroidGreen else HydraAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = check.title,
                                        fontWeight = FontWeight.Bold,
                                        color = HydraTextPrimary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = check.detail,
                                        color = HydraTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = HydraCyan)
            ) {
                Text("Dismiss", color = HydraBgDark, fontWeight = FontWeight.Bold)
            }
        }
    )
}
