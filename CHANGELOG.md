# Change Log
All notable changes to this project will be documented in this file.

## [3.0.0] - 2021-10-25
This version improves granularity of detected threat types.

### Added
- added new threat callback **'onUntrustedInstallationSourceDetected'**, which was previously part of onTamperDetected callback

### Changed
- changed threat callback from **'onFingerprintDetected'** to more understandable **'onDeviceBindingDetected'**
- increased min SDK version from 19 to 21
- increased target/compile SDK version from 29 to 31
- increased Kotlin and Gradle versions

### Fixed
- support for direct ADB side-loading (check *TalsecApplication.kt -> supportedAlternativeStores*)
- fixed a bug in a native method which caused crash on a one specific device
- fixed a false positive detection of an emulator (TECNO CD7)
- fixed a bug with a negative timeMs during run time check computation (fixes a logging)