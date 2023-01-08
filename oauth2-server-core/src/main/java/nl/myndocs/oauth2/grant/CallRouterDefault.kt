package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.exception.InvalidScopeException
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.request.AuthorizationCodeRequest
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.request.ClientCredentialsRequest
import nl.myndocs.oauth2.request.ClientRequest
import nl.myndocs.oauth2.request.PasswordGrantRequest
import nl.myndocs.oauth2.request.RefreshTokenRequest
import nl.myndocs.oauth2.request.auth.BasicAuth
import nl.myndocs.oauth2.request.headerCaseInsensitive

class ClientInfo(private val ctx: CallContext) {
    val clientId by lazy {
        ctx.formParameters["client_id"] ?: headerValues?.username
    }

    val clientSecret by lazy {
        ctx.formParameters["client_secret"] ?: headerValues?.password
    }

    private val headerValues by lazy {
        parseHeaders(ctx)
    }

    private fun parseHeaders(ctx: CallContext) =
        ctx.headerCaseInsensitive("authorization")?.let { authHeader ->
            BasicAuth.parseCredentials(authHeader)
        }
}

fun GrantingCall.grantPassword() = granter("password") {
    val accessToken = authorize(
        ClientInfo(callContext).let { ci ->
            PasswordGrantRequest(
                ci.clientId,
                ci.clientSecret,
                callContext.formParameters["username"],
                callContext.formParameters["password"],
                callContext.formParameters["scope"]
            )
        }
    )

    callContext.respondHeader("Cache-Control", "no-store")
    callContext.respondHeader("Pragma", "no-cache")
    callContext.respondJson(accessTokenResponder.createResponse(accessToken))
}

fun GrantingCall.grantClientCredentials() = granter("client_credentials") {
    val accessToken = authorize(
        ClientInfo(callContext).let { ci ->
            ClientCredentialsRequest(
                ci.clientId,
                ci.clientSecret,
                callContext.formParameters["scope"]
            )
        }
    )

    callContext.respondHeader("Cache-Control", "no-store")
    callContext.respondHeader("Pragma", "no-cache")
    callContext.respondJson(accessTokenResponder.createResponse(accessToken))
}

fun GrantingCall.grantRefreshToken() = granter("refresh_token") {
    val accessToken = refresh(
        ClientInfo(callContext).let { ci ->
            RefreshTokenRequest(
                ci.clientId,
                ci.clientSecret,
                callContext.formParameters["refresh_token"]
            )
        }
    )

    callContext.respondHeader("Cache-Control", "no-store")
    callContext.respondHeader("Pragma", "no-cache")
    callContext.respondJson(accessTokenResponder.createResponse(accessToken))
}

fun GrantingCall.grantAuthorizationCode() = granter("authorization_code") {
    val accessToken = authorize(
        ClientInfo(callContext).let { ci ->
            AuthorizationCodeRequest(
                ci.clientId,
                ci.clientSecret,
                callContext.formParameters["code"],
                callContext.formParameters["redirect_uri"]
            )
        }
    )

    callContext.respondHeader("Cache-Control", "no-store")
    callContext.respondHeader("Pragma", "no-cache")
    callContext.respondJson(accessTokenResponder.createResponse(accessToken))
}

internal val INVALID_REQUEST_FIELD_MESSAGE = "'%s' field is missing"

fun GrantingCall.validateScopes(
    client: Client,
    identity: Identity,
    requestedScopes: Set<String>
) {
    val scopesAllowed = scopesAllowed(client.clientScopes, requestedScopes)
    if (!scopesAllowed) {
        throw InvalidScopeException(requestedScopes.minus(client.clientScopes))
    }

    val allowedScopes = identityService.allowedScopes(client, identity, requestedScopes)

    val ivalidScopes = requestedScopes.minus(allowedScopes)
    if (ivalidScopes.isNotEmpty()) {
        throw InvalidScopeException(ivalidScopes)
    }
}

fun GrantingCall.tokenInfo(accessToken: String) = tokenStore.tokenInfo(accessToken) ?: throw InvalidGrantException()

fun GrantingCall.throwExceptionIfUnverifiedClient(clientRequest: ClientRequest): Client {
    val clientId = clientRequest.clientId
        ?: throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_id"))

    val clientSecret = clientRequest.clientSecret
        ?: throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_secret"))

    return clientService.clientOf(clientId, clientSecret) ?: throw InvalidClientException()
}

fun GrantingCall.scopesAllowed(clientScopes: Set<String>, requestedScopes: Set<String>): Boolean {
    return clientScopes.containsAll(requestedScopes)
}
