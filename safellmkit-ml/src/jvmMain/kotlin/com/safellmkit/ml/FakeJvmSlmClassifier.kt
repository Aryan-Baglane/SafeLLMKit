package com.safellmkit.ml

/**
 * JVM mock classifier to test the pipeline.
 * Replace later with real ONNX inference.
 */
class FakeJvmSlmClassifier : SlmClassifier {

    private val jailbreakKeywords = listOf(
        "ignore previous instructions",
        "reveal system prompt",
        "do anything now",
        "bypass policy",
        "jailbreak"
    )

    override suspend fun predict(prompt: String): SlmResult {
        val lower = prompt.lowercase()
        val hit = jailbreakKeywords.any { lower.contains(it) }

        return if (hit) {
            SlmResult(label = "JAILBREAK", probability = 0.92f, confidence = 0.92f)
        } else {
            SlmResult(label = "SAFE", probability = 0.08f, confidence = 0.60f)
        }
    }
}
