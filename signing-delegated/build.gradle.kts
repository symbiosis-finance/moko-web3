plugins {
    id("multiplatform-library-convention")
    id("publication-convention")
    id("dev.icerock.mobile.multiplatform.cocoapods")
}

dependencies {
    commonMainApi(projects.web3)
    jvmMainImplementation(libs.web3j)
}

cocoaPods {
    pod("SwiftWeb3Wrapper")
}
