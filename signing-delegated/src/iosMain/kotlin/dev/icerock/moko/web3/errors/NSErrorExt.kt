package dev.icerock.moko.web3.errors

import cocoapods.SwiftWeb3Wrapper.SwiftWeb3Error
import platform.Foundation.NSError

internal fun NSError?.toException(): Exception {
    if (this == null) return NullPointerException()
    if (this.domain != SwiftWeb3Error.errorDomain())
        return Exception(this.description)

    return when(this.code) {
        else -> return Exception()
    }
}