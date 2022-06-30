/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.requests

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.entity.BlockHash
import dev.icerock.moko.web3.entity.BlockInfo
import dev.icerock.moko.web3.entity.BlockState
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.entity.TransactionHash
import dev.icerock.moko.web3.entity.WalletAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.gas.GasConfiguration
import dev.icerock.moko.web3.entity.Web3RpcRequest
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.entity.Transaction
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.nonce.NonceManager
import dev.icerock.moko.web3.requests.polling.shortPollingUntilNotNull
import dev.icerock.moko.web3.signing.Credentials
import dev.icerock.moko.web3.signing.SignedTransaction
import dev.icerock.moko.web3.signing.unifiedSignTransferTransaction
import kotlinx.serialization.DeserializationStrategy


suspend fun <T> Web3Executor.executeBatch(vararg requests: Web3RpcRequest<T>): List<T> =
    executeBatch(requests.toList())

suspend fun Web3Executor.getTransaction(
    transactionHash: TransactionHash
): Transaction = executeBatch(Web3Requests.getTransaction(transactionHash)).first()

suspend fun Web3Executor.getTransactionReceipt(
    transactionHash: TransactionHash
): TransactionReceipt? = executeBatch(Web3Requests.getTransactionReceipt(transactionHash)).first()

suspend fun Web3Executor.getNativeBalance(
    walletAddress: WalletAddress,
    blockState: BlockState = BlockState.Latest
): BigInt = executeBatch(Web3Requests.getNativeBalance(walletAddress, blockState)).first()

suspend fun Web3Executor.getTransactionCount(
    walletAddress: WalletAddress,
    blockState: BlockState = BlockState.Pending
): BigInt = executeBatch(Web3Requests.getNativeTransactionCount(walletAddress, blockState)).first()

suspend fun <T> Web3Executor.call(
    contractAddress: ContractAddress,
    callData: HexString,
    // deserialize from calldata to normal type
    dataDeserializer: DeserializationStrategy<T>,
    blockState: BlockState = BlockState.Latest,
): T = executeBatch(Web3Requests.call(contractAddress, callData, dataDeserializer, blockState)).first()

suspend fun Web3Executor.sendTransferTransaction(
    credentials: Credentials,
    to: WalletAddress,
    value: BigInt,
    nonce: BigInt? = null,
    gasConfiguration: GasConfiguration? = null
): TransactionHash {
    val actualGasConfiguration = gasConfiguration ?: gasProvider.estimate(
        from = credentials.address,
        to = to,
        callData = null,
        value = value
    )

    if (nonce != null) {
        val signed = credentials.unifiedSignTransferTransaction(nonce, chainId, to, value, actualGasConfiguration)
        return send(signed)
    }

    return nonceManager.withNonce(credentials.address) { actualNonce ->
        val signed = credentials.unifiedSignTransferTransaction(actualNonce, chainId, to, value, actualGasConfiguration)
        return@withNonce NonceManager.NonceResult.Increase(send(signed))
    }
}

suspend fun Web3Executor.send(transaction: SignedTransaction): TransactionHash =
    executeBatch(Web3Requests.send(transaction)).first()

suspend fun Web3Executor.getGasPrice(): BigInt = executeBatch(Web3Requests.getGasPrice()).first()

suspend fun Web3Executor.getEstimateGas(
    from: EthereumAddress?,
    gasPrice: BigInt?,
    to: EthereumAddress,
    callData: HexString?,
    value: BigInt?
): BigInt = executeBatch(Web3Requests.getEstimateGas(from, gasPrice, to, callData, value)).first()

suspend fun Web3Executor.getEstimateGas(
    callRpcRequest: CallRpcRequest<*>,
    from: EthereumAddress?,
    gasPrice: BigInt?,
    value: BigInt?
): BigInt = executeBatch(Web3Requests.getEstimateGas(callRpcRequest, from, gasPrice, value)).first()

suspend fun Web3Executor.getBlockNumber(): BigInt = executeBatch(Web3Requests.getBlockNumber()).first()

suspend fun Web3Executor.getBlockByNumber(blockState: BlockState): BlockInfo? =
    executeBatch(Web3Requests.getBlockByNumber(blockState)).first()

suspend fun Web3Executor.getLogs(
    address: EthereumAddress? = null,
    fromBlock: BlockState? = null,
    toBlock: BlockState? = null,
    topics: List<Hex32String?>? = null,
    blockHash: BlockHash? = null
): List<LogEvent> = executeBatch(Web3Requests.getLogs(address, fromBlock, toBlock, topics, blockHash)).first()

suspend fun Web3Executor.waitForTransactionReceipt(
    hash: TransactionHash,
    // one minute is the default timeout
    timeOutMillis: Long? = 1L * 60L * 1_000L,
    // interval is the default interval,
    intervalMillis: Long = 1_000
): TransactionReceipt =
    shortPollingUntilNotNull(timeOutMillis, intervalMillis) {
        getTransactionReceipt(hash)
    }
