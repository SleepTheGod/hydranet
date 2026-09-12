package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AuditDao
import com.example.data.dao.FindingDao
import com.example.data.dao.ScopeDao
import com.example.data.dao.VaultDao
import com.example.data.model.AuditEntryEntity
import com.example.data.model.FindingEntity
import com.example.data.model.ScopeEntity
import com.example.data.model.VaultItemEntity

@Database(
    entities = [
        ScopeEntity::class,
        AuditEntryEntity::class,
        FindingEntity::class,
        VaultItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HydraDatabase : RoomDatabase() {
    abstract fun scopeDao(): ScopeDao
    abstract fun auditDao(): AuditDao
    abstract fun findingDao(): FindingDao
    abstract fun vaultDao(): VaultDao

    companion object {
        @Volatile
        private var INSTANCE: HydraDatabase? = null

        fun getInstance(context: Context): HydraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HydraDatabase::class.java,
                    "hydranet.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
