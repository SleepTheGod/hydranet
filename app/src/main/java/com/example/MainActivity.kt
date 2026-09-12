package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HydraViewModel
import com.example.ui.components.AuditLogDialog
import com.example.ui.components.ConsentInfoDialog
import com.example.ui.components.DoctorDialog
import com.example.ui.components.HydraBottomNav
import com.example.ui.components.HydraTopBar
import com.example.ui.components.PanicWipeDialog
import com.example.ui.components.ScopeDialog
import com.example.ui.components.VaultDialog
import com.example.ui.screens.ConsoleScreen
import com.example.ui.screens.GraphScreen
import com.example.ui.screens.GuidedScreen
import com.example.ui.theme.HydraBgDark
import com.example.ui.theme.HydraNetTheme

class MainActivity : ComponentActivity() {
    private val viewModel: HydraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HydraNetTheme {
                HydraMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HydraMainApp(viewModel: HydraViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val activeScope by viewModel.activeScope.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val scanStage by viewModel.scanStage.collectAsStateWithLifecycle()
    val scanLogs by viewModel.scanLogs.collectAsStateWithLifecycle()
    val currentFindings by viewModel.currentScanFindings.collectAsStateWithLifecycle()
    val allFindings by viewModel.allFindings.collectAsStateWithLifecycle()
    val scanError by viewModel.scanError.collectAsStateWithLifecycle()

    val terminalLines by viewModel.terminalLines.collectAsStateWithLifecycle()
    val graphNodes by viewModel.graphNodes.collectAsStateWithLifecycle()
    val selectedNode by viewModel.selectedNode.collectAsStateWithLifecycle()

    val showScopeDialog by viewModel.showScopeDialog.collectAsStateWithLifecycle()
    val showConsentInfo by viewModel.showConsentInfoDialog.collectAsStateWithLifecycle()
    val showPanicDialog by viewModel.showPanicDialog.collectAsStateWithLifecycle()
    val showAuditDialog by viewModel.showAuditDialog.collectAsStateWithLifecycle()
    val showVaultDialog by viewModel.showVaultDialog.collectAsStateWithLifecycle()
    val showDoctorDialog by viewModel.showDoctorDialog.collectAsStateWithLifecycle()

    val auditLog by viewModel.auditLog.collectAsStateWithLifecycle()
    val vaultItems by viewModel.vaultItems.collectAsStateWithLifecycle()
    val chainVerification by viewModel.chainVerification.collectAsStateWithLifecycle()
    val doctorChecks by viewModel.doctorChecks.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(HydraBgDark),
        topBar = {
            HydraTopBar(
                activeScope = activeScope,
                onScopeClick = { viewModel.openScopeDialog() },
                onDoctorClick = { viewModel.openDoctorDialog() },
                onAuditClick = { viewModel.openAuditDialog() },
                onVaultClick = { viewModel.openVaultDialog() },
                onPanicClick = { viewModel.openPanicDialog() }
            )
        },
        bottomBar = {
            HydraBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> GuidedScreen(
                    activeScope = activeScope,
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    scanStage = scanStage,
                    scanLogs = scanLogs,
                    findings = if (currentFindings.isNotEmpty()) currentFindings else allFindings,
                    scanError = scanError,
                    onOpenScopeDialog = { viewModel.openScopeDialog() },
                    onRunScan = { moduleId -> viewModel.executeScan(moduleId) }
                )
                1 -> ConsoleScreen(
                    terminalLines = terminalLines,
                    onExecuteCommand = { cmd -> viewModel.executeTerminalCommand(cmd) }
                )
                2 -> GraphScreen(
                    nodes = graphNodes,
                    selectedNode = selectedNode,
                    onSelectNode = { viewModel.selectNode(it) },
                    onAuditTarget = { target ->
                        viewModel.selectTab(0)
                        viewModel.executeScan("com.hydranet.wifi_audit", target)
                    }
                )
            }
        }

        // Dialogs & Sheets
        if (showScopeDialog) {
            ScopeDialog(
                currentScope = activeScope,
                onDismiss = { viewModel.closeScopeDialog() },
                onSave = { ssid, bssids, ipRanges, authorized ->
                    viewModel.saveScope(ssid, bssids, ipRanges, authorized)
                },
                onLearnMore = { viewModel.openConsentInfo() }
            )
        }

        if (showConsentInfo) {
            ConsentInfoDialog(onDismiss = { viewModel.closeConsentInfo() })
        }

        if (showPanicDialog) {
            PanicWipeDialog(
                onDismiss = { viewModel.closePanicDialog() },
                onConfirmWipe = { viewModel.performPanicWipe() }
            )
        }

        if (showAuditDialog) {
            AuditLogDialog(
                auditEntries = auditLog,
                verification = chainVerification,
                onDismiss = { viewModel.closeAuditDialog() }
            )
        }

        if (showVaultDialog) {
            VaultDialog(
                vaultItems = vaultItems,
                onDismiss = { viewModel.closeVaultDialog() }
            )
        }

        if (showDoctorDialog) {
            DoctorDialog(
                checks = doctorChecks,
                onDismiss = { viewModel.closeDoctorDialog() }
            )
        }
    }
}
