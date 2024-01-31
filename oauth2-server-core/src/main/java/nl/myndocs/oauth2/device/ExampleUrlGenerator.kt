package nl.myndocs.oauth2.device

object ExampleUrlGenerator : DeviceCodeUrlGenerator {
    override fun getUrl() = "https://example.com/code"
    override fun getUrl(code: String) = "${getUrl()}?code=$code"
}
