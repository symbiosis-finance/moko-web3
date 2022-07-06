package dev.icerock.moko.web3.gas

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.hex.HexString

interface GasProvider {
    suspend fun estimate(
        from: EthereumAddress?,
        to: EthereumAddress,
        callData: HexString?,
        value: BigInt?,
    ): GasConfiguration
}
