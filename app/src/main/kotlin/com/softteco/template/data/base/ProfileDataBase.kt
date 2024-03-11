package com.softteco.template.data.base

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.softteco.template.data.base.dao.BluetoothDeviceDao
import com.softteco.template.data.base.dao.MeasurementDao
import com.softteco.template.data.base.model.BluetoothDeviceDb
import com.softteco.template.data.base.model.MeasurementDb

@Database(
    entities = [
        BluetoothDeviceDb::class,
        MeasurementDb::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ProfileDataBase : RoomDatabase() {

    companion object {

        private const val DATABASE_NAME = "ProfileDataBase"

        @Volatile
        private var INSTANCE: ProfileDataBase? = null

        fun getInstance(context: Context): ProfileDataBase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it
                }
            }

        private fun buildDatabase(context: Context): ProfileDataBase {
            return Room.databaseBuilder(
                context.applicationContext,
                ProfileDataBase::class.java,
                DATABASE_NAME
            )
                .allowMainThreadQueries()
                .build()
        }
    }

    abstract fun bluetoothDevicesDao(): BluetoothDeviceDao

    abstract fun measurementDao(): MeasurementDao
}
