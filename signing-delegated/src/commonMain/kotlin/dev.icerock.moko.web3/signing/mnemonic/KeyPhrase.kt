package dev.icerock.moko.web3.signing.mnemonic

expect class KeyPhrase private constructor(value: String) {
    val value: String

    companion object {
        fun generate(): KeyPhrase
        fun wrapChecked(keyPhrase: String): KeyPhrase?
    }
}