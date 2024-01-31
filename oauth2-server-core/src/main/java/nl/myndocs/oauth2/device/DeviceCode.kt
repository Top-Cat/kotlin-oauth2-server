package nl.myndocs.oauth2.device

import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.ExpirableToken
import java.time.Instant

data class DeviceCode(
    val clientId: String,
    val scopes: String,

    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val verificationUriComplete: String,
    val interval: Int,
    override val expireTime: Instant,

    val complete: Boolean = false,
    val identity: Identity? = null
) : ExpirableToken
