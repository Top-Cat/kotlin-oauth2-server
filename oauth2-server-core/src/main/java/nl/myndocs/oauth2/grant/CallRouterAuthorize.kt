package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.exception.AuthorizationPendingException
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidIdentityException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.exception.InvalidScopeException
import nl.myndocs.oauth2.request.AuthorizationCodeRequest
import nl.myndocs.oauth2.request.ClientCredentialsRequest
import nl.myndocs.oauth2.request.DeviceCodeRequest
import nl.myndocs.oauth2.request.PasswordGrantRequest
import nl.myndocs.oauth2.scope.ScopeParser
import nl.myndocs.oauth2.token.AccessToken

/**
 * @throws InvalidIdentityException
 * @throws InvalidClientException
 * @throws InvalidScopeException
 */
fun GrantingCall.authorize(passwordGrantRequest: PasswordGrantRequest): AccessToken {
    val requestedClient = throwExceptionIfUnverifiedClient(passwordGrantRequest)

    if (passwordGrantRequest.username == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("username"))
    }

    if (passwordGrantRequest.password == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("password"))
    }

    val authorizedGrantType = AuthorizedGrantType.PASSWORD
    if (!requestedClient.authorizedGrantTypes.contains(authorizedGrantType)) {
        throw InvalidGrantException("Authorize not allowed: '$authorizedGrantType'")
    }

    val requestedIdentity = identityService.identityOf(
        requestedClient, passwordGrantRequest.username
    )

    if (requestedIdentity == null || !identityService.validCredentials(
            requestedClient,
            requestedIdentity,
            passwordGrantRequest.password
        )
    ) {
        throw InvalidIdentityException()
    }

    var requestedScopes = ScopeParser.parseScopes(passwordGrantRequest.scope)
        .toSet()

    if (passwordGrantRequest.scope == null) {
        requestedScopes = requestedClient.clientScopes
    }

    validateScopes(requestedClient, requestedIdentity, requestedScopes)

    val accessToken = converters.accessTokenConverter.convertToToken(
        requestedIdentity,
        requestedClient.clientId,
        requestedScopes,
        converters.refreshTokenConverter.convertToToken(
            requestedIdentity,
            requestedClient.clientId,
            requestedScopes
        )
    )

    tokenStore.storeAccessToken(accessToken)

    return accessToken
}

fun GrantingCall.authorize(authorizationCodeRequest: AuthorizationCodeRequest): AccessToken {
    throwExceptionIfUnverifiedClient(authorizationCodeRequest)

    if (authorizationCodeRequest.code == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("code"))
    }

    if (authorizationCodeRequest.redirectUri == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("redirect_uri"))
    }

    val consumeCodeToken = tokenStore.consumeCodeToken(authorizationCodeRequest.code)
        ?: throw InvalidGrantException()


    if (consumeCodeToken.redirectUri != authorizationCodeRequest.redirectUri || consumeCodeToken.clientId != authorizationCodeRequest.clientId) {
        throw InvalidGrantException()
    }

    val accessToken = converters.accessTokenConverter.convertToToken(
        consumeCodeToken.identity,
        consumeCodeToken.clientId,
        consumeCodeToken.scopes,
        converters.refreshTokenConverter.convertToToken(
            consumeCodeToken.identity,
            consumeCodeToken.clientId,
            consumeCodeToken.scopes
        )
    )

    tokenStore.storeAccessToken(accessToken)

    return accessToken
}

fun GrantingCall.authorize(clientCredentialsRequest: ClientCredentialsRequest): AccessToken {
    val requestedClient = throwExceptionIfUnverifiedClient(clientCredentialsRequest)

    val scopes = clientCredentialsRequest.scope
        ?.let { ScopeParser.parseScopes(it).toSet() }
        ?: requestedClient.clientScopes

    val accessToken = converters.accessTokenConverter.convertToToken(
        identity = null,
        clientId = clientCredentialsRequest.clientId!!,
        requestedScopes = scopes,
        refreshToken = converters.refreshTokenConverter.convertToToken(
            identity = null,
            clientId = clientCredentialsRequest.clientId,
            requestedScopes = scopes
        )
    )

    tokenStore.storeAccessToken(accessToken)

    return accessToken
}

fun GrantingCall.authorize(deviceCodeRequest: DeviceCodeRequest): AccessToken {
    val requestedClient = throwExceptionIfUnverifiedClient(deviceCodeRequest)

    if (deviceCodeRequest.deviceCode == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("device_code"))
    }

    val deviceCode = deviceCodeStore.getForDeviceCode(deviceCodeRequest.deviceCode)
        ?: throw InvalidGrantException()

    deviceCode.complete || throw AuthorizationPendingException("The user has not yet completed authorization")
    deviceCodeStore.removeDeviceCode(deviceCode)

    if (deviceCode.clientId != deviceCodeRequest.clientId) {
        throw InvalidGrantException()
    }

    val identity = deviceCode.identity ?: throw InvalidGrantException()
    val scopes = ScopeParser.parseScopes(deviceCode.scopes)

    validateScopes(requestedClient, identity, scopes)

    val accessToken = converters.accessTokenConverter.convertToToken(
        identity,
        deviceCode.clientId,
        scopes,
        converters.refreshTokenConverter.convertToToken(
            identity,
            deviceCode.clientId,
            scopes
        )
    )

    tokenStore.storeAccessToken(accessToken)

    return accessToken
}
