package com.habitiora.batty.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.habitiora.batty.domain.model.ThresholdsConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object ThresholdsSerializer : Serializer<ThresholdsConfig> {
    override val defaultValue: ThresholdsConfig = ThresholdsConfig()

    override suspend fun readFrom(input: InputStream): ThresholdsConfig {
        return try {
            Json.decodeFromString(
                deserializer = ThresholdsConfig.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            throw CorruptionException("Error deserializando JSON de ThresholdsConfig", e)
        }
    }

    override suspend fun writeTo(t: ThresholdsConfig, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(
                    serializer = ThresholdsConfig.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        }
    }
}