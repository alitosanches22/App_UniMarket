package com.example.aplicacion_unimarket

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    private val baseUrls = listOf(
        "http://10.0.2.2:3000/",
        "http://127.0.0.1:3000/"
    )

    val servicios: List<UniMarketApiService> by lazy {
        baseUrls.map { baseUrl ->
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UniMarketApiService::class.java)
        }
    }

    val servicio: UniMarketApiService
        get() = servicios.first()
}
