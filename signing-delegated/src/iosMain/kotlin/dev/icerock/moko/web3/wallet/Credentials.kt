package dev.icerock.moko.web3.wallet

import cocoapods.SwiftWeb3Wrapper.SwiftCredentials
import dev.icerock.moko.web3.errors.toException
import dev.icerock.moko.web3.transaction.SwiftCredentialsTransactionSigner
import com.symbiosis.sdk.transaction.TransactionSigner
import dev.icerock.moko.web3.entity.WalletAddress
import dev.icerock.moko.web3.hex.Hex32String
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError

actual class Credentials actual constructor(
    actual val address: WalletAddress,
    actual val signer: TransactionSigner
) {
    constructor(credentials: SwiftCredentials) : this(
        WalletAddress(credentials.address()),
        SwiftCredentialsTransactionSigner(credentials)
    )

    actual companion object {
        actual fun createFromKeyPhrase(keyPhrase: KeyPhrase): Credentials {
            return createCredentials { error ->
                SwiftCredentials(mnemonics = keyPhrase.value, error = error)
            }
        }

        actual fun createFromPrivateKey(key: Hex32String): Credentials {
            return createCredentials { error ->
                SwiftCredentials(privateKey = key.withoutPrefix, error)
            }
        }

        private fun createCredentials(
            createBlock: (CPointer<ObjCObjectVar<NSError?>>) -> SwiftCredentials
        ): Credentials {
            val (result, error) = memScoped {
                val p = alloc<ObjCObjectVar<NSError?>>()
                val result: SwiftCredentials? = runCatching {
                    createBlock(p.ptr)
                }.getOrNull()
                result to p.value
            }

            if (error != null) throw error.toException()
            else return Credentials(result!!)
        }
    }
}
