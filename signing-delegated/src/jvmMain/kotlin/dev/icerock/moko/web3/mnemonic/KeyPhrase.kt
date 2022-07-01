package dev.icerock.moko.web3.mnemonic

import org.web3j.crypto.MnemonicUtils
import java.security.SecureRandom

actual class KeyPhrase private actual constructor(actual val value: String) {
    actual companion object {
        private const val ENTROPY_SIZE = 16
        private val secureRandom = SecureRandom()

        actual fun generate(): KeyPhrase {
            val initialEntropy = ByteArray(ENTROPY_SIZE)
            secureRandom.nextBytes(initialEntropy)

            return MnemonicUtils.generateMnemonic(initialEntropy).let(::KeyPhrase)
        }

        actual fun wrapChecked(keyPhrase: String): KeyPhrase? =
            if (MnemonicUtils.validateMnemonic(keyPhrase)) KeyPhrase(keyPhrase) else null
    }
}
