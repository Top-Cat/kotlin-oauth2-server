package nl.myndocs.oauth2.config

import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.device.DeviceCodeConverter
import nl.myndocs.oauth2.device.DeviceCodeStore
import nl.myndocs.oauth2.device.UUIDDeviceCodeConverter
import nl.myndocs.oauth2.grant.Granter
import nl.myndocs.oauth2.grant.GrantingCall
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.response.AccessTokenResponder
import nl.myndocs.oauth2.response.DefaultAccessTokenResponder
import nl.myndocs.oauth2.response.DefaultDeviceCodeResponder
import nl.myndocs.oauth2.response.DeviceCodeResponder
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.*

object ConfigurationBuilder {
    open class Configuration {
        internal val callRouterConfiguration = CallRouterBuilder.Configuration()

        var authorizationEndpoint: String
            get() = callRouterConfiguration.authorizeEndpoint
            set(value) {
                callRouterConfiguration.authorizeEndpoint = value
            }

        var tokenEndpoint: String
            get() = callRouterConfiguration.tokenEndpoint
            set(value) {
                callRouterConfiguration.tokenEndpoint = value
            }

        var tokenInfoEndpoint: String
            get() = callRouterConfiguration.tokenInfoEndpoint
            set(value) {
                callRouterConfiguration.tokenInfoEndpoint = value
            }

        var tokenInfoCallback: (TokenInfo) -> Map<String, Any?>
            get() = callRouterConfiguration.tokenInfoCallback
            set(value) {
                callRouterConfiguration.tokenInfoCallback = value
            }

        var deviceCodeEndpoint: String
            get() = callRouterConfiguration.deviceCodeEndpoint
            set(value) {
                callRouterConfiguration.deviceCodeEndpoint = value
            }

        var granters: List<GrantingCall.() -> Granter>
            get() = callRouterConfiguration.granters
            set(value) {
                callRouterConfiguration.granters = value
            }

        var identityService: IdentityService? = null
        var clientService: ClientService? = null
        var tokenStore: TokenStore? = null
        var accessTokenConverter: AccessTokenConverter = UUIDAccessTokenConverter()
        var refreshTokenConverter: RefreshTokenConverter = UUIDRefreshTokenConverter()
        var codeTokenConverter: CodeTokenConverter = UUIDCodeTokenConverter()
        var accessTokenResponder: AccessTokenResponder = DefaultAccessTokenResponder
        var deviceCodeStore: DeviceCodeStore? = null
        var deviceCodeConverter: DeviceCodeConverter = UUIDDeviceCodeConverter()
        var deviceCodeResponder: DeviceCodeResponder = DefaultDeviceCodeResponder
    }

    fun build(
        configurer: Configuration.() -> Unit,
        configuration: Configuration
    ): nl.myndocs.oauth2.config.Configuration {
        configurer(configuration)

        val grantingCallFactory: (CallContext) -> GrantingCall = { callContext ->
            object : GrantingCall {
                override val callContext = callContext
                override val identityService = configuration.identityService!!
                override val clientService = configuration.clientService!!
                override val tokenStore = configuration.tokenStore!!
                override val deviceCodeStore = configuration.deviceCodeStore!!
                override val converters = Converters(
                    configuration.accessTokenConverter,
                    configuration.refreshTokenConverter,
                    configuration.codeTokenConverter,
                    configuration.deviceCodeConverter
                )
                override val accessTokenResponder = configuration.accessTokenResponder
                override val deviceCodeResponder = configuration.deviceCodeResponder
            }
        }
        return Configuration(
            CallRouterBuilder.build(
                configuration.callRouterConfiguration,
                grantingCallFactory
            )
        )
    }

    fun build(configurer: Configuration.() -> Unit): nl.myndocs.oauth2.config.Configuration {
        val configuration = Configuration()
        return build(configurer, configuration)
    }
}