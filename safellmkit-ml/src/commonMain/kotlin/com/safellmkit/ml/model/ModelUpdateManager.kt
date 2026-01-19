package com.safellmkit.ml.model

/**
 * Handles model versioning & updates.
 * Actual network download must be handled by platform implementation.
 */
class ModelUpdateManager(
    private val store: ModelStore
) {
    fun ensureModel(modelName: String, fallbackBytes: ByteArray? = null): String {
        if (!store.exists(modelName)) {
            if (fallbackBytes != null) {
                store.save(modelName, fallbackBytes)
            } else {
                throw IllegalStateException("Model $modelName not found and no fallback provided.")
            }
        }
        return store.getModelPath(modelName)
    }
}
