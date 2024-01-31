package nl.myndocs.oauth2.device.code

fun interface UserCodeGenerator {
    fun generate(): String
}

