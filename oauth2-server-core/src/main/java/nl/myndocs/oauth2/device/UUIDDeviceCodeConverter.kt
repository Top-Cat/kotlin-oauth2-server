package nl.myndocs.oauth2.device

import nl.myndocs.oauth2.device.code.NumericCodeGenerator
import nl.myndocs.oauth2.device.code.UserCodeGenerator
import java.time.Instant
import java.util.UUID

class UUIDDeviceCodeConverter(
    private val userCodeGenerator: UserCodeGenerator = NumericCodeGenerator(8),
    private val deviceCodeUrlGenerator: DeviceCodeUrlGenerator = ExampleUrlGenerator,
    private val pollInterval: Int = 5,
    private val deviceCodeExpirySeconds: Int = 1800
) : DeviceCodeConverter {
    override fun createDeviceCode(clientId: String, scopes: String): DeviceCode {
        val userCode = userCodeGenerator.generate()
        return DeviceCode(
            clientId,
            scopes,
            UUID.randomUUID().toString(),
            userCode,
            deviceCodeUrlGenerator.getUrl(),
            deviceCodeUrlGenerator.getUrl(userCode),
            pollInterval,
            Instant.now().plusSeconds(deviceCodeExpirySeconds.toLong())
        )
    }
}
