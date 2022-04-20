package com.ferpa.machinestock.di

import android.content.Context
import androidx.room.Room
import com.ferpa.machinestock.data.ItemDao
import com.ferpa.machinestock.data.ItemRoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideItemDatabase(@ApplicationContext context: Context): ItemRoomDatabase {
        return Room.databaseBuilder(
            context,
            ItemRoomDatabase::class.java,
            ItemRoomDatabase.DATABASE_NAME
        )
            .createFromAsset(ItemRoomDatabase.DATABASE_ASSET)
            .build()
    }

    @Provides
    @Singleton
    fun provideItemDao(itemRoomDatabase: ItemRoomDatabase) : ItemDao{
        return itemRoomDatabase.itemDao()
    }


}