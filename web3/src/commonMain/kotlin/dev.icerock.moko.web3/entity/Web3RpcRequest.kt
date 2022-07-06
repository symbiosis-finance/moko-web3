/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.entity

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

fun <TParam, TResult> Web3RpcRequest(
    method: String,
    params: List<TParam>,
    paramsSerializer: SerializationStrategy<TParam>,
    resultSerializer: DeserializationStrategy<TResult>,
    json: Json = Json
) = Web3RpcRequest(
    method = method,
    params = params.map { param -> json.encodeToJsonElement(paramsSerializer, param) },
    resultSerializer = resultSerializer
)

open class Web3RpcRequest<TResult>(
    val method: String,
    val params: List<JsonElement>,
    val resultSerializer: DeserializationStrategy<TResult>
) {
    override fun toString() =
        "Web3RpcRequest(method='$method', params=$params, resultSerializer=$resultSerializer)"
}
