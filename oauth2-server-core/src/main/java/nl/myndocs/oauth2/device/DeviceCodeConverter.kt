package nl.myndocs.oauth2.device

interface DeviceCodeConverter {
    fun createDeviceCode(clientId: String, scopes: String): DeviceCode
}
