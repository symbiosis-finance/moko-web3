package dev.icerock.moko.web3.signing

import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.signing.mnemonic.KeyPhrase

actual fun Credentials.Companion.createFromPrivateKey(privateKey: Hex32String): Credentials =
    Web3jCredentials(privateKey)

actual fun Credentials.Companion.createFromKeyPhrase(keyPhrase: KeyPhrase): Credentials =
    Web3jCredentials(keyPhrase)