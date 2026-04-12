package com.habitiora.batty.data.datastore

import androidx.datastore.core.Serializer
import com.habitiora.batty.domain.model.MonitorSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

object MonitorSettingsSerializer : Serializer<MonitorSettings> {
    override val defaultValue: MonitorSettings = MonitorSettings()

    override suspend fun readFrom(input: InputStream): MonitorSettings {
        return try {
            Json.decodeFromString(
                MonitorSettings.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            Timber.e(e, "Error leyendo MonitorSettings de DataStore")
            defaultValue
        }
    }

    override suspend fun writeTo(t: MonitorSettings, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(MonitorSettings.serializer(), t).encodeToByteArray()
            )
        }
    }
}