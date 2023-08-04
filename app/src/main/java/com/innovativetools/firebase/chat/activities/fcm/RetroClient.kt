package com.innovativetools.firebase.chat.activities.fcm

import retrofit2.Retrofit
import com.innovativetools.firebase.chat.activities.fcm.RetroClient
import retrofit2.converter.gson.GsonConverterFactory

object RetroClient {
    private var retrofit: Retrofit? = null
    @JvmStatic
    fun getClient(url: String?): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }
}