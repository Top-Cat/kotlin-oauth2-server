package nl.myndocs.oauth2.device

interface DeviceCodeUrlGenerator {
    fun getUrl(): String
    fun getUrl(code: String): String
}
