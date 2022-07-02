package dev.icerock.moko.web3.signing

import cocoapods.SwiftWeb3Wrapper.SwiftCredentials
import cocoapods.SwiftWeb3Wrapper.SwiftTransactionEncoder
import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.entity.WalletAddress
import dev.icerock.moko.web3.gas.GasConfiguration
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.signing.mnemonic.KeyPhrase

class TrustWalletCredentials(private val credentials: SwiftCredentials) : Credentials.Local {
    constructor(privateKey: Hex32String) :
            this(SwiftCredentials(privateKey.withoutPrefix, error = null))

    constructor(keyPhrase: KeyPhrase) :
            this(SwiftCredentials(mnemonics = keyPhrase.value, error = null))

    override fun signTransferTransaction(
        nonce: BigInt,
        chainId: BigInt,
        to: WalletAddress,
        value: BigInt,
        gasConfiguration: GasConfiguration
    ): SignedTransaction {
        val encoded = when (gasConfiguration) {
            is GasConfiguration.Legacy ->
                SwiftTransactionEncoder.signTransactionWithNonce(
                    nonce = nonce.toHexString(),
                    gasPrice = gasConfiguration.gasPrice.toHexString(),
                    gasLimit = gasConfiguration.gasLimit.toHexString(),
                    to = to.prefixed,
                    value = value.toHexString(),
                    chainId = chainId.toHexString(),
                    credentials = credentials,
                    data = null
                )
            is GasConfiguration.EIP1559 ->
                SwiftTransactionEncoder.signTransactionWithChainId(
                    chainId = chainId.toHexString(),
                    nonce = nonce.toHexString(),
                    gasLimit = gasConfiguration.gasLimit.toHexString(),
                    to = to.prefixed,
                    value = value.toHexString(),
                    data = null,
                    maxPriorityFeePerGas = gasConfiguration.maxPriorityFeePerGas.toHexString(),
                    maxFeePerGas = gasConfiguration.maxFeePerGas.toHexString(),
                    credentials = credentials
                )
        }
        return SignedTransaction(HexString(encoded))
    }

    override fun signContractTransaction(
        nonce: BigInt,
        chainId: BigInt,
        to: ContractAddress,
        callData: HexString,
        value: BigInt,
        gasConfiguration: GasConfiguration
    ): SignedTransaction {
        val encoded = when (gasConfiguration) {
            is GasConfiguration.Legacy ->
                SwiftTransactionEncoder.signTransactionWithNonce(
                    nonce = nonce.toHexString(),
                    gasPrice = gasConfiguration.gasPrice.toHexString(),
                    gasLimit = gasConfiguration.gasLimit.toHexString(),
                    to = to.prefixed,
                    value = value.toHexString(),
                    chainId = chainId.toHexString(),
                    credentials = credentials,
                    data = callData.withoutPrefix
                )
            is GasConfiguration.EIP1559 ->
                SwiftTransactionEncoder.signTransactionWithChainId(
                    chainId = chainId.toHexString(),
                    nonce = nonce.toHexString(),
                    gasLimit = gasConfiguration.gasLimit.toHexString(),
                    to = to.prefixed,
                    value = value.toHexString(),
                    data = callData.withoutPrefix,
                    maxPriorityFeePerGas = gasConfiguration.maxPriorityFeePerGas.toHexString(),
                    maxFeePerGas = gasConfiguration.maxFeePerGas.toHexString(),
                    credentials = credentials
                )
        }

        return SignedTransaction(HexString(encoded))
    }

    private fun BigInt.toHexString() = HexString(this).withoutPrefix

    override val address: WalletAddress = WalletAddress(credentials.address())
}
