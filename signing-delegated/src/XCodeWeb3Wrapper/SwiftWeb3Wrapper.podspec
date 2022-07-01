Pod::Spec.new do |spec|
    spec.name                     = 'SwiftWeb3Wrapper'
    spec.version                  = '1.0.0'
    spec.homepage                 = 'Link'
    spec.source                   = { :git => "Not Published", :tag => "Cocoapods/#{spec.name}/#{spec.version}" }
    spec.authors                  = 'IceRock Development'
    spec.license                  = ''
    spec.summary                  = 'Objc Wrapper for web3swift pod'
    spec.module_name              = "#{spec.name}"

    spec.source_files             = "Sources/**/*.{h,m,swift}"

    spec.ios.deployment_target  = '12.0'
    spec.swift_version          = '5.0'

    spec.dependency 'TrustWalletCore', '2.7.0'
    spec.dependency 'MnemonicKit', '1.3.9'

    spec.pod_target_xcconfig = {
        'VALID_ARCHS' => '$(ARCHS_STANDARD_64_BIT)'
    }
end
