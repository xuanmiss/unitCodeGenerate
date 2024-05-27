package com.miss.unitcodegenerate.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.nio.charset.Charset
import java.util.stream.Collectors


class UnitCodeGenerateService {

    private val client = OkHttpClient()
    private val gson = Gson()

    companion object {
        private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
    }

    fun generateUnitTestCode(javaPath: String) {

        val javaCode = readFileToString(javaPath)

        val json = gson.toJson(mapOf("javaCode" to javaCode))
        val requestBody = json.toRequestBody(JSON_MEDIA_TYPE.toMediaTypeOrNull())


        val request = Request.Builder()
            .post(requestBody)
            .url("http://localhost:8080/javaParse/methodBodyByCode")
            .build()
        val response = client.newCall(request).execute()
        response.body
        if (response.isSuccessful) {
            val body = response.body?.string()
            val typeToken = object : TypeToken<Map<String, Any>>() {}.type
            val fromJson = gson.fromJson<Map<String, Any>>(body, typeToken)
            val className = fromJson.get("className")
            val codeList = fromJson.get("codeList") as List<String>
            val responseMutableList = codeList.stream().map {
                val jsonBody = gson.toJson(mapOf("className" to className, "code" to it))
                val codeAndClassName = jsonBody.toRequestBody(JSON_MEDIA_TYPE.toMediaTypeOrNull())
                val generateRequest = Request.Builder()
                    .post(codeAndClassName)
                    .url("http://localhost:5000/unit-test/testCode")
                    .build()
                client.newCall(generateRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        println("请求失败，响应码: ${response.code}")
                        return@use null // 或者你可以选择抛出异常
                    }
                    response.body?.string()
                }
            }
                .filter { it != null }
                .collect(Collectors.toList())
            val codeListBody = gson.toJson(responseMutableList)
            val codeListRequestBody = codeListBody.toRequestBody(JSON_MEDIA_TYPE.toMediaTypeOrNull())
            val mergedTestCodeRequest = Request.Builder()
                .post(codeListRequestBody)
                .url("http://localhost:8080/javaParse/mergeFiles")
                .build()
            client.newCall(mergedTestCodeRequest).execute().use { mergedTestCodeMapResponse ->
                if (!mergedTestCodeMapResponse.isSuccessful) {
                    println("请求失败，响应码: ${mergedTestCodeMapResponse.code}")
                    return@use null // 或者你可以选择抛出异常
                }
                mergedTestCodeMapResponse.body?.string()
                gson.fromJson<Map<String, Any>>(body, object : TypeToken<Map<String, Any>>() {}.type)
            }.let {
                println(it)
            }


        } else {
            println("Request failed with code: ${response.code}")
        }
    }

    private fun readFileToString(filePath: String, charset: Charset = Charsets.UTF_8): String {
        val file = File(filePath)
        return file.readText(charset)
    }


}