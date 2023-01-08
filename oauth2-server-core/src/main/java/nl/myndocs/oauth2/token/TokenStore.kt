package nl.myndocs.oauth2.token

import nl.myndocs.oauth2.identity.TokenInfo

interface TokenStore {
    fun storeAccessToken(accessToken: AccessToken)

    fun accessToken(token: String): AccessToken?

    fun tokenInfo(token: String): TokenInfo?

    fun revokeAccessToken(token: String)

    fun storeCodeToken(codeToken: CodeToken)

    fun codeToken(token: String): CodeToken?

    /**
     * Retrieve token and delete it from store
     */
    fun consumeCodeToken(token: String): CodeToken?

    fun storeRefreshToken(refreshToken: RefreshToken)

    fun refreshToken(token: String): RefreshToken?

    fun revokeRefreshToken(token: String)
}