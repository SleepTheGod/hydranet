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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ScopeEntity
import com.example.ui.theme.HydraAndroidGreen
import com.example.ui.theme.HydraBgDark
import com.example.ui.theme.HydraBorder
import com.example.ui.theme.HydraCyan
import com.example.ui.theme.HydraPurple
import com.example.ui.theme.HydraRed
import com.example.ui.theme.HydraSurface
import com.example.ui.theme.HydraTextMuted
import com.example.ui.theme.HydraTextPrimary
import com.example.ui.theme.HydraViolet

@Composable
fun HydraTopBar(
    activeScope: ScopeEntity?,
    onScopeClick: () -> Unit,
    onDoctorClick: () -> Unit,
    onAuditClick: () -> Unit,
    onVaultClick: () -> Unit,
    onPanicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = HydraSurface,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = HydraBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Header with Dragon Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onDoctorClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(HydraPurple, HydraViolet, HydraCyan)
                                )
                            )
                            .padding(1.5.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hydranet_logo),
                            contentDescription = "HydraNet Dragon Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "HYDRA",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.5.sp,
                                color = HydraCyan
                            )
                            Text(
                                text = "NET",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.5.sp,
                                color = HydraPurple
                            )
                        }
                        Text(
                            text = "ANDROID EDITION v0.1",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp,
                            color = HydraAndroidGreen
                        )
                    }
                }

                // Quick Action Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDoctorClick,
                        modifier = Modifier.testTag("btn_doctor")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "Doctor Diagnostics",
                            tint = HydraCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onAuditClick,
                        modifier = Modifier.testTag("btn_audit_log")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = "Cryptographic Audit Log",
                            tint = HydraPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onVaultClick,
                        modifier = Modifier.testTag("btn_loot_vault")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted Loot Vault",
                            tint = HydraAndroidGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onPanicClick,
                        modifier = Modifier.testTag("btn_panic_wipe")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReportProblem,
                            contentDescription = "Emergency Panic Wipe",
                            tint = HydraRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Active Scope Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(HydraBgDark)
                    .border(
                        width = 1.dp,
                        color = if (activeScope != null && !activeScope.isExpired()) HydraAndroidGreen.copy(alpha = 0.5f) else HydraRed.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onScopeClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (activeScope != null && !activeScope.isExpired()) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "Scope Indicator",
                        tint = if (activeScope != null && !activeScope.isExpired()) HydraAndroidGreen else HydraRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeScope != null && !activeScope.isExpired()) "SCOPE: ${activeScope.ssid}" else "NO ACTIVE SCOPE (REFUSAL LOCKED)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeScope != null && !activeScope.isExpired()) HydraTextPrimary else HydraRed
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (activeScope != null && activeScope.authorized) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(HydraAndroidGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AUTH VERIFIED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = HydraAndroidGreen
                        )
                    } else {
                        Text(
                            text = "TAP TO DEFINE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = HydraCyan
                        )
                    }
                }
            }
        }
    }
}
