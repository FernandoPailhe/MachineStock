package com.ferpa.machinestock.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ferpa.machinestock.model.Item

@Database (entities = [Item::class], version = 1, exportSchema = true)
abstract class ItemRoomDatabase : RoomDatabase () {

    abstract fun itemDao(): ItemDao


    companion object {
        const val DATABASE_NAME = "used_item_database"
        const val DATABASE_ASSET = "database/used_machines.sqlite"
    }

    /** This is no longer necessary with Hilt
    companion object {

        @Volatile
        private var INSTANCE: ItemRoomDatabase? = null

        fun getDatabase(context: Context): ItemRoomDatabase{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ItemRoomDatabase::class.java,
                    "used_item_database"
                )
                    .createFromAsset("database/used_machines.sqlite")
                    .build()
                INSTANCE = instance
                return instance
            }
        }

    }
    **/

}