package com.redstar.redefinencm.server

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.BufferedReader
import java.io.InputStreamReader

class ApiServer(private val context: Context) : NanoHTTPD(8080) {

    override fun serve(session: IHTTPSession): Response {
        val path = session.uri.replace("/", "_").removeSuffix("_")
        val fileName = "$path.json"
        Log.i("MyApiServer", "Received $path: $fileName")

        return try {
            val assetManager = context.assets
            val inputStream = assetManager.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.use { it.readText() }
            newFixedLengthResponse(Response.Status.OK, "application/json", content)
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Mock response not found for: ${session.uri}")
        }
    }
}
