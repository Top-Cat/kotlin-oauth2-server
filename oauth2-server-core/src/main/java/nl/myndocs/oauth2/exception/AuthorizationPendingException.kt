package nl.myndocs.oauth2.exception

class AuthorizationPendingException(message: String) : OauthException(OauthError.AUTHORIZATION_PENDING, message)
