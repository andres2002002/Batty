package com.habitiora.batty.data.proto

import androidx.datastore.core.CorruptionException
import com.habitiora.batty.data.proto.ThresholdsConfig
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object ThresholdsSerializer : Serializer<ThresholdsConfig> {
    override val defaultValue: ThresholdsConfig = ThresholdsConfig.newBuilder()
        .addAllLowThresholds(listOf(15, 10, 7, 4))
        .addAllHighThresholds(listOf(85, 90, 98))
        // map fields quedan vacíos (false por defecto)
        .build()

    override suspend fun readFrom(input: InputStream): ThresholdsConfig {
        try {
            return ThresholdsConfig.parseFrom(input)
        } catch (ex: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", ex)
        }
    }

    override suspend fun writeTo(t: ThresholdsConfig, output: OutputStream) =
        t.writeTo(output)
}