import Foundation
import WalletCore
import MnemonicKit

extension String {
    var hexData: Data? {
        return Data(hexString: self.count % 2 == 0 ? self : "0\(self)")
    }
    
    var prefixedHex: String {
        return "0x\(self)"
    }
}

extension String {
    func toHexStringWithoutPrefix() -> String {
        if self.hasPrefix("0x") {
            return String(self.dropFirst(2))
        } else {
            return self
        }
    }
}
