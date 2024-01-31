package nl.myndocs.oauth2.device

interface DeviceCodeStore {
    fun storeDeviceCode(deviceCode: DeviceCode)
    fun getForUserCode(userCode: String): DeviceCode?
    fun consumeIfComplete(deviceCode: String): DeviceCode?
}
