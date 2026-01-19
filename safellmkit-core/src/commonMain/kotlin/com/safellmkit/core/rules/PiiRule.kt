package com.safellmkit.core.rules

import com.safellmkit.core.GuardrailFinding

class PiiRule : Rule {

    private val emailRegex = Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}""")
    private val phoneRegex = Regex("""\b(\+?\d{1,3}[- ]?)?\d{10}\b""")

    override fun name() = "PII_V1"
    override fun category() = "PII"

    override fun check(input: String): List<GuardrailFinding> {
        val findings = mutableListOf<GuardrailFinding>()

        if (emailRegex.containsMatchIn(input)) {
            findings.add(
                GuardrailFinding(
                    category = category(),
                    rule = name(),
                    severity = 6,
                    message = "Email address detected.",
                    owaspMapping = com.safellmkit.core.compliance.OwaspLlmTop10.LLM06
                )
            )
        }

        if (phoneRegex.containsMatchIn(input)) {
            findings.add(
                GuardrailFinding(
                    category = category(),
                    rule = name(),
                    severity = 6,
                    message = "Phone number detected.",
                    owaspMapping = com.safellmkit.core.compliance.OwaspLlmTop10.LLM06
                )
            )
        }

        return findings
    }

    override fun sanitize(input: String): String {
        var safe = input
        safe = safe.replace(emailRegex, "[REDACTED_EMAIL]")
        safe = safe.replace(phoneRegex, "[REDACTED_PHONE]")
        return safe
    }
}
