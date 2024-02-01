package nl.myndocs.oauth2.tokenstore.inmemory

import nl.myndocs.oauth2.device.DeviceCode
import nl.myndocs.oauth2.device.DeviceCodeStore
import nl.myndocs.oauth2.token.ExpirableToken

class InMemoryDeviceCodeStore : DeviceCodeStore {
    private val deviceCodes = mutableMapOf<String, DeviceCode>()

    override fun storeDeviceCode(deviceCode: DeviceCode) {
        removeExpiredTokens()
        deviceCodes[deviceCode.userCode] = deviceCode
    }

    override fun getForUserCode(userCode: String): DeviceCode? =
        locateToken(deviceCodes, userCode)

    override fun getForDeviceCode(deviceCode: String) =
        deviceCodes.values.firstOrNull { it.deviceCode == deviceCode }?.let {
            if (it.expired()) {
                deviceCodes.remove(it.userCode)
                null
            } else {
                it
            }
        }

    override fun removeDeviceCode(deviceCode: DeviceCode) {
        deviceCodes.remove(deviceCode.userCode)
    }

    private fun <T : ExpirableToken> locateToken(tokens: MutableMap<String, T>, token: String): T? {
        var tokenFromMap = tokens[token]

        if (tokenFromMap != null && tokenFromMap.expired()) {
            tokens.remove(token)

            tokenFromMap = null
        }

        return tokenFromMap
    }

    private fun removeExpiredTokens() {
        deviceCodes.entries.removeAll { it.value.expired() }
    }
}
