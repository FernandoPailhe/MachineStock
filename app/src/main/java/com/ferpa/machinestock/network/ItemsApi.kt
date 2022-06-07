package com.ferpa.machinestock.network

import com.ferpa.machinestock.model.Item
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ItemsApi {

    @GET("/getItemById")
    suspend fun getItemById(@Body itemId: Long): Item

    @GET("/getAllItems")
    suspend fun getItems(@Query ("product") product: String, @Query ("value") value: String) : List<Item>

    @GET("/getAllItems")
    suspend fun getAllItems(): Response<List<Item>>

    @POST("/newItem")
    suspend fun postNewItem(@Body item: Item): Call<*>

    @POST("/updateItem")
    suspend fun updateItem(@Body item: Item): Call<*>

    @Multipart
    @POST
    suspend fun postNewPhoto(@Part image:MultipartBody.Part):Call<*>

    companion object {
        const val BASE_LOCAL_URL = "http://192.168.100.4:8080"
        const val BASE_ORACLE_URL = "http://129.151.106.230"
    }


}