/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.entity.BlockHash
import dev.icerock.moko.web3.entity.BlockState
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.ABIEncoder.encodeCallDataForMethod
import dev.icerock.moko.web3.entity.TransactionHash
import dev.icerock.moko.web3.gas.GasConfiguration
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.requests.CallRpcRequest
import dev.icerock.moko.web3.requests.Web3Requests
import dev.icerock.moko.web3.requests.executeBatch
import dev.icerock.moko.web3.requests.getLogs
import dev.icerock.moko.web3.signing.Credentials
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.JsonArray

class SmartContract(
    val executor: Web3Executor,
    val contractAddress: ContractAddress,
    val abiJson: JsonArray
) {
    @Suppress("UNCHECKED_CAST")
    private fun <T> makeAbiDeserializer(method: String, mapper: (List<Any?>) -> T): DeserializationStrategy<T> =
        object : DeserializationStrategy<T> {
            override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
                serialName = "StringAbiPrimitiveDeserializer",
                kind = PrimitiveKind.STRING
            )
            override fun deserialize(decoder: Decoder): T {
                val abiResult = HexString(decoder.decodeString())
                return ABIDecoder.decodeCallDataForOutputs(abiJson, method, abiResult).let(mapper)
            }
        }

    fun <T> readRequest(
        method: String,
        params: List<Any?>,
        mapper: (List<Any?>) -> T
    ): CallRpcRequest<T> {
        val data = encodeMethod(method, params)
        return Web3Requests.call(contractAddress, data, makeAbiDeserializer(method, mapper))
    }

    private fun encodeMethod(
        method: String,
        params: List<Any?>
    ): HexString = encodeCallDataForMethod(abiJson, method, params)

    suspend fun <T> read(
        method: String,
        params: List<Any?>,
        mapper: (List<Any?>) -> T
    ): T = executor.executeBatch(readRequest(method, params, mapper)).first()

    fun getLogsRequest(
        fromBlock: BlockState? = null,
        toBlock: BlockState? = null,
        topics: List<Hex32String?>? = null,
        blockHash: BlockHash? = null
    ) = Web3Requests.getLogs(contractAddress, fromBlock, toBlock, topics, blockHash)

    suspend fun getLogs(
        fromBlock: BlockState? = null,
        toBlock: BlockState? = null,
        topics: List<Hex32String?>? = null,
        blockHash: BlockHash? = null
    ) = executor.getLogs(contractAddress, fromBlock, toBlock, topics, blockHash)

    fun hashEventSignature(event: String): Hex32String = ABIEncoder.hashEventSignature(abiJson, event)

    fun writeRequest(
        method: String,
        params: List<Any>,
        value: BigInt = 0.bi
    ): WriteRequest = WriteRequest(
        contract = this,
        callData = encodeMethod(method, params),
        value = value
    )

    suspend fun write(
        credentials: Credentials,
        method: String,
        params: List<Any>,
        value: BigInt = 0.bi,
        nonce: BigInt? = null,
        gasConfiguration: GasConfiguration? = null
    ): TransactionHash = writeRequest(method, params, value)
        .send(credentials, nonce, gasConfiguration)
}

