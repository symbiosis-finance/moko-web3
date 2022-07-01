package dev.icerock.moko.web3.transaction

import cocoapods.SwiftWeb3Wrapper.SwiftCredentials
import cocoapods.SwiftWeb3Wrapper.SwiftTransactionEncoder
import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.errors.toException
import com.symbiosis.sdk.gas.GasConfiguration
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.WalletAddress
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError

class SwiftCredentialsTransactionSigner (
    private val credentials: SwiftCredentials
) : TransactionSigner.Blocking {

    override fun signTransferTransactionBlocking(
        nonce: BigInt,
        chainId: BigInt,
        to: WalletAddress,
        value: BigInt,
        gasConfiguration: GasConfiguration
    ): SignedTransaction {
        val encoded = when (gasConfiguration) {
            is GasConfiguration.Legacy -> encoderAction {
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
            }
            is GasConfiguration.EIP1559 -> encoderAction {
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
        }
        return SignedTransaction(encoded)
    }

    override fun signContractTransactionBlocking(
        nonce: BigInt,
        chainId: BigInt,
        to: ContractAddress,
        contractData: String,
        value: BigInt,
        gasConfiguration: GasConfiguration
    ): SignedTransaction {
        val encoded = when (gasConfiguration) {
            is GasConfiguration.Legacy -> encoderAction {
                SwiftTransactionEncoder.signTransactionWithNonce(
                    nonce = nonce.toHexString(),
                    gasPrice = gasConfiguration.gasPrice.toHexString(),
                    gasLimit = gasConfiguration.gasLimit.toHexString(),
                    to = to.prefixed,
                    value = value.toHexString(),
                    chainId = chainId.toHexString(),
                    credentials = credentials,
                    data = contractData
                )
            }
            is GasConfiguration.EIP1559 -> encoderAction {
                SwiftTransactionEncoder.signTransactionWithChainId(
                    chainId = chainId.toHexString(),
                    nonce = nonce.toHexString(),
                    gasLimit = gasConfiguration.gasLimit.toHexString(),
                    to = to.prefixed,
                    value = value.toHexString(),
                    data = contractData,
                    maxPriorityFeePerGas = gasConfiguration.maxPriorityFeePerGas.toHexString(),
                    maxFeePerGas = gasConfiguration.maxFeePerGas.toHexString(),
                    credentials = credentials
                )
            }
        }

        return SignedTransaction(encoded)
    }

    private fun encoderAction(
        action: (CPointer<ObjCObjectVar<NSError?>>) -> String?
    ): String {
        val (result, error) = memScoped {
            val p = alloc<ObjCObjectVar<NSError?>>()
            val result: String? = runCatching {
                action(p.ptr)
            }.getOrNull()
            result to p.value
        }
        if (error != null) throw error.toException()
        else return result!!
    }

    private fun BigInt.toHexString() = this.toString(radix = 16)
}
