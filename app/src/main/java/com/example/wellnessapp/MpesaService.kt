package com.example.wellnessapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MpesaService {
    @GET("oauth/v1/generate?grant_type=client_credentials")
    fun getAccessToken(@Header("Authorization") auth: String): Call<AccessToken>

    @POST("mpesa/stkpush/v1/processrequest")
    fun sendSTKPush(
        @Header("Authorization") auth: String,
        @Body request: STKPushRequest
    ): Call<STKPushResponse>
}
