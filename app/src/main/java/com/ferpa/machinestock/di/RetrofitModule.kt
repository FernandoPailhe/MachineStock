package com.ferpa.machinestock.di

import com.ferpa.machinestock.network.ItemsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    @Singleton
    fun provideItemsApi(): ItemsApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ItemsApi.BASE_LOCAL_URL)
            .build()
            .create(ItemsApi::class.java)
    }

}