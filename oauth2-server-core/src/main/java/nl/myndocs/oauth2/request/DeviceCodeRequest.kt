package nl.myndocs.oauth2.request

data class DeviceCodeRequest(
    override val clientId: String?,
    override val clientSecret: String?,
    val deviceCode: String?
) : ClientRequest {
    val grant_type = "device_code"
}