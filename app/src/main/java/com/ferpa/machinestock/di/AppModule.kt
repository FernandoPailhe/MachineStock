package com.ferpa.machinestock.di

import android.content.Context
import com.ferpa.machinestock.data.MachineDao
import com.ferpa.machinestock.data.MachinesRepository
import com.ferpa.machinestock.utilities.CustomListUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        machineDao: MachineDao,
        customListUtil: CustomListUtil,
        @ApplicationContext appContext: Context
        ) : MachinesRepository {
        return MachinesRepository(machineDao, customListUtil, appContext)
    }

 }
