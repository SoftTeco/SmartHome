package com.softteco.template.data.base.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.softteco.template.data.base.model.MeasurementDb
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(measurement: MeasurementDb): Long

    @Query("select * from measurements")
    fun getList(): Flow<List<MeasurementDb>>

    @Query("DELETE FROM measurements WHERE guid = :guid")
    fun delete(guid: String): Int
}
