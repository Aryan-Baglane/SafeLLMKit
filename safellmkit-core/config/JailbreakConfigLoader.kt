package safellmkit.core.config

import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.io.path.readText

object JailbreakConfigLoader {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun loadFromFile(filePath: String): JailbreakSignalsConfig {
        val text = Path(filePath).readText()
        return json.decodeFromString(JailbreakSignalsConfig.serializer(), text)
    }
}
