package com.example.engine

import com.example.data.model.AuditEntryEntity
import java.security.MessageDigest

object AuditAnchorEngine {

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun computeEntryHash(
        prevHash: String,
        id: String,
        ts: String,
        action: String,
        operator: String,
        scopeId: String,
        target: String,
        moduleId: String,
        result: String
    ): String {
        val payload = "$prevHash|$id|$ts|$action|$operator|$scopeId|$target|$moduleId|$result"
        return sha256(payload)
    }

    fun verifyChain(entries: List<AuditEntryEntity>): ChainVerificationResult {
        if (entries.isEmpty()) {
            return ChainVerificationResult(isValid = true, message = "Audit log is empty. Genesis ready.", brokenIndex = -1)
        }

        var expectedPrev = ""
        for (i in entries.indices) {
            val entry = entries[i]
            if (entry.prevHash != expectedPrev) {
                return ChainVerificationResult(
                    isValid = false,
                    message = "Hash chain broken at index $i! Expected prevHash '$expectedPrev' but found '${entry.prevHash}'.",
                    brokenIndex = i
                )
            }
            val expectedHash = computeEntryHash(
                prevHash = entry.prevHash,
                id = entry.id,
                ts = entry.timestamp,
                action = entry.action,
                operator = entry.operator,
                scopeId = entry.scopeId,
                target = entry.target,
                moduleId = entry.moduleId,
                result = entry.result
            )
            if (entry.entryHash != expectedHash) {
                return ChainVerificationResult(
                    isValid = false,
                    message = "Tamper detected at entry #${i} ('${entry.action}')! Computed hash does not match stored hash.",
                    brokenIndex = i
                )
            }
            expectedPrev = entry.entryHash
        }

        val merkleRoot = computeMerkleRoot(entries.map { it.entryHash })
        return ChainVerificationResult(
            isValid = true,
            message = "Cryptographic hash-chain is 100% valid (${entries.size} entries). Merkle root anchored.",
            brokenIndex = -1,
            merkleRoot = merkleRoot
        )
    }

    fun computeMerkleRoot(hashes: List<String>): String {
        if (hashes.isEmpty()) return ""
        if (hashes.size == 1) return hashes[0]

        var currentLevel = hashes
        while (currentLevel.size > 1) {
            val nextLevel = mutableListOf<String>()
            var i = 0
            while (i < currentLevel.size) {
                val left = currentLevel[i]
                val right = if (i + 1 < currentLevel.size) currentLevel[i + 1] else left
                nextLevel.add(sha256(left + right))
                i += 2
            }
            currentLevel = nextLevel
        }
        return currentLevel[0]
    }
}

data class ChainVerificationResult(
    val isValid: Boolean,
    val message: String,
    val brokenIndex: Int,
    val merkleRoot: String = ""
)
