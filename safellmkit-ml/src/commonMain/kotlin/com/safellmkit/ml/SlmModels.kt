package com.safellmkit.ml

/**
 * Output of ML/SLM classifier.
 * We use this for jailbreak detection, not text generation.
 */
data class SlmResult(
    val label: String,              // SAFE / JAILBREAK / SUSPICIOUS
    val probability: Float,         // 0.0..1.0
    val confidence: Float = probability
)
