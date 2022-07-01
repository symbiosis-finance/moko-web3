package dev.icerock.moko.web3

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.entity.WalletAddress
import dev.icerock.moko.web3.gas.GasConfiguration
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.mnemonic.KeyPhrase
import dev.icerock.moko.web3.signing.Credentials
import dev.icerock.moko.web3.signing.SignedTransaction
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.utils.Numeric
import java.math.BigInteger
import org.web3j.crypto.Credentials as Web3jCredentialsUnderlying

class Web3jCredentials(private val underlying: Web3jCredentialsUnderlying) : Credentials.Local {

    constructor(privateKey: Hex32String) : this(Web3jCredentialsUnderlying.create(privateKey.withoutPrefix))
    constructor(keyPhrase: KeyPhrase) : this(createWeb3jUnderlyingFromKeyPhrase(keyPhrase))

    override fun signTransferTransaction(
        nonce: BigInt,
        chainId: BigInt,
        to: WalletAddress,
        value: BigInt,
        gasConfiguration: GasConfiguration
    ): SignedTransaction {
        val transaction = when (gasConfiguration) {
            is GasConfiguration.Legacy -> RawTransaction.createEtherTransaction(
                BigInteger(nonce.toString()),
                BigInteger(gasConfiguration.gasPrice.toString()),
                BigInteger(gasConfiguration.gasLimit.toString()),
                to.withoutPrefix,
                BigInteger(value.toString())
            )
            is GasConfiguration.EIP1559 -> RawTransaction.createEtherTransaction(
                chainId.toString().toLong(),
                BigInteger(nonce.toString()),
                BigInteger(gasConfiguration.gasLimit.toString()),
                to.withoutPrefix,
                BigInteger(value.toString()),
                BigInteger(gasConfiguration.maxPriorityFeePerGas.toString()),
                BigInteger(gasConfiguration.maxFeePerGas.toString())
            )
        }
        val encoded = TransactionEncoder.signMessage(
            /* rawTransaction = */transaction,
            /* chainId = */chainId.toString().toLong(),
            /* credentials = */underlying
        )
        return SignedTransaction(HexString(Numeric.toHexString(encoded)))
    }

    override fun signContractTransaction(
        nonce: BigInt,
        chainId: BigInt,
        to: ContractAddress,
        callData: HexString,
        value: BigInt,
        gasConfiguration: GasConfiguration
    ): SignedTransaction {
        val transaction = when (gasConfiguration) {
            is GasConfiguration.Legacy -> RawTransaction.createTransaction(
                BigInteger(nonce.toString()),
                BigInteger(gasConfiguration.gasPrice.toString()),
                BigInteger(gasConfiguration.gasLimit.toString()),
                to.withoutPrefix,
                BigInteger(value.toString()),
                callData.prefixed
            )
            is GasConfiguration.EIP1559 -> RawTransaction.createTransaction(
                chainId.toString().toLong(),
                BigInteger(nonce.toString()),
                BigInteger(gasConfiguration.gasLimit.toString()),
                to.withoutPrefix,
                BigInteger(value.toString()),
                callData.prefixed,
                BigInteger(gasConfiguration.maxPriorityFeePerGas.toString()),
                BigInteger(gasConfiguration.maxFeePerGas.toString())
            )
        }
        val encoded = TransactionEncoder.signMessage(
            /* rawTransaction = */transaction,
            /* chainId = */chainId.toString().toLong(),
            /* credentials = */underlying
        )
        return SignedTransaction(HexString(encoded))
    }

    override val address: WalletAddress = WalletAddress(underlying.address)
}

private fun createWeb3jUnderlyingFromKeyPhrase(keyPhrase: KeyPhrase): Web3jCredentialsUnderlying {
    // https://github.com/web3j/web3j/issues/932
    // "m/44'/60'/0'/0/0"
    val path = @Suppress("MagicNumber") intArrayOf(
        44 or Bip32ECKeyPair.HARDENED_BIT,
        60 or Bip32ECKeyPair.HARDENED_BIT,
        0 or Bip32ECKeyPair.HARDENED_BIT,
        0,
        0
    )

    val seed = MnemonicUtils.generateSeed(keyPhrase.value, null)
    val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
    val bip44Keypair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)

    return Web3jCredentialsUnderlying.create(bip44Keypair)
}
