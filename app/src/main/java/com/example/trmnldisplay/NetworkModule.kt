package com.example.trmnldisplay

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT_SECONDS = 15L
private const val READ_TIMEOUT_SECONDS = 30L
private const val BASE_URL = "https://usetrmnl.com/"

/**
 * Singleton OkHttpClient instance shared across the application.
 */
val appOkHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .build()

/**
 * Retrofit interface for TRMNL API.
 */
interface TrmnlApi {
    @GET("api/display")
    suspend fun getDisplay(
        @Header("access-token") apiKey: String,
        @Header("id") macAddress: String,
        @Header("battery-voltage") batteryVoltage: Float = 5.0f,
        @Header("wifi-rssi") wifiRssi: Int = -50,
        @Header("fw-version") fwVersion: String = "1.0.0"
    ): TrmnlResponse
}

/**
 * Data model for the TRMNL API response.
 */
data class TrmnlResponse(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("refresh_rate") val refreshRateSeconds: Long?,
    @SerializedName("reset_duration") val resetDurationMinutes: Long?
)

/**
 * Singleton Retrofit instance.
 */
private val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(appOkHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

/**
 * Singleton TrmnlApi instance.
 */
val trmnlApi: TrmnlApi = retrofit.create(TrmnlApi::class.java)
