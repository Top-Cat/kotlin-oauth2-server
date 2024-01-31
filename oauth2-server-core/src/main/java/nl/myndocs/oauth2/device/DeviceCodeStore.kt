package nl.myndocs.oauth2.device

interface DeviceCodeStore {
    fun storeDeviceCode(deviceCode: DeviceCode)
    fun getForUserCode(userCode: String): DeviceCode?
    fun getForDeviceCode(deviceCode: String): DeviceCode?
    fun removeDeviceCode(deviceCode: DeviceCode)
}
