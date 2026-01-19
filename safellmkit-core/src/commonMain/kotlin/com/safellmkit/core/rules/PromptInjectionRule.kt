package com.safellmkit.core.rules

import com.safellmkit.core.GuardrailFinding

class PromptInjectionRule : Rule {

    private val patterns = listOf(
        "ignore previous instructions",
        "ignore all previous instructions",
        "disregard above instructions",
        "reveal system prompt",
        "show system prompt",
        "you are now",
        "act as developer",
        "act as system",
        "jailbreak"
    )

    override fun name() = "PROMPT_INJECTION_V1"
    override fun category() = "PROMPT_INJECTION"

    override fun check(input: String): List<GuardrailFinding> {
        val lower = input.lowercase()
        val hits = patterns.filter { lower.contains(it) }

        return hits.map { hit ->
            GuardrailFinding(
                category = category(),
                rule = name(),
                severity = 9,
                message = "Possible prompt injection phrase detected: \"$hit\"",
                owaspMapping = com.safellmkit.core.compliance.OwaspLlmTop10.LLM01
            )
        }
    }
}
