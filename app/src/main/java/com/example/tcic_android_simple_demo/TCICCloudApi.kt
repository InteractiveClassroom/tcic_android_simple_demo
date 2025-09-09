package com.example.tcic_android_simple_demo

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.suspendCoroutine

object TCICCloudApi {
    private var secretId: String = ""
    private var secretKey: String = ""
    private var appId: Int = 0

    private val client = OkHttpClient()
    private const val HOST = "https://lcic.tencentcloudapi.com"
    private const val SERVICE = "lcic"
    private const val VERSION = "2022-08-17"
    private const val REGION = "ap-guangzhou"

    fun setConfig(secretId: String, secretKey: String, appId: Int) {
        this.secretId = secretId
        this.secretKey = secretKey
        this.appId = appId
    }

    suspend fun registerUser(): RegisterUserResponse = suspendCoroutine { continuation ->
        val action = "RegisterUser"
        val params = JSONObject().apply {
            put("SdkAppId", appId)
        }

        makeRequest(action, params) { response ->
            val result = if (response != null) {
                try {
                    val responseObj = response.getJSONObject("Response")
                    if (responseObj.has("Error")) {
                        RegisterUserResponse(
                            hasError = true,
                            errorMessage = responseObj.getJSONObject("Error").getString("Message")
                        )
                    } else {
                        RegisterUserResponse(
                            userId = responseObj.getString("UserId"),
                            token = responseObj.getString("Token")
                        )
                    }
                } catch (e: Exception) {
                    RegisterUserResponse(hasError = true, errorMessage = e.message ?: "解析响应失败")
                }
            } else {
                RegisterUserResponse(hasError = true, errorMessage = "请求失败")
            }
            continuation.resumeWith(Result.success(result))
        }
    }

    suspend fun createRoom(teacherId: String): CreateRoomResponse = suspendCoroutine { continuation ->
        val action = "CreateRoom"
        val params = JSONObject().apply {
            put("SdkAppId", appId)
            put("Name", "Demo Room ${System.currentTimeMillis()}")
            put("StartTime", (System.currentTimeMillis() / 1000) + 10)
            put("EndTime", (System.currentTimeMillis() / 1000) + 3600)
            put("TeacherId", teacherId)
            put("Resolution", 1)
            put("MaxMicNumber", 6)
            put("SubType", "videodoc")
        }

        makeRequest(action, params) { response ->
            val result = if (response != null) {
                try {
                    val responseObj = response.getJSONObject("Response")
                    if (responseObj.has("Error")) {
                        CreateRoomResponse(
                            hasError = true,
                            errorMessage = responseObj.getJSONObject("Error").getString("Message")
                        )
                    } else {
                        CreateRoomResponse(
                            roomId = responseObj.getInt("RoomId")
                        )
                    }
                } catch (e: Exception) {
                    CreateRoomResponse(hasError = true, errorMessage = e.message ?: "解析响应失败")
                }
            } else {
                CreateRoomResponse(hasError = true, errorMessage = "请求失败")
            }
            continuation.resumeWith(Result.success(result))
        }
    }

    private fun makeRequest(action: String, params: JSONObject, callback: (JSONObject?) -> Unit) {
        val timestamp = System.currentTimeMillis() / 1000
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestamp * 1000))

        val body = params.toString()
        val contentType = "application/json"

        val authorization = generateAuthorization(
            action, timestamp, date, body
        )

        val request = Request.Builder()
            .url(HOST)
            .post(body.toRequestBody(contentType.toMediaType()))
            .addHeader("Authorization", authorization)
            .addHeader("Content-Type", contentType)
            .addHeader("X-TC-Action", action)
            .addHeader("X-TC-Timestamp", timestamp.toString())
            .addHeader("X-TC-Version", VERSION)
            .addHeader("X-TC-Region", REGION)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    callback(responseBody?.let { JSONObject(it) })
                } catch (e: Exception) {
                    callback(null)
                }
            }
        })
    }

    private fun generateAuthorization(
        action: String,
        timestamp: Long,
        date: String,
        payload: String
    ): String {
        val service = "lcic"
        val host = "lcic.tencentcloudapi.com"
        val endpoint = "https://$host"
        val version = "2022-08-17"
        val algorithm = "TC3-HMAC-SHA256"

        // 获取当前时间戳（秒）
        val timestamp = System.currentTimeMillis() / 1000
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestamp * 1000))

        // ************* 步骤 1：拼接规范请求串 *************
        val httpRequestMethod = "POST"
        val canonicalUri = "/"
        val canonicalQuerystring = ""
        val ct = "application/json; charset=utf-8"
        val canonicalHeaders = "content-type:$ct\nhost:$host\nx-tc-action:${action.toLowerCase()}\n"
        val signedHeaders = "content-type;host;x-tc-action"
        val hashedRequestPayload = sha256(payload)
        val canonicalRequest = """$httpRequestMethod
$canonicalUri
$canonicalQuerystring
$canonicalHeaders
$signedHeaders
$hashedRequestPayload"""


        // ************* 步骤 2：拼接待签名字符串 *************
        val credentialScope = "$date/$service/tc3_request"
        val hashedCanonicalRequest = sha256(canonicalRequest)
        val stringToSign = """$algorithm
$timestamp
$credentialScope
$hashedCanonicalRequest"""


        // ************* 步骤 3：计算签名 *************
        val secretDate = hmacSha256("TC3$secretKey".toByteArray(), date)
        val secretService = hmacSha256(secretDate, service)
        val secretSigning = hmacSha256(secretService, "tc3_request")
        val signature = bytesToHex(hmacSha256(secretSigning, stringToSign))


        // ************* 步骤 4：拼接 Authorization *************
        val authorization = "$algorithm Credential=$secretId/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"


        return authorization
    }

    // SHA256 哈希函数
    private fun sha256(data: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(data.toByteArray())
        return bytesToHex(digest)
    }

    // HMAC-SHA256 签名函数
    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(key, "HmacSHA256")
        mac.init(secretKeySpec)
        return mac.doFinal(data.toByteArray())
    }

    // 字节数组转十六进制字符串
    private fun bytesToHex(bytes: ByteArray): String {
        val hexString = StringBuilder()
        for (byte in bytes) {
            val hex = Integer.toHexString(0xff and byte.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}

// Response classes
data class RegisterUserResponse(
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val userId: String = "",
    val token: String = ""
) {
    fun hasError() = hasError
}

data class CreateRoomResponse(
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val roomId: Int = 0
) {
    fun hasError() = hasError
}