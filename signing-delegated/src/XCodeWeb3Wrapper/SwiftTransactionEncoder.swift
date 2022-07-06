import Foundation
import WalletCore

@objc public class SwiftTransactionEncoder: NSObject {
    
    @objc static public func signTransaction(
        nonce: String,
        gasPrice: String,
        gasLimit: String,
        to: String,
        value: String,
        chainId: String,
        credentials: SwiftCredentials,
        data: String? = nil
    ) -> String {
        let data: Data = data != nil ? data!.toHexStringWithoutPrefix().hexData! : Data()
        
        let transactionToSign = EthereumSigningInput.with {
            $0.chainID = chainId.hexData!
            $0.nonce = nonce.hexData!
            $0.gasPrice = gasPrice.hexData!
            $0.gasLimit = gasLimit.hexData!
            $0.toAddress = to
            $0.transaction = EthereumTransaction.with {
                $0.transfer = EthereumTransaction.Transfer.with {
                    $0.amount = value.hexData!
                    $0.data = data
                }
            }
            $0.privateKey = credentials.exportPrivateKeyData()
        }
        
        let signedTransaction: EthereumSigningOutput = AnySigner.sign(input: transactionToSign, coin: .ethereum)
        return signedTransaction.encoded.hexString.prefixedHex
    }
    
    @objc static public func signTransaction(
        chainId: String,
        nonce: String,
        gasLimit: String,
        to: String,
        value: String,
        maxPriorityFeePerGas: String,
        maxFeePerGas: String,
        data: String? = nil,
        credentials: SwiftCredentials
    ) -> String {
        let data: Data = data != nil ? data!.toHexStringWithoutPrefix().hexData! : Data()
        
        let transactionToSign = EthereumSigningInput.with {
            $0.chainID = chainId.hexData!
            $0.nonce = nonce.hexData!
            $0.maxFeePerGas = maxFeePerGas.hexData!
            $0.maxInclusionFeePerGas = maxPriorityFeePerGas.hexData!
            $0.txMode = .enveloped
            $0.toAddress = to
            $0.gasLimit = gasLimit.hexData!
            $0.transaction = EthereumTransaction.with {
                $0.transfer = EthereumTransaction.Transfer.with {
                    $0.amount = Data(hexString: value)!
                    $0.data = data
                }
            }
            $0.privateKey = credentials.exportPrivateKeyData()
        }
        
        let signedTransaction: EthereumSigningOutput = AnySigner.sign(input: transactionToSign, coin: .ethereum)
        return signedTransaction.encoded.hexString.prefixedHex
    }
}


