import Foundation
import WalletCore

@objc public class SwiftCredentials: NSObject {
    
    @objc public let address: String

    private static let storeName: String = "SymbiosisStore"
    private static let coinType: CoinType = .ethereum
    private let privateKeyData: Data
    
    @objc public init(mnemonics: String) throws {
        guard let storedKey = StoredKey.importHDWallet(mnemonic: mnemonics, name: Self.storeName, password: Data(), coin: Self.coinType) else {
            throw SwiftWeb3Error.createCreateStoredKeyError()
        }
        guard let wallet = storedKey.wallet(password: Data()) else {
            throw SwiftWeb3Error.createCreateWalletError()
        }
        self.address = wallet.getAddressForCoin(coin: Self.coinType)
        self.privateKeyData = wallet.getKeyForCoin(coin: Self.coinType).data
    }
    
    @objc public init(privateKey: String) throws {
        guard let privateKeyData = privateKey.hexData,
              let publicKey = PrivateKey(data: privateKeyData)?.getPublicKeySecp256k1(compressed: false)
        else {
            throw SwiftWeb3Error.createPrivateKeyDataCreateError()
        }
        let address = AnyAddress.init(publicKey: publicKey, coin: Self.coinType)
        self.address = address.description
        self.privateKeyData = privateKeyData
    
    }
    
    @objc public func exportPrivateKeyData() -> Data {
        return privateKeyData
    }
}
