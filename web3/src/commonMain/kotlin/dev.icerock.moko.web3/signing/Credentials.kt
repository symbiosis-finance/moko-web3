package dev.icerock.moko.web3.signing

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.gas.GasConfiguration
import dev.icerock.moko.web3.entity.WalletAddress
import dev.icerock.moko.web3.hex.HexString

sealed interface Credentials {
    val address: WalletAddress

    interface Local : Credentials {
        fun signTransferTransaction(
            nonce: BigInt,
            chainId: BigInt,
            to: WalletAddress,
            value: BigInt,
            gasConfiguration: GasConfiguration
        ): SignedTransaction
        fun signContractTransaction(
            nonce: BigInt,
            chainId: BigInt,
            to: ContractAddress,
            callData: HexString,
            value: BigInt,
            gasConfiguration: GasConfiguration
        ): SignedTransaction
    }

    interface Async : Credentials {
        suspend fun signTransferTransaction(
            nonce: BigInt,
            chainId: BigInt,
            to: WalletAddress,
            value: BigInt,
            gasConfiguration: GasConfiguration
        ): SignedTransaction
        suspend fun signContractTransaction(
            nonce: BigInt,
            chainId: BigInt,
            to: ContractAddress,
            callData: HexString,
            value: BigInt,
            gasConfiguration: GasConfiguration
        ): SignedTransaction
    }

    companion object
}

internal suspend fun Credentials.unifiedSignTransferTransaction(
    nonce: BigInt,
    chainId: BigInt,
    to: WalletAddress,
    value: BigInt,
    gasConfiguration: GasConfiguration
) = when (this) {
    is Credentials.Async -> signTransferTransaction(nonce, chainId, to, value, gasConfiguration)
    is Credentials.Local -> signTransferTransaction(nonce, chainId, to, value, gasConfiguration)
}

internal suspend fun Credentials.unifiedSignContractTransaction(
    nonce: BigInt,
    chainId: BigInt,
    to: ContractAddress,
    callData: HexString,
    value: BigInt,
    gasConfiguration: GasConfiguration
) = when (this) {
    is Credentials.Async -> signContractTransaction(nonce, chainId, to, callData, value, gasConfiguration)
    is Credentials.Local -> signContractTransaction(nonce, chainId, to, callData, value, gasConfiguration)
}
