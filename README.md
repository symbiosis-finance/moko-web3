![moko-web3](https://user-images.githubusercontent.com/5010169/128702515-fc3928c7-d391-4234-9caa-15ab265cd7c1.png)  
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://img.shields.io/maven-central/v/dev.icerock.moko/web3) ](https://repo1.maven.org/maven2/dev/icerock/moko/web3/) ![kotlin-version](https://kotlin-version.aws.icerock.dev/kotlin-version?group=dev.icerock.moko&name=web3)

# Mobile Kotlin web3
This is a Kotlin MultiPlatform library that allow you to interact with ethereum networks by Web3 protocol.

## Table of Contents
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Samples](#samples)
- [Set Up Locally](#set-up-locally)
- [Contributing](#contributing)
- [License](#license)

## Features
...

## Requirements
- Gradle version 6.8+
- Android API 16+
- iOS version 11.0+

## Installation
root build.gradle  
```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/symbiosis-finance/maven")
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("TOKEN")
        }
    }
}
```

project build.gradle
```groovy
dependencies {
    commonMainApi("dev.icerock.moko:web3:1.0.0")
}
```

## Getting started

To start with, you should create `Web3Executor` instance.

```kotlin
val web3 = Web3("rpc url")
```

Now you can connect to the blockchain using this library. 

For example, a web3 call:

```kotlin
val balance = web3.getNativeBalance(WalletAddress("..."))
```

moko-web3 also supports **batching**:

```kotlin
val (firstBalance, secondBalance) = 
    web3.executeBatch(
        Web3Requests.getNativeBalance(WalletAddress("...")),
        Web3Requests.getNativeBalance(WalletAddress("..."))
    )
```

This will be retrieved in one HTTP request.

If you want to call a smart contract method, see an example [here](web3/src/commonMain/kotlin/dev.icerock.moko.web3/contract/param/README.md)

## Contributing
All development (both new features and bug fixes) is performed in `develop` branch. This way `master` sources always contain sources of the most recently released version. Please send PRs with bug fixes to `develop` branch. Fixes to documentation in markdown files are an exception to this rule. They are updated directly in `master`.

The `develop` branch is pushed to `master` during release.

More detailed guide for contributors see in [contributing guide](CONTRIBUTING.md).

## License
        
    Copyright 2021 Symbiosis Labs Ltd.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
