package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.entity.TransactionHash
import dev.icerock.moko.web3.gas.GasConfiguration
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.nonce.NonceManager
import dev.icerock.moko.web3.requests.send
import dev.icerock.moko.web3.signing.Credentials
import dev.icerock.moko.web3.signing.unifiedSignContractTransaction

class WriteRequest(
    val contract: SmartContract,
    val callData: HexString,
    val value: BigInt
) {
    suspend fun estimateGas(from: EthereumAddress?): GasConfiguration {
        return contract.executor.gasProvider.estimate(
            from = from,
            to = contract.contractAddress,
            callData = callData,
            value = value
        )
    }

    suspend fun send(
        credentials: Credentials,
        nonce: BigInt? = null,
        gasConfiguration: GasConfiguration? = null
    ): TransactionHash {
        val actualGasConfiguration = gasConfiguration ?: estimateGas(credentials.address)

        if (nonce != null) {
            val signed = credentials.unifiedSignContractTransaction(
                nonce, contract.executor.chainId, contract.contractAddress,
                callData, value, actualGasConfiguration
            )
            return contract.executor.send(signed)
        }

        return contract.executor.nonceManager.withNonce(credentials.address) { actualNonce ->
            val signed = credentials.unifiedSignContractTransaction(
                actualNonce, contract.executor.chainId, contract.contractAddress,
                callData, value, actualGasConfiguration
            )
            return@withNonce NonceManager.NonceResult.Increase(contract.executor.send(signed))
        }
    }
}
