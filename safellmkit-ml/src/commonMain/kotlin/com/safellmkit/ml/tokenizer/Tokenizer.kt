package com.safellmkit.ml.tokenizer

data class TokenizedInput(
    val inputIds: LongArray,
    val attentionMask: LongArray
)

interface Tokenizer {
    fun tokenize(text: String, maxLen: Int = 64): TokenizedInput
}
