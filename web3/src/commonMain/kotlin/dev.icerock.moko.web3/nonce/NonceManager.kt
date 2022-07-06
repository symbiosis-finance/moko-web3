package dev.icerock.moko.web3.nonce

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.entity.WalletAddress

interface NonceManager {

    sealed interface NonceResult<T> {
        val value: T
        class Increase<T>(override val value: T) : NonceResult<T>
        class Keep<T>(override val value: T) : NonceResult<T>
    }

    // block returns if local nonce should be increased
    suspend fun <T> withNonce(walletAddress: WalletAddress, block: suspend (nonce: BigInt) -> NonceResult<T>): T
}
