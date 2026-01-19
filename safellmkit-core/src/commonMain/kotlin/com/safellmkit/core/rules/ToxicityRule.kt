package com.safellmkit.core.rules

import com.safellmkit.core.GuardrailFinding

class ToxicityRule : Rule {

    private val bannedWords = listOf(
        "idiot",
        "stupid",
        "dumb",
        "hate you"
    )

    override fun name() = "TOXICITY_V1"
    override fun category() = "TOXICITY"

    override fun check(input: String): List<GuardrailFinding> {
        val lower = input.lowercase()
        val hits = bannedWords.filter { lower.contains(it) }

        return hits.map { hit ->
            GuardrailFinding(
                category = category(),
                rule = name(),
                severity = 4,
                message = "Toxic content detected: \"$hit\""
            )
        }
    }

    override fun sanitize(input: String): String {
        var safe = input
        bannedWords.forEach { w ->
            safe = safe.replace(w, "***", ignoreCase = true)
        }
        return safe
    }
}
