package com.safellmkit.core

import com.safellmkit.core.policy.GuardrailStage
import com.safellmkit.core.policy.GuardrailsPolicies
import com.safellmkit.core.policy.GuardrailsPolicy
import com.safellmkit.core.policy.RulePolicy
import kotlin.math.min

class GuardrailsEngine(
    private val policy: GuardrailsPolicy = GuardrailsPolicies.strict()
) {

    fun validateInput(input: String): GuardrailResult =
        validate(stage = GuardrailStage.INPUT, text = input)

    fun validateOutput(output: String): GuardrailResult =
        validate(stage = GuardrailStage.OUTPUT, text = output)

    private fun validate(stage: GuardrailStage, text: String): GuardrailResult {
        val rules = when (stage) {
            GuardrailStage.INPUT -> policy.inputRules
            GuardrailStage.OUTPUT -> policy.outputRules
        }

        val findings = rules.flatMap { rp ->
            rp.rule.check(text).filter { it.severity >= rp.minSeverityToTrigger }
        }

        val riskScore = computeRisk(findings)

        // Decide action using strongest rule mode triggered
        val action = decideAction(rules, findings)

        val safeText = when (action) {
            GuardrailAction.ALLOW -> text
            GuardrailAction.SANITIZE -> sanitizeAll(text, rules)
            GuardrailAction.BLOCK -> null
        }

        val msg = when (action) {
            GuardrailAction.ALLOW -> "Allowed ✅"
            GuardrailAction.SANITIZE -> "Sanitized ⚠️ Guardrails detected risky/sensitive content."
            GuardrailAction.BLOCK -> "Blocked 🚫 Guardrails policy rejected this content."
        }

        return GuardrailResult(
            action = action,
            riskScore = riskScore,
            safeText = safeText,
            findings = findings,
            messageToUser = msg
        )
    }

    private fun sanitizeAll(text: String, rules: List<RulePolicy>): String {
        var safe = text
        rules.forEach { rp ->
            safe = rp.rule.sanitize(safe)
        }
        return safe
    }

    private fun decideAction(rules: List<RulePolicy>, findings: List<GuardrailFinding>): GuardrailAction {
        if (findings.isEmpty()) return GuardrailAction.ALLOW

        // If any rule is configured to block and it triggered → BLOCK
        val blocked = rules.any { rp ->
            rp.mode == com.safellmkit.core.policy.GuardrailMode.BLOCK_IF_FOUND &&
                    findings.any { it.rule == rp.rule.name() }
        }
        if (blocked) return GuardrailAction.BLOCK

        // Else sanitize if any sanitize rule triggered
        val sanitize = rules.any { rp ->
            rp.mode == com.safellmkit.core.policy.GuardrailMode.SANITIZE_IF_FOUND &&
                    findings.any { it.rule == rp.rule.name() }
        }
        if (sanitize) return GuardrailAction.SANITIZE

        return GuardrailAction.ALLOW
    }

    private fun computeRisk(findings: List<GuardrailFinding>): Int {
        val base = findings.sumOf { it.severity * 10 }
        return min(base, 100)
    }
}
