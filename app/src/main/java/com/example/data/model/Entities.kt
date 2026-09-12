package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scopes")
data class ScopeEntity(
    @PrimaryKey val id: String,
    val ssid: String,
    val bssids: String = "", // Comma-separated
    val ipRanges: String = "", // Comma-separated (CIDR)
    val authorized: Boolean = false,
    val operatorName: String = "Operator",
    val engagementId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 3600_000L // 1 hour default
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt
}

@Entity(tableName = "audit_log")
data class AuditEntryEntity(
    @PrimaryKey val id: String,
    val timestamp: String,
    val operator: String,
    val scopeId: String,
    val action: String,
    val target: String,
    val moduleId: String,
    val result: String,
    val rawCommand: String,
    val entryHash: String,
    val prevHash: String
)

@Entity(tableName = "findings")
data class FindingEntity(
    @PrimaryKey val id: String,
    val scanId: String,
    val severity: String, // CRITICAL, HIGH, MEDIUM, LOW, INFO
    val title: String,
    val plainEnglish: String,
    val technical: String,
    val remediation: String,
    val mitreAttack: String = "", // e.g. "T1040, T1557"
    val category: String = "wireless",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // "WPA Handshake", "BLE Capture", "Device Profile", "Audit Report"
    val encryptedBlob: String,
    val sha256Checksum: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val tags: String = ""
)
