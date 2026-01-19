package com.safellmkit.core.policy

import com.safellmkit.core.GuardrailAction

/**
 * How to treat rule matches.
 */
enum class GuardrailMode {
    ALLOW_ONLY,
    SANITIZE_IF_FOUND,
    BLOCK_IF_FOUND
}

fun GuardrailMode.toAction(): GuardrailAction = when (this) {
    GuardrailMode.ALLOW_ONLY -> GuardrailAction.ALLOW
    GuardrailMode.SANITIZE_IF_FOUND -> GuardrailAction.SANITIZE
    GuardrailMode.BLOCK_IF_FOUND -> GuardrailAction.BLOCK
}
