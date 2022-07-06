package dev.icerock.moko.web3.signing.mnemonic

import cocoapods.SwiftWeb3Wrapper.MnemonicUtils

actual class KeyPhrase private actual constructor(actual val value: String) {
    actual companion object {
        actual fun generate(): KeyPhrase {
            return KeyPhrase(MnemonicUtils.generateMnemonics())
        }

        actual fun wrapChecked(keyPhrase: String): KeyPhrase? =
            if (MnemonicUtils.validateMnemonic(keyPhrase)) KeyPhrase(keyPhrase) else null
    }
}