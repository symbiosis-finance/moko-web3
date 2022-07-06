/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract.param

import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.contract.StaticEncoder

object AddressParam : StaticEncoder<EthereumAddress> {
    override fun encode(item: EthereumAddress): ByteArray =
        UInt256Param.encode(item.bigInt)

    override fun decode(source: ByteArray): EthereumAddress =
        EthereumAddress.createInstance(UInt256Param.decode(source))
}
