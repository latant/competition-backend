package app.validation

import org.apache.commons.validator.routines.EmailValidator
import java.util.regex.Pattern

inline fun String.requireValidEmail(lazyMessage: () -> Any) {
   require(EmailValidator.getInstance().isValid(this), lazyMessage)
}

inline fun String.requireNotBlank(lazyMessage: () -> Any) {
    require(isNotBlank(), lazyMessage)
}

inline fun String.requireValidPassword(lazyMessage: () -> Any) {
    require(length > 3, lazyMessage)
}

inline fun Int.requirePositive(lazyMessage: () -> Any) {
    require(this > 0, lazyMessage)
}

inline fun List<Any?>.requireMultiple(lazyMessage: () -> Any) {
    require(size > 1, lazyMessage)
}

val cssHexColorRegex: Regex = Pattern.compile("#([a-f]|[A-F]|[0-9]){3}(([a-f]|[A-F]|[0-9]){3})?\$").toRegex()

inline fun String.requireValidCssHexColor(lazyMessage: () -> Any) {
    require(cssHexColorRegex.matches(this), lazyMessage)
}

inline fun Int.requireMultiple(lazyMessage: () -> Any) {
    require(this > 1, lazyMessage)
}
