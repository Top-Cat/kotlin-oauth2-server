package nl.myndocs.oauth2.client

interface ClientService {
    fun clientOf(clientId: String): Client?
    fun clientOf(clientId: String, clientSecret: String): Client?
    fun validClient(client: Client, clientSecret: String): Boolean
}