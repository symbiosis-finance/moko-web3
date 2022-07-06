import Foundation

@objc public class SwiftWeb3Error: NSError {
    @objc public static let errorCreateWalletCode: Int = 1
    @objc public static let errorCreateStoredKeyCode: Int = 2
    @objc public static let errorCreatePrivateKeyDataCode: Int = 3
    @objc public static let errorDomain: String = "SwiftWeb3ErrorDomain"
    
    static func createCreateWalletError() -> NSError {
        return NSError(
            domain: Self.errorDomain,
            code: Self.errorCreateWalletCode,
            userInfo: nil
        )
    }
    
    static func createCreateStoredKeyError() -> NSError {
        return NSError(
            domain: Self.errorDomain,
            code: Self.errorCreateStoredKeyCode,
            userInfo: nil
        )
    }
    
    static func createPrivateKeyDataCreateError() -> NSError {
        return NSError(
            domain: Self.errorDomain,
            code: Self.errorCreatePrivateKeyDataCode,
            userInfo: nil
        )
    }
}
