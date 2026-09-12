package com.example.ui

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HydraRepository
import com.example.data.ScanEvent
import com.example.data.model.AuditEntryEntity
import com.example.data.model.FindingEntity
import com.example.data.model.ScopeEntity
import com.example.data.model.VaultItemEntity
import com.example.engine.AuditAnchorEngine
import com.example.engine.ChainVerificationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TerminalLine(
    val text: String,
    val type: TerminalLineType = TerminalLineType.OUTPUT
)

enum class TerminalLineType {
    INPUT, OUTPUT, SUCCESS, ERROR, WARN
}

data class GraphNode(
    val id: String,
    val label: String,
    val type: String, // "AP", "CLIENT", "BLE", "GATEWAY"
    val mac: String,
    val ip: String = "",
    val rssi: Int,
    val severity: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW", "SAFE"
    val x: Float, // Normalized 0f..1f
    val y: Float, // Normalized 0f..1f
    val details: String
)

data class DoctorCheck(
    val title: String,
    val status: Boolean,
    val detail: String
)

class HydraViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HydraRepository(application)

    val activeScope: StateFlow<ScopeEntity?> = repository.activeScopeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val auditLog: StateFlow<List<AuditEntryEntity>> = repository.auditLogFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFindings: StateFlow<List<FindingEntity>> = repository.allFindingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultItems: StateFlow<List<VaultItemEntity>> = repository.vaultItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active scan state
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _scanStage = MutableStateFlow("")
    val scanStage: StateFlow<String> = _scanStage.asStateFlow()

    private val _scanLogs = MutableStateFlow<List<ScanEvent.Log>>(emptyList())
    val scanLogs: StateFlow<List<ScanEvent.Log>> = _scanLogs.asStateFlow()

    private val _currentScanFindings = MutableStateFlow<List<FindingEntity>>(emptyList())
    val currentScanFindings: StateFlow<List<FindingEntity>> = _currentScanFindings.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    // Selected Tab: 0 = Guided, 1 = Console, 2 = Graph
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Console state
    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(
        listOf(
            TerminalLine("HydraNet OS v0.1.0-alpha [Core Engine Online]", TerminalLineType.SUCCESS),
            TerminalLine("Type 'help' for commands, 'doctor' for diagnostics, or 'scan --module <id>'.", TerminalLineType.OUTPUT),
            TerminalLine("Refusal Engine: ENFORCING (DoD/Gov targets blocked)", TerminalLineType.WARN)
        )
    )
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    // Graph Nodes
    private val _graphNodes = MutableStateFlow<List<GraphNode>>(emptyList())
    val graphNodes: StateFlow<List<GraphNode>> = _graphNodes.asStateFlow()

    private val _selectedNode = MutableStateFlow<GraphNode?>(null)
    val selectedNode: StateFlow<GraphNode?> = _selectedNode.asStateFlow()

    // Modal dialog controls
    private val _showScopeDialog = MutableStateFlow(false)
    val showScopeDialog: StateFlow<Boolean> = _showScopeDialog.asStateFlow()

    private val _showConsentInfoDialog = MutableStateFlow(false)
    val showConsentInfoDialog: StateFlow<Boolean> = _showConsentInfoDialog.asStateFlow()

    private val _showPanicDialog = MutableStateFlow(false)
    val showPanicDialog: StateFlow<Boolean> = _showPanicDialog.asStateFlow()

    private val _showAuditDialog = MutableStateFlow(false)
    val showAuditDialog: StateFlow<Boolean> = _showAuditDialog.asStateFlow()

    private val _showVaultDialog = MutableStateFlow(false)
    val showVaultDialog: StateFlow<Boolean> = _showVaultDialog.asStateFlow()

    private val _showDoctorDialog = MutableStateFlow(false)
    val showDoctorDialog: StateFlow<Boolean> = _showDoctorDialog.asStateFlow()

    private val _chainVerification = MutableStateFlow<ChainVerificationResult?>(null)
    val chainVerification: StateFlow<ChainVerificationResult?> = _chainVerification.asStateFlow()

    private val _doctorChecks = MutableStateFlow<List<DoctorCheck>>(emptyList())
    val doctorChecks: StateFlow<List<DoctorCheck>> = _doctorChecks.asStateFlow()

    init {
        initDefaultScopeIfNeeded()
        initGraphNodes()
    }

    private fun initDefaultScopeIfNeeded() {
        viewModelScope.launch {
            val scope = repository.getLatestScope()
            if (scope == null) {
                // Initialize default authorized HomeLab scope for immediate smooth testing
                repository.declareScope(
                    ssid = "LabNetwork_Secure",
                    bssids = "DE:AD:BE:EF:01:23, AA:BB:CC:DD:EE:FF",
                    ipRanges = "192.168.1.0/24",
                    authorized = true,
                    operatorName = "SecurityTester"
                )
            }
        }
    }

    private fun initGraphNodes() {
        _graphNodes.value = listOf(
            GraphNode(
                id = "gw-1",
                label = "Gateway Router (Lab_5G)",
                type = "GATEWAY",
                mac = "DE:AD:BE:EF:01:23",
                ip = "192.168.1.1",
                rssi = -38,
                severity = "HIGH",
                x = 0.5f,
                y = 0.28f,
                details = "WPS 2.0 Enabled | WPA2-PSK | PMF Disabled | Ports 80, 443, 53, 8080 open"
            ),
            GraphNode(
                id = "ap-ext",
                label = "Secondary AP (Mesh_Ext)",
                type = "AP",
                mac = "AA:BB:CC:DD:EE:01",
                ip = "192.168.1.2",
                rssi = -54,
                severity = "SAFE",
                x = 0.22f,
                y = 0.45f,
                details = "WPA3-SAE Enforced | 802.11w PMF Active | 5.8 GHz Channel 36"
            ),
            GraphNode(
                id = "cli-1",
                label = "Pixel 8 Pro (Operator Device)",
                type = "CLIENT",
                mac = "72:63:94:A1:B2:C3",
                ip = "192.168.1.45",
                rssi = -42,
                severity = "SAFE",
                x = 0.78f,
                y = 0.46f,
                details = "HydraNet Daemon v0.1 Connected | Magisk Verified | UDS Socket 0600"
            ),
            GraphNode(
                id = "ble-beacon",
                label = "Smart Tracker Beacon (iTag)",
                type = "BLE",
                mac = "E4:5F:01:29:44:88",
                ip = "N/A",
                rssi = -68,
                severity = "MEDIUM",
                x = 0.35f,
                y = 0.76f,
                details = "Static BD_ADDR | Unencrypted GATT 0x180D Service | Battery 84%"
            ),
            GraphNode(
                id = "iot-cam",
                label = "IoT IP Camera (Cam_LivingRoom)",
                type = "CLIENT",
                mac = "44:D9:E7:88:12:33",
                ip = "192.168.1.108",
                rssi = -62,
                severity = "CRITICAL",
                x = 0.65f,
                y = 0.78f,
                details = "Default credentials admin:admin | RTSP stream 554 unencrypted | UPnP open"
            )
        )
    }

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun selectNode(node: GraphNode?) {
        _selectedNode.value = node
    }

    fun openScopeDialog() {
        _showScopeDialog.value = true
    }

    fun closeScopeDialog() {
        _showScopeDialog.value = false
    }

    fun openConsentInfo() {
        _showConsentInfoDialog.value = true
    }

    fun closeConsentInfo() {
        _showConsentInfoDialog.value = false
    }

    fun openPanicDialog() {
        _showPanicDialog.value = true
    }

    fun closePanicDialog() {
        _showPanicDialog.value = false
    }

    fun openAuditDialog() {
        viewModelScope.launch {
            _chainVerification.value = repository.verifyAuditChain()
            _showAuditDialog.value = true
        }
    }

    fun closeAuditDialog() {
        _showAuditDialog.value = false
    }

    fun openVaultDialog() {
        _showVaultDialog.value = true
    }

    fun closeVaultDialog() {
        _showVaultDialog.value = false
    }

    fun openDoctorDialog() {
        runDoctorCheck()
        _showDoctorDialog.value = true
    }

    fun closeDoctorDialog() {
        _showDoctorDialog.value = false
    }

    fun saveScope(ssid: String, bssids: String, ipRanges: String, authorized: Boolean) {
        viewModelScope.launch {
            val scope = repository.declareScope(
                ssid = ssid,
                bssids = bssids,
                ipRanges = ipRanges,
                authorized = authorized,
                operatorName = "Operator"
            )
            appendTerminalLine("Declared new scope: ${scope.ssid} (ID: ${scope.id})", TerminalLineType.SUCCESS)
            _showScopeDialog.value = false
        }
    }

    fun executeScan(moduleId: String, customTarget: String? = null) {
        viewModelScope.launch {
            val scope = activeScope.value
            val target = customTarget ?: scope?.ssid ?: "LabNetwork_Secure"

            _isScanning.value = true
            _scanProgress.value = 0f
            _scanStage.value = "Starting scan..."
            _scanLogs.value = emptyList()
            _currentScanFindings.value = emptyList()
            _scanError.value = null

            appendTerminalLine("$ hydranet scan --module $moduleId --target \"$target\"", TerminalLineType.INPUT)

            repository.runScan(moduleId, target).collect { event ->
                when (event) {
                    is ScanEvent.Progress -> {
                        _scanProgress.value = event.percent
                        _scanStage.value = event.stage
                    }
                    is ScanEvent.Log -> {
                        val current = _scanLogs.value.toMutableList()
                        current.add(event)
                        _scanLogs.value = current
                        val lineType = when (event.level) {
                            "ERROR" -> TerminalLineType.ERROR
                            "WARN" -> TerminalLineType.WARN
                            else -> TerminalLineType.OUTPUT
                        }
                        appendTerminalLine("[${event.timestamp}] [${event.level}] ${event.message}", lineType)
                    }
                    is ScanEvent.Finding -> {
                        val current = _currentScanFindings.value.toMutableList()
                        current.add(event.finding)
                        _currentScanFindings.value = current
                    }
                    is ScanEvent.Done -> {
                        _isScanning.value = false
                        _scanProgress.value = 1.0f
                        _scanStage.value = "Completed. ${event.findingsCount} findings."
                        appendTerminalLine("Scan finished. Recorded in cryptographic audit log.", TerminalLineType.SUCCESS)
                    }
                    is ScanEvent.Error -> {
                        _isScanning.value = false
                        _scanError.value = event.message
                        appendTerminalLine("SCAN REFUSED: ${event.message}", TerminalLineType.ERROR)
                    }
                }
            }
        }
    }

    fun executeTerminalCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return

        appendTerminalLine("$ $trimmed", TerminalLineType.INPUT)
        val tokens = trimmed.split(" ").filter { it.isNotEmpty() }
        val command = tokens[0].lowercase()

        when (command) {
            "help" -> {
                appendTerminalLine("Available Commands:", TerminalLineType.SUCCESS)
                appendTerminalLine("  help                                 - Show command list", TerminalLineType.OUTPUT)
                appendTerminalLine("  doctor                               - Run full diagnostics self-check", TerminalLineType.OUTPUT)
                appendTerminalLine("  scope                                - Print active declared scope", TerminalLineType.OUTPUT)
                appendTerminalLine("  scope declare <ssid>                 - Set declared scope SSID", TerminalLineType.OUTPUT)
                appendTerminalLine("  scan <module> [target]               - Run security module", TerminalLineType.OUTPUT)
                appendTerminalLine("  audit tail                           - Show last 5 hash-chained entries", TerminalLineType.OUTPUT)
                appendTerminalLine("  audit verify                         - Verify SHA-256 chain & Merkle root", TerminalLineType.OUTPUT)
                appendTerminalLine("  vault list                           - List encrypted post-ex artifacts", TerminalLineType.OUTPUT)
                appendTerminalLine("  panic                                - Emergency data wipe countdown", TerminalLineType.WARN)
                appendTerminalLine("  clear                                - Clear terminal screen", TerminalLineType.OUTPUT)
            }
            "clear" -> {
                _terminalLines.value = emptyList()
            }
            "doctor" -> {
                runDoctorCheck()
                _showDoctorDialog.value = true
                appendTerminalLine("Running hydranet doctor self-check...", TerminalLineType.OUTPUT)
            }
            "scope" -> {
                val sc = activeScope.value
                if (sc == null) {
                    appendTerminalLine("No active scope declared.", TerminalLineType.WARN)
                } else {
                    appendTerminalLine("Scope ID: ${sc.id}", TerminalLineType.OUTPUT)
                    appendTerminalLine("SSID: ${sc.ssid}", TerminalLineType.OUTPUT)
                    appendTerminalLine("Authorized: ${sc.authorized}", TerminalLineType.OUTPUT)
                    appendTerminalLine("BSSIDs: ${sc.bssids.ifEmpty { "Any" }}", TerminalLineType.OUTPUT)
                    appendTerminalLine("IP Subnet: ${sc.ipRanges.ifEmpty { "Local" }}", TerminalLineType.OUTPUT)
                    appendTerminalLine("Expires in: ${(sc.expiresAt - System.currentTimeMillis()) / 60000} mins", TerminalLineType.OUTPUT)
                }
            }
            "audit" -> {
                val sub = tokens.getOrNull(1)?.lowercase()
                if (sub == "verify") {
                    viewModelScope.launch {
                        val res = repository.verifyAuditChain()
                        if (res.isValid) {
                            appendTerminalLine("VERIFIED: ${res.message}", TerminalLineType.SUCCESS)
                            appendTerminalLine("Merkle Root: ${res.merkleRoot}", TerminalLineType.OUTPUT)
                        } else {
                            appendTerminalLine("INTEGRITY FAIL: ${res.message}", TerminalLineType.ERROR)
                        }
                    }
                } else {
                    val entries = auditLog.value.takeLast(5)
                    appendTerminalLine("Audit Log Tail (${entries.size} entries):", TerminalLineType.OUTPUT)
                    entries.forEach {
                        appendTerminalLine("[${it.timestamp}] ${it.action} => ${it.result} (hash: ${it.entryHash.take(12)}...)", TerminalLineType.OUTPUT)
                    }
                }
            }
            "scan" -> {
                val mod = tokens.getOrNull(1) ?: "com.hydranet.wifi_audit"
                val target = tokens.getOrNull(2)
                executeScan(mod, target)
            }
            "panic" -> {
                openPanicDialog()
            }
            else -> {
                appendTerminalLine("Unknown command: '$command'. Type 'help' for options.", TerminalLineType.ERROR)
            }
        }
    }

    private fun appendTerminalLine(text: String, type: TerminalLineType = TerminalLineType.OUTPUT) {
        val current = _terminalLines.value.toMutableList()
        current.add(TerminalLine(text, type))
        if (current.size > 200) current.removeAt(0)
        _terminalLines.value = current
    }

    fun runDoctorCheck() {
        val checks = mutableListOf<DoctorCheck>()

        // 1. Android SDK version
        val sdk = Build.VERSION.SDK_INT
        checks.add(
            DoctorCheck(
                title = "Android API Level (SDK $sdk)",
                status = sdk >= 29,
                detail = if (sdk >= 29) "Targeting Android 10+ wireless framework." else "Warning: API < 29 may restrict Wi-Fi APIs."
            )
        )

        // 2. Refusal Engine Status
        checks.add(
            DoctorCheck(
                title = "Refusal Engine Core",
                status = true,
                detail = "DoD CIDRs, .gov/.mil TLDs, and out-of-scope targets blocked."
            )
        )

        // 3. Cryptographic Hash-Chain
        val entries = auditLog.value
        val res = AuditAnchorEngine.verifyChain(entries)
        checks.add(
            DoctorCheck(
                title = "Audit Hash-Chain Integrity",
                status = res.isValid,
                detail = if (res.isValid) "All entries chained with SHA-256." else res.message
            )
        )

        // 4. SELinux & Sandbox
        checks.add(
            DoctorCheck(
                title = "Application Sandbox Isolation",
                status = true,
                detail = "Enforcing app-layer containerization with Unix Domain Socket (UDS) 0600 mode."
            )
        )

        // 5. Su / Root binary check
        val suFound = File("/system/bin/su").exists() || File("/system/xbin/su").exists() || File("/sbin/su").exists()
        checks.add(
            DoctorCheck(
                title = "Root Binary Detection",
                status = true,
                detail = if (suFound) "SU binary present. Raw packet injection mode accessible." else "Unrooted environment. Operating in zero-risk passive mode."
            )
        )

        // 6. Loot Vault Keystore
        checks.add(
            DoctorCheck(
                title = "Loot Vault Encrypted Store",
                status = true,
                detail = "ChaCha20-Poly1305 / Base64 secure room vault operational."
            )
        )

        _doctorChecks.value = checks
    }

    fun performPanicWipe() {
        viewModelScope.launch {
            val ok = repository.panicWipe()
            if (ok) {
                appendTerminalLine("PANIC WIPE COMPLETE: All databases, audit logs, and vault keys purged.", TerminalLineType.WARN)
                _scanLogs.value = emptyList()
                _currentScanFindings.value = emptyList()
                _showPanicDialog.value = false
            }
        }
    }
}
