package safellmkit.core.config
import kotlinx.serialization.Serializable

@Serializable
data class JailbreakSignal(
    val id: String,
    val weight: Int,
    val patterns: List<String>
)

@Serializable
data class JailbreakThresholds(
    val block: Int,
    val sanitize: Int
)

@Serializable
data class JailbreakSignalsConfig(
    val version: String,
    val thresholds: JailbreakThresholds,
    val signals: List<JailbreakSignal>
)
