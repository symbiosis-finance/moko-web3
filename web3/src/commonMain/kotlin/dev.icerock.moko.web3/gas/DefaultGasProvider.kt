package dev.icerock.moko.web3.gas

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.requests.getEstimateGas
import dev.icerock.moko.web3.requests.getGasPrice

class DefaultGasProvider(private val web3Executor: Web3Executor) : GasProvider {
    private suspend fun gasPrice(): BigInt = web3Executor.getGasPrice()
    private suspend fun gasLimit(
        from: EthereumAddress?,
        to: EthereumAddress,
        gasPrice: BigInt?,
        callData: HexString?,
        value: BigInt?
    ): BigInt = web3Executor.getEstimateGas(
        from = from,
        gasPrice = gasPrice,
        to = to,
        callData = callData,
        value = value
    )

    override suspend fun estimate(
        from: EthereumAddress?,
        to: EthereumAddress,
        callData: HexString?,
        value: BigInt?,
    ): GasConfiguration {
        val gasPrice = gasPrice()

        return GasConfiguration.Legacy(
            gasPrice = gasPrice,
            gasLimit = gasLimit(
                from = from,
                to = to,
                gasPrice = gasPrice,
                callData = callData,
                value = value
            )
        )
    }
}