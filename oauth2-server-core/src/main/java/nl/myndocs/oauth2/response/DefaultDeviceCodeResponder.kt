package nl.myndocs.oauth2.response

import nl.myndocs.oauth2.device.DeviceCode

object DefaultDeviceCodeResponder : DeviceCodeResponder {
    override fun createResponse(deviceCode: DeviceCode): Map<String, Any?> = with(deviceCode) {
        mapOf(
            "user_code" to userCode,
            "verification_url" to verificationUri,
            "verification_url_complete" to verificationUriComplete,
            "device_code" to this.deviceCode,
            "interval" to interval,
            "expires_in" to this.expiresIn()
        )
    }
}
