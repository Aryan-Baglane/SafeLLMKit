package com.safellmkit.core.remote

import com.safellmkit.core.config.JailbreakSignalsConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

interface SignatureVerifier {
    fun verify(data: ByteArray, signature: String, publicKey: String): Boolean
}

class RemoteConfigLoader(
    private val client: HttpClient,
    private val verifier: SignatureVerifier,
    private val publicKey: String
) {
    suspend fun loadJailbreakConfig(url: String, signatureHeader: String = "X-Signature"): JailbreakSignalsConfig {
        val response = client.get(url)
        val bytes = response.readBytes()
        val signature = response.headers[signatureHeader]
            ?: throw IllegalArgumentException("Missing signature header: $signatureHeader")

        if (!verifier.verify(bytes, signature, publicKey)) {
            throw RuntimeException("Invalid config signature. Potential tampering detected.")
        }

        val jsonString = bytes.decodeToString()
        return Json.decodeFromString(jsonString)
    }
}
