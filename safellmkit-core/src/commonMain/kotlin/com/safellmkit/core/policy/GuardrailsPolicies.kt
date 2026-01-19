package com.safellmkit.core.policy

import com.safellmkit.core.rules.PiiRule
import com.safellmkit.core.rules.PromptInjectionRule
import com.safellmkit.core.rules.Rule
import com.safellmkit.core.rules.ToxicityRule

object GuardrailsPolicies {

    fun strict(
        promptInjectionRule: Rule = PromptInjectionRule()
    ): GuardrailsPolicy {
        return GuardrailsPolicy(
            inputRules = listOf(
                RulePolicy(
                    rule = promptInjectionRule,
                    mode = GuardrailMode.BLOCK_IF_FOUND,
                    minSeverityToTrigger = 8
                ),
                RulePolicy(
                    rule = PiiRule(),
                    mode = GuardrailMode.SANITIZE_IF_FOUND,
                    minSeverityToTrigger = 1
                ),
                RulePolicy(
                    rule = ToxicityRule(),
                    mode = GuardrailMode.SANITIZE_IF_FOUND,
                    minSeverityToTrigger = 1
                )
            ),
            outputRules = listOf(
                RulePolicy(
                    rule = PiiRule(),
                    mode = GuardrailMode.SANITIZE_IF_FOUND,
                    minSeverityToTrigger = 1
                )
            )
        )
    }
}
