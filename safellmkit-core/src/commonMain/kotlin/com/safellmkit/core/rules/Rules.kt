package com.safellmkit.core.rules

import com.safellmkit.core.GuardrailFinding

interface Rule {
    fun name(): String
    fun category(): String
    fun check(input: String): List<GuardrailFinding>
    fun sanitize(input: String): String = input
}
