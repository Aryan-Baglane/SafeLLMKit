package com.safellmkit.core.rules


import com.safellmkit.core.GuardrailFinding
import com.safellmkit.core.config.JailbreakSignalsConfig

class SignalJailbreakRule(
    private val config: JailbreakSignalsConfig
) : Rule {

    override fun name() = "SIGNAL_JAILBREAK_RULE"
    override fun category() = "PROMPT_INJECTION"

    override fun check(input: String): List<GuardrailFinding> {
        val lower = input.lowercase()

        var score = 0
        val triggered = mutableListOf<String>()

        for (signal in config.signals) {
            val match = signal.patterns.any { lower.contains(it.lowercase()) }
            if (match) {
                score += signal.weight
                triggered.add(signal.id)
            }
        }

        if (triggered.isEmpty()) return emptyList()

        val severity = when {
            score >= config.thresholds.block -> 10
            score >= config.thresholds.sanitize -> 7
            else -> 4
        }

        return listOf(
            GuardrailFinding(
                category = category(),
                rule = name(),
                severity = severity,
                message = "Jailbreak signals detected: ${triggered.joinToString()} (score=$score)"
            )
        )
    }
}
