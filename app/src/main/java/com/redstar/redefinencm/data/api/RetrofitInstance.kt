package com.redstar.redefinencm.data.api

import android.util.Log
import android.widget.Toast
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.util.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val BASE_URL = getBaseUrl()
    private val REAL_IP = getRealIP()
    private val COOKIE = getCookie() // Use cleaned cookie
    private val NO_COOKIE_URLS = listOf("Login")
    private val TIMESTAMP_URLS = listOf("Login", "")

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
            // Proceed with the modified request
            chain.proceed(finalRequest)
        }
        .addInterceptor { chain ->
            val request = chain.request()
            val t1 = System.nanoTime()
            val response: Response = chain.proceed(request)
            val t2 = System.nanoTime()
            if (BuildConfig.DEBUG) {
                Log.d(
                    "RetrofitInstance",
                    "Received response for ${
                        response.request.url.toString().substringBefore("=")
                    } in ${(t2 - t1) / 1e6} ms"
                )
            }
            response

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
        DataStoreManager.getStringItem("cookie", "")
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
        DataStoreManager.getStringItem("server", "")
    }
}

// 公共 Retrofit 调用封装，防止异常崩溃
suspend fun <T> safeApiCall(apiCall: suspend () -> T): T? {
    return try {
        apiCall()
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                RedefineNCMApplication.getApplicationContext(),
                "Request failed: ${e.message}",
                Toast.LENGTH_SHORT,
            ).show()
        }
        Log.e("safeApiCall", "API call failed: ${e.message}", e)
        null
    }
}
