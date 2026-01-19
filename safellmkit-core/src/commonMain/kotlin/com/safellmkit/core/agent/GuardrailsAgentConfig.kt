package com.safellmkit.core.agent

/**
 * Controls how ML scoring is used.
 */
data class GuardrailsAgentConfig(
    val enableMlFallback: Boolean = true,
    val mlRiskThresholdBlock: Float = 0.85f,
    val mlRiskThresholdSanitize: Float = 0.55f,
    val rulesRiskThresholdToAskMl: Int = 35 // if rules score >= this, call ML for confirmation
)
