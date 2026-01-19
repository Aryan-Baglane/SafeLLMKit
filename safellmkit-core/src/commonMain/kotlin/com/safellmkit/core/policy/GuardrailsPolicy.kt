package com.safellmkit.core.policy

import com.safellmkit.core.rules.Rule

data class RulePolicy(
    val rule: Rule,
    val mode: GuardrailMode,
    val minSeverityToTrigger: Int = 1 // 1..10
)

data class GuardrailsPolicy(
    val inputRules: List<RulePolicy>,
    val outputRules: List<RulePolicy>
)
