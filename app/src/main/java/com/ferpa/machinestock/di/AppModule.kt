package com.ferpa.machinestock.di

import com.ferpa.machinestock.data.ItemDao
import com.ferpa.machinestock.data.ItemRepository
import com.ferpa.machinestock.utilities.CustomListUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)//With this, all dependencies inject here will be alive at all application
object AppModule {

    @Provides
    @Singleton
    fun provideCustomListUtil(): CustomListUtil{
        return CustomListUtil()
    }

    @Provides
    @Singleton
    fun provideItemRepository(
            itemDao: ItemDao,
            customListUtil: CustomListUtil
        ) : ItemRepository {
        return ItemRepository(itemDao, customListUtil)
    }

 }
