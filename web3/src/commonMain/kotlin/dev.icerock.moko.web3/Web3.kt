/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.annotation.DelicateWeb3Api
import dev.icerock.moko.web3.entity.RpcRequest
import dev.icerock.moko.web3.entity.RpcResponse
import dev.icerock.moko.web3.entity.Web3RpcException
import dev.icerock.moko.web3.entity.Web3RpcRequest
import dev.icerock.moko.web3.gas.DefaultGasProvider
import dev.icerock.moko.web3.gas.GasProvider
import dev.icerock.moko.web3.nonce.DefaultNonceManager
import dev.icerock.moko.web3.nonce.NonceManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.*

/**
 * Default http implementation for web3 requests.
 * @delicate Don't use the default constructor since it is for test purposes only
 */
class Web3 @DelicateWeb3Api constructor(
    override val chainId: BigInt,
    private val httpClient: HttpClient,
    private val json: Json,
    private val endpointUrl: String
) : Web3Executor {
    @OptIn(DelicateWeb3Api::class)
    constructor(chainId: BigInt, endpointUrl: String) : this(
        chainId = chainId,
        httpClient = HttpClient {
            install(DefaultRequest) {
                // some networks require the content type to be set
                contentType(ContentType.Application.Json)
            }
        },
        json = Json {
            // some networks return an additional info in models that may not be documented
            ignoreUnknownKeys = true
        },
        endpointUrl = endpointUrl
    )

    override val nonceManager = DefaultNonceManager(web3Executor = this)
    override val gasProvider = DefaultGasProvider(web3Executor = this)

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun <R> executeBatch(requests: List<Web3RpcRequest<out R>>): List<R> {
        // Used later for logging if exception
        val rawRequests = requests
            .mapIndexed { index, web3Request ->
                RpcRequest(
                    method = web3Request.method,
                    id = index,
                    params = web3Request.params
                )
            }

        val encodedToStringBody = rawRequests
            .map { request ->
                json.encodeToJsonElement(
                    serializer = RpcRequest.serializer(JsonElement.serializer()),
                    // cannot use copy since generics mismatch
                    value = RpcRequest(
                        method = request.method,
                        id = request.id,
                        jsonrpc = request.jsonrpc,
                        params = request.params
                    )
                )
            }.let { list -> json.encodeToString(list) }

        val responses = httpClient
            .post {
                url(endpointUrl)
                setBody(encodedToStringBody)
            }.body<String>().let { raw ->
                json.decodeFromString<List<JsonObject>>(raw)
            }

        // Here we are restoring the order
        return requests.mapIndexed { index, request ->
            val response = responses.first { response ->
                val id = response.getValue(key = "id").jsonPrimitive.int
                return@first id == index
            }

            return@mapIndexed processResponse(
                request = rawRequests[index],
                deserializer = request.resultSerializer,
                content = response.toString()
            )
        }
    }

    private fun <T> RpcResponse<JsonElement>.typed(
        deserializer: DeserializationStrategy<T>
    ) = RpcResponse(
        jsonrpc = jsonrpc,
        id = id,
        result = json.decodeFromJsonElement(deserializer, element = result ?: JsonNull),
        error = error
    )

    private fun <T> processResponse(
        request: RpcRequest<*>,
        deserializer: DeserializationStrategy<T>,
        content: String
    ): T {
        val response = json.decodeFromString(RpcResponse.serializer(JsonElement.serializer()), content)

        @Suppress("UNCHECKED_CAST")
        when {
            response.error != null -> throw Web3RpcException(
                code = response.error.code,
                message = response.error.message,
                request = request
            )
            else -> return response.typed(deserializer).result as T
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <T> unsafeCast(value: T, serializer: SerializationStrategy<*>): Pair<T, SerializationStrategy<T>> =
        value to (serializer as SerializationStrategy<T>)
}
