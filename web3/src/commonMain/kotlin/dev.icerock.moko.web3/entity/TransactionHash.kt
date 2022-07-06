/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.entity

import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.ParametrizedHexStringSerializer
import kotlinx.serialization.Serializable

object TransactionHashSerializer : ParametrizedHexStringSerializer<TransactionHash>(TransactionHash)

@Serializable(with = TransactionHashSerializer::class)
class TransactionHash(value: String) : Hex32String(value) {
    companion object : SizedFactory<TransactionHash> {
        override val size: Int = 32
        override fun createInstance(value: String) = TransactionHash(value)
    }
}
