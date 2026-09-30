package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettingsEntity)

    @Update
    suspend fun update(settings: AppSettingsEntity)

    @Query("UPDATE app_settings SET nextInvoiceSeq = nextInvoiceSeq + 1 WHERE id = 1")
    suspend fun incrementInvoiceSeq()

    @Query("UPDATE app_settings SET nextProductSeq = nextProductSeq + 1 WHERE id = 1")
    suspend fun incrementProductSeq()

    @Query("UPDATE app_settings SET nextCustomerSeq = nextCustomerSeq + 1 WHERE id = 1")
    suspend fun incrementCustomerSeq()

    @Query("UPDATE app_settings SET nextSupplierSeq = nextSupplierSeq + 1 WHERE id = 1")
    suspend fun incrementSupplierSeq()
}
