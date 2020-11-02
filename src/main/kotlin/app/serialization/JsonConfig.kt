package app.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.time.ZonedDateTime

object JsonConfig {

    val json = Json {
        serializersModule = SerializersModule {
            contextual(object : KSerializer<ZonedDateTime> {
                override val descriptor = PrimitiveSerialDescriptor(ZonedDateTime::class.qualifiedName!!, PrimitiveKind.STRING)
                override fun deserialize(decoder: Decoder) = ZonedDateTime.parse(decoder.decodeString())
                override fun serialize(encoder: Encoder, value: ZonedDateTime) = encoder.encodeString(value.toString())
            })
        }
    }

}