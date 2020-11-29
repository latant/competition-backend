package app.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.contextual
import java.time.LocalDateTime
import java.time.ZonedDateTime

object JsonConfig {

    val json = Json {
        serializersModule = SerializersModule {
            contextualString(ZonedDateTime::toString, ZonedDateTime::parse)
            contextualString(LocalDateTime::toString, LocalDateTime::parse)
        }
    }

    private inline fun <reified T: Any> SerializersModuleBuilder.contextualString(
        crossinline toString: (T) -> String,
        crossinline fromString: (String) -> T
    ) {
        contextual(object : KSerializer<T> {
            override val descriptor: SerialDescriptor get() =
                PrimitiveSerialDescriptor(T::class.qualifiedName!!, PrimitiveKind.STRING)
            override fun deserialize(decoder: Decoder): T {
                try {
                    return fromString(decoder.decodeString())
                } catch (e: Throwable) {
                    throw SerializationException(e.message)
                }
            }
            override fun serialize(encoder: Encoder, value: T) {
                encoder.encodeString(toString(value))
            }
        })
    }

}