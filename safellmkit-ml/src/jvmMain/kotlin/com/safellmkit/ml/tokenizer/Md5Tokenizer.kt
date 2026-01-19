package com.safellmkit.ml.tokenizer

import java.security.MessageDigest
import java.math.BigInteger

/**
 * Kotlin implementation of the STABLE MD5 tokenizer.
 * This must match the Python function:
 * 
 * def stable_hash(word: str) -> int:
 *    return int(hashlib.md5(word.encode("utf-8")).hexdigest(), 16)
 */
class Md5Tokenizer(
    private val maxLen: Int = 64,
    private val vocabSize: Int = 8192
) : Tokenizer {

    override fun tokenize(text: String, maxLen: Int): TokenizedInput {
        val normalized = text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .trim()

        val words = normalized.split(Regex("\\s+")).filter { it.isNotEmpty() }
        
        val inputIds = LongArray(maxLen) { 0 }
        val attentionMask = LongArray(maxLen) { 0 }

        val count = minOf(words.size, maxLen)
        val vocabSizeBig = BigInteger.valueOf(vocabSize.toLong())

        for (i in 0 until count) {
            val h = getHashBigInt(words[i])
            val idx = h.mod(vocabSizeBig).toInt() + 1 // BigInteger.mod returns positive remainder
            inputIds[i] = idx.toLong()
            attentionMask[i] = 1L
        }

        return TokenizedInput(inputIds, attentionMask)
    }

    private fun getHashBigInt(word: String): BigInteger {
        val md = MessageDigest.getInstance("MD5")
        val digestBytes = md.digest(word.toByteArray())
        val hexString = digestBytes.joinToString("") { "%02x".format(it) }
        val truncatedHex = if (hexString.length >= 8) hexString.substring(0, 8) else hexString
        // Interpret as positive integer
        return BigInteger(truncatedHex, 16)
    }
}
