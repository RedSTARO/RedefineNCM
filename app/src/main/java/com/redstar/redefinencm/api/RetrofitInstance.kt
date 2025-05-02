package com.redstar.redefinencm.api

import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.util.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val BASE_URL = getBaseUrl()
    private val REAL_IP = getRealIP()
    private val COOKIE = getCookie() // Use cleaned cookie
    private val NO_COOKIE_URLS = listOf("login")
    private val TIMESTAMP_URLS = listOf("login")

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val originalUrl = original.url

            // Build new URL with realIP parameter
            var newUrl = originalUrl.newBuilder()
                .addQueryParameter("realIP", REAL_IP)
                .build()

            // Add timestamp parameter for specific URLs
            if (TIMESTAMP_URLS.any { originalUrl.encodedPath.contains(it) }) {
                newUrl = newUrl.newBuilder()
                    .addQueryParameter("timestamp", System.currentTimeMillis().toString())
                    .build()
            }

            // Build the new request, conditionally adding the Cookie header
            val requestBuilder = original.newBuilder().url(newUrl)
            if (!NO_COOKIE_URLS.any { originalUrl.encodedPath.contains(it) }) {
                requestBuilder.addHeader("Cookie", COOKIE)
            }

            // Build the final request
            val finalRequest = requestBuilder.build()

            // Log URL and headers in debug mode
            if (BuildConfig.DEBUG) {
                Log.d("RetrofitInstance", "URL: ${finalRequest.url}")
//                Log.d("RetrofitInstance", "Header Cookie: ${finalRequest.header("Cookie")}")
            }

            // Proceed with the modified request
            chain.proceed(finalRequest)
        }
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
}

fun getRealIP(): String {
    return "192.168.1.1"
}

fun getCookie(): String {
    val rawCookie = runBlocking {
        DataStoreManager.getAppDataStore().data.first()[stringPreferencesKey("cookie")] ?: ""
    }
    if (rawCookie.isEmpty()) return ""

    // Split the cookie string by semicolons and extract name=value pairs
    val cleanCookies = rawCookie.split(";")
        .map { it.trim() }
        .filter { it.contains("=") }.mapNotNull { part ->
            // Extract only the name=value part, ignoring attributes
            val nameValue = part.substringBefore(";").trim()
            if (nameValue.isNotEmpty()) nameValue else null
        }
        .joinToString("; ")

    return cleanCookies
}

fun getBaseUrl(): String {
    return runBlocking {
        DataStoreManager.getAppDataStore().data.first()[stringPreferencesKey("server")] ?: ""
    }
}