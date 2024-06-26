package com.softteco.template.data.base

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.softteco.template.data.base.dao.ThermometerDataDao
import com.softteco.template.data.base.dao.DevicesDao
import com.softteco.template.data.base.model.DeviceDb
import com.softteco.template.data.base.model.ThermometerDataDb
import com.softteco.template.data.base.model.ThermometerValuesDb

@Database(
    entities = [
        DeviceDb::class,
        ThermometerDataDb::class,
        ThermometerValuesDb::class
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

    abstract fun devicesDao(): DevicesDao

    abstract fun thermometerDataDao(): ThermometerDataDao
}
