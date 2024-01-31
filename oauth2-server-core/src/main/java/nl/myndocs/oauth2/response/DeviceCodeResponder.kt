package nl.myndocs.oauth2.response

import nl.myndocs.oauth2.device.DeviceCode

interface DeviceCodeResponder {
    fun createResponse(deviceCode: DeviceCode): Map<String, Any?>
}
