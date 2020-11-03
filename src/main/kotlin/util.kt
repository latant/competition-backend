import app.security.UserPrincipal
import io.ktor.application.*
import io.ktor.auth.*
import java.io.File
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

private object ResourcesToken
fun resourceURL(name: String): URL = ResourcesToken::class.java.classLoader.getResource(name)
    ?: error("Resource '$name' could not be found")
fun resourceFile(name: String) = File(resourceURL(name).toURI())
fun resourceFileText(name: String) = resourceFile(name).readText()
fun resourceCypher(name: String) = resourceFileText("cypher/$name.cyp")

val ApplicationCall.userPrincipal get() = principal<UserPrincipal>()

fun ZonedDateTime.atUTC(): LocalDateTime = toOffsetDateTime().atZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()

fun <E> Sequence<E>.repeat() = sequence { while (true) { yieldAll(this@repeat) } }
fun <E> Sequence<E>.repeatEveryNth(n: Int) = sequence {
    var i = 1
    forEach {
        yield(it)
        if (i == n) {
            yield(it)
            i = 1
        }
        i++
    }
}

val IntRange.length get() = last - first + 1

fun String.toZonedDateTime(): ZonedDateTime = ZonedDateTime.parse(this)
fun utcNow() = ZonedDateTime.now().atUTC()
fun LocalDateTime.startOfDay(): LocalDateTime = withHour(0).withMinute(0).withSecond(0).withNano(0)