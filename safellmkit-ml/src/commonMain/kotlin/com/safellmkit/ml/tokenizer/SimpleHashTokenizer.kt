package com.safellmkit.ml.tokenizer

/**
 * Simple tokenizer for MVP classification models trained using the same approach.
 *
 * ✅ Works if you train your model using this same hashing/token strategy.
 * ❌ Not compatible with pretrained BERT ONNX models.
 */
class SimpleHashTokenizer(
    private val vocabSize: Int = 8192
) : Tokenizer {

    override fun tokenize(text: String, maxLen: Int): TokenizedInput {
        val cleaned = text.lowercase()
            .replace(Regex("""[^a-z0-9\s]"""), " ")
            .trim()

        val words = cleaned.split(Regex("\\s+")).filter { it.isNotBlank() }

        val ids = LongArray(maxLen) { 0L }
        val mask = LongArray(maxLen) { 0L }

        val count = minOf(words.size, maxLen)

        for (i in 0 until count) {
            val h = words[i].hashCode()
            val idx = (kotlin.math.abs(h) % vocabSize) + 1 // reserve 0 for pad
            ids[i] = idx.toLong()
            mask[i] = 1L
        }

        return TokenizedInput(
            inputIds = ids,
            attentionMask = mask
        )
    }
}
