package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AuditEntryEntity
import com.example.data.model.FindingEntity
import com.example.data.model.ScopeEntity
import com.example.data.model.VaultItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScopeDao {
    @Query("SELECT * FROM scopes ORDER BY createdAt DESC")
    fun getAllScopes(): Flow<List<ScopeEntity>>

    @Query("SELECT * FROM scopes ORDER BY createdAt DESC LIMIT 1")
    fun getLatestScopeFlow(): Flow<ScopeEntity?>

    @Query("SELECT * FROM scopes ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestScope(): ScopeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScope(scope: ScopeEntity)

    @Query("DELETE FROM scopes")
    suspend fun clearScopes()
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_log ORDER BY rowid ASC")
    fun getAllAuditEntriesFlow(): Flow<List<AuditEntryEntity>>

    @Query("SELECT * FROM audit_log ORDER BY rowid ASC")
    suspend fun getAllAuditEntries(): List<AuditEntryEntity>

    @Query("SELECT * FROM audit_log ORDER BY rowid DESC LIMIT 1")
    suspend fun getLastAuditEntry(): AuditEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: AuditEntryEntity)

    @Query("DELETE FROM audit_log")
    suspend fun clearAuditLog()
}

@Dao
interface FindingDao {
    @Query("SELECT * FROM findings ORDER BY timestamp DESC")
    fun getAllFindingsFlow(): Flow<List<FindingEntity>>

    @Query("SELECT * FROM findings WHERE scanId = :scanId ORDER BY timestamp DESC")
    fun getFindingsByScan(scanId: String): Flow<List<FindingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFindings(findings: List<FindingEntity>)

    @Query("DELETE FROM findings")
    suspend fun clearFindings()
}

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items ORDER BY dateAdded DESC")
    fun getAllVaultItemsFlow(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items ORDER BY dateAdded DESC")
    suspend fun getAllVaultItems(): List<VaultItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteVaultItem(id: String)

    @Query("DELETE FROM vault_items")
    suspend fun clearVault()
}
