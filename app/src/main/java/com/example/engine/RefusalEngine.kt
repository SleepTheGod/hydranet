package com.example.engine

import com.example.data.model.ScopeEntity

data class RefusalDecision(
    val allowed: Boolean,
    val reason: String
)

object RefusalEngine {
    private val BLOCKED_SUFFIXES = listOf(
        ".gov", ".mil", ".gov.uk", ".gov.au", ".gov.ca", ".mil.uk", ".fed.us"
    )

    // First octets of DoD and reserved blocks
    private val BLOCKED_FIRST_OCTETS = setOf(6, 7, 11, 21, 22, 26, 28, 29, 30, 33, 55, 214, 215)

    fun isCriticalInfra(target: String): Boolean {
        val t = target.trim().lowercase()
        if (BLOCKED_SUFFIXES.any { t.endsWith(it) }) return true

        // Check if IP
        val parts = t.split(".")
        if (parts.size == 4) {
            val first = parts[0].toIntOrNull()
            if (first != null) {
                if (first in BLOCKED_FIRST_OCTETS) return true
                if (first in 224..255) return true // Multicast & experimental
            }
        }
        return false
    }

    fun evaluate(target: String, scope: ScopeEntity?): RefusalDecision {
        val t = target.trim()
        if (t.isBlank()) {
            return RefusalDecision(false, "Target cannot be empty.")
        }

        // 1. Hard blocklist check
        if (isCriticalInfra(t)) {
            return RefusalDecision(
                allowed = false,
                reason = "SECURITY REFUSAL: Target '$t' is on the hard blocklist (Critical Infrastructure / Government / Military namespace)."
            )
        }

        // 2. Active scope check
        if (scope == null) {
            return RefusalDecision(
                allowed = false,
                reason = "REFUSAL: No active declared scope. An authorized scope must be defined before scanning."
            )
        }

        if (scope.isExpired()) {
            return RefusalDecision(
                allowed = false,
                reason = "REFUSAL: Declared scope has expired at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date(scope.expiresAt))}."
            )
        }

        if (!scope.authorized) {
            return RefusalDecision(
                allowed = false,
                reason = "REFUSAL: Operator authorization checkbox was not verified."
            )
        }

        // 3. Match against SSID
        if (t.equals(scope.ssid.trim(), ignoreCase = true)) {
            return RefusalDecision(true, "In scope (SSID match).")
        }

        // Match against BSSID list
        val bssids = scope.bssids.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        if (bssids.contains(t.lowercase())) {
            return RefusalDecision(true, "In scope (BSSID match).")
        }

        // Match against IP ranges or local target
        val ipRanges = scope.ipRanges.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (ipRanges.any { it.equals(t, ignoreCase = true) || isIpInCidr(t, it) }) {
            return RefusalDecision(true, "In scope (IP subnet match).")
        }

        // For device local checks
        if (t.equals("self", ignoreCase = true) || t.equals("localhost", ignoreCase = true) || t.startsWith("ble://")) {
            return RefusalDecision(true, "Permitted local diagnostic target.")
        }

        return RefusalDecision(
            allowed = false,
            reason = "REFUSAL: Target '$t' is outside the declared scope '${scope.ssid}'. Cross-boundary testing is strictly forbidden."
        )
    }

    private fun isIpInCidr(ip: String, cidr: String): Boolean {
        return try {
            if (!cidr.contains("/")) return ip == cidr
            val parts = cidr.split("/")
            val network = parts[0]
            val maskBits = parts[1].toInt()
            val ipInt = ipToInt(ip)
            val netInt = ipToInt(network)
            val mask = if (maskBits == 0) 0 else (-1 shl (32 - maskBits))
            (ipInt and mask) == (netInt and mask)
        } catch (_: Exception) {
            false
        }
    }

    private fun ipToInt(ip: String): Int {
        val octets = ip.split(".").map { it.toInt() }
        return (octets[0] shl 24) or (octets[1] shl 16) or (octets[2] shl 8) or octets[3]
    }
}
