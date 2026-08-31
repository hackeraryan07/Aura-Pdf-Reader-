package com.example.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AiApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchModels(provider: String, apiKey: String): Result<List<Pair<String, String>>> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) throw Exception("API Key is empty")
            
            val models = mutableListOf<Pair<String, String>>()
            
            when (provider) {
                "Gemini" -> {
                    val request = Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
                        .build()
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val jsonObject = JSONObject(body)
                        val modelsArray = jsonObject.getJSONArray("models")
                        for (i in 0 until modelsArray.length()) {
                            val model = modelsArray.getJSONObject(i)
                            val name = model.optString("name").removePrefix("models/")
                            val displayName = model.optString("displayName", name)
                            if (name.contains("gemini", ignoreCase = true)) {
                                models.add(displayName to name)
                            }
                        }
                    } else {
                        throw Exception(parseError(body) ?: "HTTP ${response.code}")
                    }
                }
                "OpenAI" -> {
                    val request = Request.Builder()
                        .url("https://api.openai.com/v1/models")
                        .addHeader("Authorization", "Bearer $apiKey")
                        .build()
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val jsonObject = JSONObject(body)
                        val dataArray = jsonObject.getJSONArray("data")
                        for (i in 0 until dataArray.length()) {
                            val model = dataArray.getJSONObject(i)
                            val id = model.getString("id")
                            if (id.startsWith("gpt-") || id.startsWith("o1-") || id.startsWith("o3-")) {
                                models.add(id to id)
                            }
                        }
                    } else {
                        throw Exception(parseError(body) ?: "HTTP ${response.code}")
                    }
                }
                else -> throw Exception("Provider not supported for auto-fetch")
            }
            Result.success(models.sortedBy { it.second }.reversed())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testApi(provider: String, apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) throw Exception("API Key is empty")
            
            when (provider) {
                "Gemini" -> {
                    val request = Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        Result.success("Connection successful!")
                    } else {
                        val body = response.body?.string()
                        throw Exception(parseError(body) ?: "API Error: ${response.code}")
                    }
                }
                "OpenAI" -> {
                    val request = Request.Builder()
                        .url("https://api.openai.com/v1/models")
                        .addHeader("Authorization", "Bearer $apiKey")
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        Result.success("Connection successful!")
                    } else {
                        val body = response.body?.string()
                        throw Exception(parseError(body) ?: "API Error: ${response.code}")
                    }
                }
                else -> throw Exception("Cannot test Custom/None provider")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseError(body: String?): String? {
        if (body == null) return null
        return try {
            val json = JSONObject(body)
            if (json.has("error")) {
                val error = json.get("error")
                if (error is JSONObject) {
                    error.optString("message")
                } else if (error is String) {
                    error
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
