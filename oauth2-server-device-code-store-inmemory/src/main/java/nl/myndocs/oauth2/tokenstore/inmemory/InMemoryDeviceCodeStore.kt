package nl.myndocs.oauth2.tokenstore.inmemory

import nl.myndocs.oauth2.device.DeviceCode
import nl.myndocs.oauth2.device.DeviceCodeStore
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.ExpirableToken

class InMemoryDeviceCodeStore : DeviceCodeStore {
    private val deviceCodes = mutableMapOf<String, DeviceCode>()

    override fun storeDeviceCode(deviceCode: DeviceCode) {
        deviceCodes[deviceCode.userCode] = deviceCode
    }

    override fun getForUserCode(userCode: String): DeviceCode? =
            locateToken(deviceCodes, userCode)

    fun completeDeviceCode(userCode: String, identity: Identity) {
        deviceCodes.computeIfPresent(userCode) { _, deviceCode ->
            deviceCode.copy(complete = true, identity = identity)
        }
    }

    override fun consumeIfComplete(deviceCode: String) =
        deviceCodes.values.firstOrNull { it.deviceCode == deviceCode }?.let {
            if (it.complete || it.expired()) {
                deviceCodes.remove(it.userCode)
            }

            if (it.expired()) {
                null
            } else {
                it
            }
        }

    private fun <T : ExpirableToken> locateToken(tokens: MutableMap<String, T>, token: String): T? {
        var tokenFromMap = tokens[token]

        if (tokenFromMap != null && tokenFromMap.expired()) {
            tokens.remove(token)

            tokenFromMap = null
        }

        return tokenFromMap
    }
}
