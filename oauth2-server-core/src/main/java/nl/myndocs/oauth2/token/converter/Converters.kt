package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.device.DeviceCodeConverter

data class Converters(
    val accessTokenConverter: AccessTokenConverter,
    val refreshTokenConverter: RefreshTokenConverter,
    val codeTokenConverter: CodeTokenConverter,
    val deviceCodeConverter: DeviceCodeConverter
)