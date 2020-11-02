package app.error

import java.lang.Exception

class RequestErrorException(val requestError: RequestError) : Exception()