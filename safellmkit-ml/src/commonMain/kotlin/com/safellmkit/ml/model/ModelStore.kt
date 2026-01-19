package com.safellmkit.ml.model

/**
 * Platform-safe model store interface.
 * - Android: store in app filesDir
 * - iOS: store in Documents/Library
 * - JVM: store in ~/.safellmkit/models
 */
interface ModelStore {
    fun getModelPath(modelName: String): String
    fun exists(modelName: String): Boolean
    fun save(modelName: String, bytes: ByteArray)
}
