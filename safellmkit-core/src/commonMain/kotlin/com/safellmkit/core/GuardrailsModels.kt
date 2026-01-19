package com.safellmkit.core

import com.safellmkit.core.compliance.OwaspLlmTop10
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class GuardrailAction {
    ALLOW,
    SANITIZE,
    BLOCK
}

@Serializable
data class GuardrailFinding(
    val category: String,
    val rule: String,
    val severity: Int, // 1..10
    val message: String,
    val owaspMapping: OwaspLlmTop10? = null
)

@Serializable
data class GuardrailResult(
    val action: GuardrailAction,
    val riskScore: Int, // 0..100
    val safeText: String? = null,
    val findings: List<GuardrailFinding>,
    val messageToUser: String
) {
    fun toExplainabilityReport(): String {
        val json = Json { prettyPrint = true }
        return json.encodeToString(this)
    }
}
