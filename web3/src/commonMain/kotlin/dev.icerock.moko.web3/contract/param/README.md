# Params

This is a tutorial how to call smartcontract methods.

First, you need to create an instance of smart contract:

```kotlin
val smartContract = SmartContract(
    web3 = Web3(...),
    contractAddress = ContractAddress(...),
    abiJson = ...  // deserialized abi using kotlinx-serialization
)
```

Then you can call different methods. Here is an example of balanceOf request from ERC20 token.

```kotlin
smartContract.read(
    method = "balanceOf",
    params = listOf(WalletAddress("..."))
) { result: List<Any?> ->
    result[0] as BigInt
}
```

This call would be correct for ABI below:

```json
{
    "constant": true,
    "inputs": [
        {
            "name": "_owner",
            "type": "address"
        }
    ],
    "name": "balanceOf",
    "outputs": [
        {
            "name": "balance",
            "type": "uint256"
        }
    ],
    "payable": false,
    "stateMutability": "view",
    "type": "function"
}
```

To pass valid params and make proper casts, you should know something about type mappings:

| solidity type               | kotlin type     | encoder         |
|-----------------------------|-----------------|-----------------|
| bool                        | Boolean         | BoolParam       |
| address                     | EthereumAddress | AddressParam    |
| uint\[N] (any size)         | BigInt          | UInt256Param    |
| bytes\[N] (any size)        | ByteArray       | SizedBytesParam |
| bytes (dynamic size)        | ByteArray       | BytesParam      |
| uint[], bool[], etc. (list) | List<Any?>      | ListParam       |
| string                      | String          | StringParam     |

