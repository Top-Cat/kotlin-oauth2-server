package nl.myndocs.oauth2.device.code

class AlphaCodeGenerator(val length: Int) : UserCodeGenerator {
    private val allowedChars = ('A'..'Z') + ('0'..'9')
    override fun generate() = (1..length).map { allowedChars.random() }.joinToString("")
}
