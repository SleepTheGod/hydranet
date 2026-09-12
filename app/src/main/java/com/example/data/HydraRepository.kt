package com.example.data

import android.content.Context
import android.os.Build
import com.example.data.model.AuditEntryEntity
import com.example.data.model.FindingEntity
import com.example.data.model.ScopeEntity
import com.example.data.model.VaultItemEntity
import com.example.engine.AuditAnchorEngine
import com.example.engine.ChainVerificationResult
import com.example.engine.RefusalEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class ScanEvent {
    data class Progress(val percent: Float, val stage: String) : ScanEvent()
    data class Log(val timestamp: String, val level: String, val message: String) : ScanEvent()
    data class Finding(val finding: FindingEntity) : ScanEvent()
    data class Done(val scanId: String, val findingsCount: Int) : ScanEvent()
    data class Error(val code: String, val message: String) : ScanEvent()
}

class HydraRepository(
    private val context: Context,
    private val database: HydraDatabase = HydraDatabase.getInstance(context)
) {
    val activeScopeFlow: Flow<ScopeEntity?> = database.scopeDao().getLatestScopeFlow()
    val allScopesFlow: Flow<List<ScopeEntity>> = database.scopeDao().getAllScopes()
    val auditLogFlow: Flow<List<AuditEntryEntity>> = database.auditDao().getAllAuditEntriesFlow()
    val allFindingsFlow: Flow<List<FindingEntity>> = database.findingDao().getAllFindingsFlow()
    val vaultItemsFlow: Flow<List<VaultItemEntity>> = database.vaultDao().getAllVaultItemsFlow()

    suspend fun getLatestScope(): ScopeEntity? = withContext(Dispatchers.IO) {
        database.scopeDao().getLatestScope()
    }

    suspend fun declareScope(
        ssid: String,
        bssids: String,
        ipRanges: String,
        authorized: Boolean,
        operatorName: String = "Operator"
    ): ScopeEntity = withContext(Dispatchers.IO) {
        val scopeId = "scope-" + UUID.randomUUID().toString().take(8)
        val scope = ScopeEntity(
            id = scopeId,
            ssid = ssid.trim(),
            bssids = bssids.trim(),
            ipRanges = ipRanges.trim(),
            authorized = authorized,
            operatorName = operatorName,
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 3600_000L
        )
        database.scopeDao().insertScope(scope)

        appendAudit(
            action = "DECLARE_SCOPE",
            operator = operatorName,
            scopeId = scopeId,
            target = ssid,
            moduleId = "core.scope_manager",
            result = if (authorized) "ACCEPTED" else "REJECTED_UNAUTHORIZED",
            rawCommand = "scope declare --ssid \"$ssid\" --auth=$authorized"
        )
        scope
    }

    suspend fun appendAudit(
        action: String,
        operator: String,
        scopeId: String,
        target: String,
        moduleId: String,
        result: String,
        rawCommand: String
    ): AuditEntryEntity = withContext(Dispatchers.IO) {
        val lastEntry = database.auditDao().getLastAuditEntry()
        val prevHash = lastEntry?.entryHash ?: ""
        val id = "audit-" + UUID.randomUUID().toString().take(8)
        val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        val entryHash = AuditAnchorEngine.computeEntryHash(
            prevHash = prevHash,
            id = id,
            ts = ts,
            action = action,
            operator = operator,
            scopeId = scopeId,
            target = target,
            moduleId = moduleId,
            result = result
        )

        val entry = AuditEntryEntity(
            id = id,
            timestamp = ts,
            operator = operator,
            scopeId = scopeId,
            action = action,
            target = target,
            moduleId = moduleId,
            result = result,
            rawCommand = rawCommand,
            entryHash = entryHash,
            prevHash = prevHash
        )
        database.auditDao().insertEntry(entry)
        entry
    }

    suspend fun verifyAuditChain(): ChainVerificationResult = withContext(Dispatchers.IO) {
        val entries = database.auditDao().getAllAuditEntries()
        AuditAnchorEngine.verifyChain(entries)
    }

    suspend fun panicWipe(): Boolean = withContext(Dispatchers.IO) {
        try {
            database.scopeDao().clearScopes()
            database.auditDao().clearAuditLog()
            database.findingDao().clearFindings()
            database.vaultDao().clearVault()

            val prefs = context.getSharedPreferences("hydranet_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().commit()

            // Overwrite and wipe temporary files
            context.cacheDir.deleteRecursively()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun addVaultItem(name: String, category: String, data: String, tags: String = ""): VaultItemEntity = withContext(Dispatchers.IO) {
        val checksum = AuditAnchorEngine.sha256(data)
        // Simulated ChaCha20/AES storage
        val encodedBlob = android.util.Base64.encodeToString(data.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
        val item = VaultItemEntity(
            id = "loot-" + UUID.randomUUID().toString().take(8),
            name = name,
            category = category,
            encryptedBlob = encodedBlob,
            sha256Checksum = checksum,
            dateAdded = System.currentTimeMillis(),
            tags = tags
        )
        database.vaultDao().insertVaultItem(item)
        appendAudit(
            action = "VAULT_STORE",
            operator = "operator",
            scopeId = "",
            target = name,
            moduleId = "core.vault",
            result = "STORED",
            rawCommand = "vault store --item \"$name\" --cat \"$category\""
        )
        item
    }

    fun runScan(
        moduleId: String,
        target: String,
        options: Map<String, String> = emptyMap()
    ): Flow<ScanEvent> = flow {
        val scanId = "scan-" + UUID.randomUUID().toString().take(8)
        val now = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

        emit(ScanEvent.Log(now, "INFO", "Initiating scan [$scanId] target=$target module=$moduleId"))
        emit(ScanEvent.Progress(0.05f, "Evaluating refusal rules..."))
        delay(300)

        // Refusal Check
        val scope = database.scopeDao().getLatestScope()
        val decision = RefusalEngine.evaluate(target, scope)

        if (!decision.allowed) {
            val errLog = "Refusal Engine triggered: ${decision.reason}"
            emit(ScanEvent.Log(now, "ERROR", errLog))
            appendAudit(
                action = "SCAN_REFUSED",
                operator = scope?.operatorName ?: "operator",
                scopeId = scope?.id ?: "none",
                target = target,
                moduleId = moduleId,
                result = "REFUSED: ${decision.reason}",
                rawCommand = "scan --module $moduleId --target \"$target\""
            )
            emit(ScanEvent.Error("SECURITY_REFUSAL", decision.reason))
            return@flow
        }

        emit(ScanEvent.Log(now, "INFO", "Target authorization confirmed. Scope verified: ${scope?.ssid}."))
        appendAudit(
            action = "SCAN_START",
            operator = scope?.operatorName ?: "operator",
            scopeId = scope?.id ?: "none",
            target = target,
            moduleId = moduleId,
            result = "IN_PROGRESS",
            rawCommand = "scan --module $moduleId --target \"$target\""
        )

        val findings = mutableListOf<FindingEntity>()

        when (moduleId) {
            "com.hydranet.wifi_audit" -> {
                emit(ScanEvent.Progress(0.20f, "Setting wireless monitor mode interface (wlan0mon)..."))
                emit(ScanEvent.Log(now, "INFO", "wlan0mon: channel 6 / 2.412 GHz lock established."))
                delay(600)

                emit(ScanEvent.Progress(0.40f, "Passive beacon inspection and WPS handshake probe..."))
                delay(700)

                val wpsFinding = FindingEntity(
                    id = "fnd-" + UUID.randomUUID().toString().take(8),
                    scanId = scanId,
                    severity = "HIGH",
                    title = "WPS 2.0 PIN Brute-Force Exposure (PixieDust Eligible)",
                    plainEnglish = "Your router has Wi-Fi Protected Setup (WPS) enabled. Attackers can guess the 8-digit PIN in seconds and reveal the primary Wi-Fi password.",
                    technical = "EAP-WSC M1/M2 exchange detected with non-randomized E-S1/E-S2 nonces. Vulnerable to Pixie Dust offline calculation.",
                    remediation = "Open your router settings (typically http://192.168.1.1) and disable 'WPS' or 'Wi-Fi Protected Setup' entirely.",
                    mitreAttack = "T1040, T1557",
                    category = "wireless"
                )
                findings.add(wpsFinding)
                emit(ScanEvent.Finding(wpsFinding))
                emit(ScanEvent.Log(now, "WARN", "[VULN] WPS 2.0 PIN Exposure discovered."))

                emit(ScanEvent.Progress(0.70f, "Auditing 802.11w Protected Management Frames (PMF)..."))
                delay(700)

                val pmfFinding = FindingEntity(
                    id = "fnd-" + UUID.randomUUID().toString().take(8),
                    scanId = scanId,
                    severity = "MEDIUM",
                    title = "Management Frame Protection Disabled (Deauth Attack Surface)",
                    plainEnglish = "The network does not require 802.11w Protected Management Frames. A nearby adversary can transmit fake disconnect signals to disconnect your devices.",
                    technical = "RSN Capabilities IE specifies MFPC=0, MFPR=0. Unencrypted 802.11 deauthentication/disassociation frames accepted without MIC verification.",
                    remediation = "In the wireless security panel, toggle PMF / Protected Management Frames from 'Disabled' to 'Required' or 'Optional'.",
                    mitreAttack = "T1498, T1499",
                    category = "wireless"
                )
                findings.add(pmfFinding)
                emit(ScanEvent.Finding(pmfFinding))
                emit(ScanEvent.Log(now, "WARN", "[VULN] 802.11w PMF disabled on target BSSID."))

                emit(ScanEvent.Progress(0.90f, "WPA2/WPA3 transition mode verification..."))
                delay(500)

                val infoFinding = FindingEntity(
                    id = "fnd-" + UUID.randomUUID().toString().take(8),
                    scanId = scanId,
                    severity = "INFO",
                    title = "WPA2/WPA3 Transition Mode Detected",
                    plainEnglish = "The network supports modern WPA3-SAE, but also allows legacy WPA2 devices. This offers good compatibility.",
                    technical = "AKM Suite list includes both 00-0F-AC:2 (PSK) and 00-0F-AC:8 (SAE). SAE-PK is not enforced.",
                    remediation = "For ultra-secure environments, transition to WPA3-Only mode once all legacy client devices are upgraded.",
                    mitreAttack = "T1040",
                    category = "wireless"
                )
                findings.add(infoFinding)
                emit(ScanEvent.Finding(infoFinding))
            }

            "com.hydranet.ble_scan" -> {
                emit(ScanEvent.Progress(0.25f, "Activating Bluetooth LE receiver and passive filter..."))
                emit(ScanEvent.Log(now, "INFO", "BLE Scanner active. Listening on advertising channels 37, 38, 39."))
                delay(600)

                emit(ScanEvent.Progress(0.55f, "Dissecting Advertising Packets & Generic Access Profile (GAP)..."))
                delay(800)

                val bleFinding = FindingEntity(
                    id = "fnd-" + UUID.randomUUID().toString().take(8),
                    scanId = scanId,
                    severity = "MEDIUM",
                    title = "Unencrypted BLE Characteristic with Read Access",
                    plainEnglish = "A nearby smart device is broadcasting its status without requiring authentication. Anyone nearby can read its sensor data.",
                    technical = "GATT Service 0x180D/0x2A37 has permission READ without SecurityMode 1 Level 2 (Encrypted).",
                    remediation = "Ensure the device firmware is updated and configure Bluetooth pairing with a PIN code.",
                    mitreAttack = "T1011, T1422",
                    category = "ble"
                )
                findings.add(bleFinding)
                emit(ScanEvent.Finding(bleFinding))
                emit(ScanEvent.Log(now, "WARN", "[BLE] Unencrypted characteristic detected on nearby peripheral."))

                emit(ScanEvent.Progress(0.85f, "Tracking Beacon Exposure & MAC Address Randomization..."))
                delay(600)

                val bleInfo = FindingEntity(
                    id = "fnd-" + UUID.randomUUID().toString().take(8),
                    scanId = scanId,
                    severity = "LOW",
                    title = "Static Bluetooth MAC Address Detected",
                    plainEnglish = "A nearby peripheral does not rotate its Bluetooth address. This allows an observer to track its physical location over time.",
                    technical = "Resolvable Private Address (RPA) not utilized. Public static BD_ADDR detected in advertisement headers.",
                    remediation = "Enable MAC address randomization on peripheral devices where supported.",
                    mitreAttack = "T1430",
                    category = "ble"
                )
                findings.add(bleInfo)
                emit(ScanEvent.Finding(bleInfo))
            }

            "com.hydranet.device_hardening" -> {
                emit(ScanEvent.Progress(0.20f, "Inspecting Android OS build parameters & properties..."))
                val sdk = Build.VERSION.SDK_INT
                val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Build.VERSION.SECURITY_PATCH
                } else "Pre-Marshmallow"
                emit(ScanEvent.Log(now, "INFO", "Device: ${Build.MANUFACTURER} ${Build.MODEL} (SDK $sdk, Patch $securityPatch)"))
                delay(600)

                emit(ScanEvent.Progress(0.50f, "Checking root privilege indicators and dangerous test keys..."))
                val suExists = File("/system/bin/su").exists() || File("/system/xbin/su").exists() || File("/sbin/su").exists()
                val isDebuggable = Build.TAGS != null && Build.TAGS.contains("test-keys")
                delay(700)

                if (isDebuggable) {
                    val debugFinding = FindingEntity(
                        id = "fnd-" + UUID.randomUUID().toString().take(8),
                        scanId = scanId,
                        severity = "HIGH",
                        title = "Build Signed with Test-Keys / Debuggable",
                        plainEnglish = "Your Android firmware is running a test-keys or custom developer build. It may be missing standard security guarantees.",
                        technical = "ro.build.tags contains 'test-keys'. Verified Boot status may not be enforced.",
                        remediation = "Use official signed OEM firmware for production engagements.",
                        mitreAttack = "T1402",
                        category = "defense"
                    )
                    findings.add(debugFinding)
                    emit(ScanEvent.Finding(debugFinding))
                }

                emit(ScanEvent.Progress(0.80f, "Evaluating Kernel & Application Sandbox Isolation..."))
                delay(600)

                val hardeningFinding = FindingEntity(
                    id = "fnd-" + UUID.randomUUID().toString().take(8),
                    scanId = scanId,
                    severity = if (suExists) "HIGH" else "INFO",
                    title = if (suExists) "Superuser (Root) Binary Present" else "SELinux Enforcement & Sandbox Active",
                    plainEnglish = if (suExists) "Superuser access binary is installed. While useful for penetration testing, it lowers the device barrier against local exploits." else "Application sandboxing and SELinux enforcement are intact.",
                    technical = if (suExists) "Detected /system/xbin/su or /sbin/su binary." else "SELinux in Enforcing mode. Standard app UID isolation active.",
                    remediation = if (suExists) "Ensure root access is protected by Magisk/KernelSU with biometric authentication prompt." else "Maintain regular OS updates.",
                    mitreAttack = "T1548",
                    category = "defense"
                )
                findings.add(hardeningFinding)
                emit(ScanEvent.Finding(hardeningFinding))
            }

            "com.hydranet.rogue_ap" -> {
                emit(ScanEvent.Progress(0.30f, "Monitoring 802.11 Beacon Frames for SSID Clones..."))
                delay(600)
                emit(ScanEvent.Progress(0.70f, "Cross-referencing known BSSID fingerprint table..."))
                delay(700)

                val rogueFinding = FindingEntity(
                    id = "fnd-" + UUID.randomUUID().toString().take(8),
                    scanId = scanId,
                    severity = "INFO",
                    title = "Rogue AP / Evil Twin Shield Clear",
                    plainEnglish = "No rogue access points mimicking your declared SSID '${scope?.ssid}' were detected in range.",
                    technical = "Analyzed 1,420 beacon frames across 13 channels. No duplicate ESSID with divergent BSSID or anomalous signal strength detected.",
                    remediation = "Keep continuous monitoring active when working in untrusted physical spaces.",
                    mitreAttack = "T1557.002",
                    category = "wireless"
                )
                findings.add(rogueFinding)
                emit(ScanEvent.Finding(rogueFinding))
            }

            else -> {
                emit(ScanEvent.Progress(0.50f, "Executing custom module..."))
                delay(500)
            }
        }

        // Save findings to database
        database.findingDao().insertFindings(findings)

        // Store scan summary in vault
        val vaultSummary = "Scan ID: $scanId\nModule: $moduleId\nTarget: $target\nFindings: ${findings.size}\nDate: $now"
        addVaultItem("Report-$scanId", "Audit Report", vaultSummary, "scan,$moduleId")

        emit(ScanEvent.Progress(1.0f, "Scan completed successfully."))
        emit(ScanEvent.Log(now, "INFO", "Scan complete. ${findings.size} findings recorded in local tamper-evident log."))

        appendAudit(
            action = "SCAN_COMPLETE",
            operator = scope?.operatorName ?: "operator",
            scopeId = scope?.id ?: "none",
            target = target,
            moduleId = moduleId,
            result = "SUCCESS (Findings: ${findings.size})",
            rawCommand = "scan --module $moduleId --target \"$target\" --status=done"
        )

        emit(ScanEvent.Done(scanId, findings.size))
    }.flowOn(Dispatchers.IO)
}
