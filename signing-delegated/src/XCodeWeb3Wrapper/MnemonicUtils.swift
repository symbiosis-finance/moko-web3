import Foundation
import WalletCore
import MnemonicKit

@objc public class MnemonicUtils: NSObject {
    @objc public static func generateMnemonics() -> String {
        // TODO: Generate mnemonics not implemeted in WalletCore
        return MnemonicKit.Mnemonic.generateMnemonic(strength: 128, language: .english)!
    }

    @objc public static func validateMnemonic(_ mnemonics: String) -> Bool {
        return Mnemonic.isValid(mnemonic: mnemonics)
    }
}
