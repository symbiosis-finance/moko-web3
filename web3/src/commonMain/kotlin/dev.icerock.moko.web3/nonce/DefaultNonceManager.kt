package dev.icerock.moko.web3.nonce

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.entity.WalletAddress
import dev.icerock.moko.web3.requests.getTransactionCount
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun DefaultNonceManager(web3Executor: Web3Executor): DefaultNonceManager = DefaultNonceManager(
    object : DefaultNonceManager.RemoteNonceProvider {
        override suspend fun loadNonce(walletAddress: WalletAddress): BigInt =
            web3Executor.getTransactionCount(walletAddress)
    }
)

class DefaultNonceManager(private val remoteNonceProvider: RemoteNonceProvider) : NonceManager {
    private val cache: MutableMap<WalletAddress, BigInt> = mutableMapOf()
    private val mutex = Mutex()

    override suspend fun <T> withNonce(
        walletAddress: WalletAddress,
        block: suspend (nonce: BigInt) -> NonceManager.NonceResult<T>
    ): T {
        mutex.withLock {
            val remoteNonce = remoteNonceProvider.loadNonce(walletAddress)
            val localNonce = cache[walletAddress] ?: remoteNonce

            val bestNonce = when (remoteNonce > localNonce) {
                true -> remoteNonce
                false -> localNonce
            }

            val result = block(bestNonce)

            when (result) {
                is NonceManager.NonceResult.Increase -> cache[walletAddress] = bestNonce + 1
                is NonceManager.NonceResult.Keep -> {}
            }

            return result.value
        }
    }

    interface RemoteNonceProvider {
        suspend fun loadNonce(walletAddress: WalletAddress): BigInt
    }
}
