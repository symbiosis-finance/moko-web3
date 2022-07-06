/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.requests.polling

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.entity.BlockInfo
import dev.icerock.moko.web3.entity.BlockState
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.requests.Web3Requests
import dev.icerock.moko.web3.requests.getBlockNumber
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform


private operator fun BigInt.rangeTo(other: BigInt): Iterable<BigInt> =
    generateSequence(seed = this) { block -> (block + 1).takeIf { it <= other} }.asIterable()

fun Web3Executor.newBlocksShortPolling(
    fromBlock: BigInt? = null,
    pollingInterval: Long = 5_000
): Flow<BlockInfo> =
    flow {
        var previousBlockNumber = fromBlock ?: getBlockNumber()
        while (true) {
            delay(pollingInterval)
            val blockNumber = getBlockNumber()
            if (blockNumber != previousBlockNumber) {
                emit(value = previousBlockNumber to blockNumber)
                previousBlockNumber = blockNumber
            }
        }
    }.transform { (fromBlock, toBlock): Pair<BigInt, BigInt> ->
        val blockNumbers = fromBlock..(toBlock - 1)

        val requests = blockNumbers
            .map(BlockState::Quantity)
            .map(Web3Requests::getBlockByNumber)

        executeBatch(requests)
            .forEach { block ->
                block ?: error("Block does not seem to exist")
                emit(block)
            }
    }
