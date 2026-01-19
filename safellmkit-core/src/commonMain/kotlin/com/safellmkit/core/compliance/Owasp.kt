package com.safellmkit.core.compliance

enum class OwaspLlmTop10(val code: String, val title: String) {
    LLM01("LLM01", "Prompt Injection"),
    LLM02("LLM02", "Insecure Output Handling"),
    LLM03("LLM03", "Training Data Poisoning"),
    LLM04("LLM04", "Model Denial of Service"),
    LLM05("LLM05", "Supply Chain Vulnerabilities"),
    LLM06("LLM06", "Sensitive Information Disclosure"),
    LLM07("LLM07", "Insecure Plugin Design"),
    LLM08("LLM08", "Excessive Agency"),
    LLM09("LLM09", "Overreliance"),
    LLM10("LLM10", "Model Theft");

    companion object {
        fun fromCode(code: String): OwaspLlmTop10? = entries.find { it.code == code }
    }
}
