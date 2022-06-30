package dev.icerock.moko.web3

import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.mnemonic.KeyPhrase
import dev.icerock.moko.web3.signing.Credentials

expect fun Credentials.Companion.createFromPrivateKey(privateKey: Hex32String): Credentials

fun Credentials.Companion.createFromPrivateKey(privateKey: String): Credentials? {
    val key = try { Hex32String(privateKey) } catch (_: Throwable) { return null }
    return createFromPrivateKey(key)
}

expect fun Credentials.Companion.createFromKeyPhrase(keyPhrase: KeyPhrase): Credentials

fun Credentials.Companion.createFromKeyPhrase(keyPhrase: String): Credentials? {
    val key = KeyPhrase.wrapChecked(keyPhrase) ?: return null
    return createFromKeyPhrase(key)
}

fun Credentials.Companion.createFromKeyPhraseOrPrivateKey(value: String): Credentials? =
    when (value.split(" ").size) {
        1 -> createFromPrivateKey(value)
        else -> createFromKeyPhrase(value)
    }
