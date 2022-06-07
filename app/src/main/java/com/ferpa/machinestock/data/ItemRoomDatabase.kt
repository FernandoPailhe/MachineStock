package com.ferpa.machinestock.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ferpa.machinestock.model.Item

@Database (entities = [Item::class], version = 1, exportSchema = false)
abstract class ItemRoomDatabase : RoomDatabase () {

    abstract fun itemDao(): ItemDao

    companion object {
        const val DATABASE_NAME = "used_item_database"
        const val DATABASE_ASSET = "database/used_machines.sqlite"
    }

}