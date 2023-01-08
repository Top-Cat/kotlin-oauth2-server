package nl.myndocs.oauth2.client.inmemory

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService

class InMemoryClient : ClientService {
    private val clients = mutableListOf<ClientConfiguration>()

    fun client(inline: ClientConfiguration.() -> Unit): InMemoryClient {
        val client = ClientConfiguration()
        inline(client)

        clients.add(client)
        return this
    }

    private fun toClient(client: ClientConfiguration) =
        Client(client.clientId!!, client.scopes, client.redirectUris, client.authorizedGrantTypes)

    override fun clientOf(clientId: String, clientSecret: String) =
        clients.firstOrNull { it.clientId == clientId && it.clientSecret == clientSecret }?.let(::toClient)

    override fun clientOf(clientId: String) =
        configuredClient(clientId)?.let(::toClient)

    override fun validClient(client: Client, clientSecret: String) =
        configuredClient(client.clientId)!!.clientSecret == clientSecret

    private fun configuredClient(clientId: String) =
            clients.firstOrNull { it.clientId == clientId }
}