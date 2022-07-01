package dev.icerock.moko.web3.internal.time

import platform.Foundation.NSDate

actual val timeMillis: Long get() = (NSDate().timeIntervalSinceReferenceDate() * 1000).toLong()
