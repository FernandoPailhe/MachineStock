package com.ferpa.machinestock.network

import com.ferpa.machinestock.model.Item
import retrofit2.http.GET

interface ItemsApi {

    @GET("/testItem")
    suspend fun getTestItem(): Item


    companion object {
        const val BASE_URL = "http://192.168.100.4:8080"
    }



}