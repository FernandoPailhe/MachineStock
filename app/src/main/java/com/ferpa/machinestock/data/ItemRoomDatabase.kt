package com.ferpa.machinestock.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.MachineStockUser
import com.ferpa.machinestock.model.MainMenuPreferences

@Database(
    version = 4,
    entities = [
        Item::class,
        MachineStockUser::class,
        MainMenuPreferences::class
    ],
    autoMigrations = [
        AutoMigration (from = 2, to = 4),
        AutoMigration (from = 3, to = 4)
    ],
    exportSchema = true
)

abstract class ItemRoomDatabase : RoomDatabase() {

    abstract fun machineDao(): MachineDao

    companion object {
        const val DATABASE_NAME = "machine_stock_db"
    }

}