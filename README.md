
<h1>
<img src="https://raw.githubusercontent.com/talsec/Free-RASP-Community/master/visuals/freeRASP.png" width=100%>
</h1>

# freeRASP for Android

FreeRASP for Android is a lightweight and easy-to-use mobile app protection and security monitoring SDK. It is designed to combat reverse engineering, tampering, or similar attack attempts. FreeRASP covers several attack vectors and enables you to set a response to each threat.

Android version detects security issues such as:
* App installed on a rooted device
* Hooking or running the app on the emulator
* Tampering with the application
* Attaching a debugger to the application

To learn more about freeRASP features, visit our main GitHub [repository](https://github.com/talsec/Free-RASP-Community).

# :notebook_with_decorative_cover: Table of contents

- [Usage](#usage)
  - [Step 1: Add Talsec to your Gradle](#step-1-add-talsec-to-your-gradle)
  - [Step 2: Setup the Configuration for your App](#step-2-setup-the-configuration-for-your-app)
    - [Dev vs Release version](#dev-vs-release-version)
  - [Step 3: Handle detected threats](#step-3-handle-detected-threats)
    - [(Optional) Device state information](#optional-device-state-information)
  - [Step 4: Test it!](#step-4-test-it)
  - [Step 5: Additional note about obfuscation](#step-5-additional-note-about-obfuscation)
  - [Step 6: Google Play's Data Safety Policy](#step-6-google-plays-data-safety-policy)
- [About Us](#about-us)
- [License](#license)

# Usage
The installation guide will lead you through the whole implementation, such as adding the SDK to the gradle, configuring it for your app, handling detected threats. It will also instruct you about required data safety policies.

You can check the expected result in the demo app. This is how final files should look like: 
* [build.gradle (:app)](https://github.com/talsec/Free-RASP-Android/blob/master/FreeRASPDemoApp/app/build.gradle)
* [build.gradle (project)](https://github.com/talsec/Free-RASP-Android/blob/master/FreeRASPDemoApp/build.gradle)
* [TalsecApplication.kt](https://github.com/talsec/Free-RASP-Android/blob/master/FreeRASPDemoApp/app/src/main/java/com/aheaditec/talsec/demoapp/TalsecApplication.kt)
* [AndroidManifest.xml](https://github.com/talsec/Free-RASP-Android/blob/master/FreeRASPDemoApp/app/src/main/AndroidManifest.xml)

## Step 1: Add Talsec to your Gradle
Set our nexus artifact repository in your project's `build.gradle` (or `settings.gradle` if you are using settings repositories):
```gradle
[build.gradle (NameOfProject)]
...
repositories {
    google()
    mavenCentral()
    maven { url "https://nexus3-public.monetplus.cz/repository/ahead-talsec-free-rasp" }
    maven { url "https://jitpack.io" }
}
```

Set release and debug dependencies in your :app module's `build.gradle`:
```gradle
[build.gradle (: app)]
...

dependencies {
    // freeRASP SDK  
    implementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:7.0.0'
    ...
```


## Step 2: Setup the Configuration for your App

1. Create arbitrary subclass of `Application()`, override it's `onCreate()` and implement interface of `ThreatListener.ThreatDetected`. You can, of course, use your Application subclass if you already have one in your project.
```kt
[TalsecApplication.kt]

class TalsecApplication : Application(), ThreatListener.ThreatDetected {
    override fun onCreate() {
        super.onCreate()
    }
}
```

2. Add this new subclass to `AndroidManifest.xml`" inside `<application>` tag:
```xml
[AndroidManifest.xml]

<application
    android:name=".TalsecApplication"
    ...
```
3. Setup the Configuration for your app with your values 😉.

You must get your expected signing certificate hashes in Base64 form. You can go through [this manual](https://github.com/talsec/Free-RASP-Community/wiki/Getting-your-signing-certificate-hash-of-app) to learn how to sign your app in more detail, including manual signing and using Google's Play app signing. Alternatively, you can use already prepared helper function `Log.e(..)` in the `onCreate()` to get a hash of the signing certificate easily. The `expectedSigningCertificateHashBase64` is an array of certificate hashes, as the support of multiple certificate hashes is included (e.g. if you are using a different certificate hash for Huawei App Gallery). The Helper functions are located in the `Utils.kt`:

```kt
[TalsecApplication.kt]

override fun onCreate() {
    super.onCreate()

    // Uncomment the following Log.e(...) to get your expectedSigningCertificateHashBase64
    // Copy the result from logcat and assign to expectedSigningCertificateHashBase64
    // Log.e("SigningCertificateHash", Utils.computeSigningCertificateHash(this))
    ...
```

The value of `expectedPackageName` is self-explanatory.

The value of `watcherMail` is automatically used as the target address for your security reports. Mail has a strict form `'name@domain.com'`. 

You can assign just `emptyArray()` to `supportedAlternativeStores` if you publish on the Google Play Store and Huawei AppGallery, as these are already included internally. Otherwise add package names of the alternative stores.

`isProd`  defaults to  `true`  when undefined. If you want to use the Dev version to disable checks described  [in the chapter below](https://github.com/talsec/Free-RASP-Android#dev-vs-release-version), set the parameter to  `false`. Make sure that you have the Release version in the production (i.e. isProd set to true)!. To simplify switching between debug and release version of Talsec based on the build type, you can use `BuildConfig.BUILD_TYPE.contains("Release", true)` as a value for `isProd`.
```kt
[TalsecApplication.kt]

companion object {
    private const val expectedPackageName = "com.aheaditec.talsec.demoapp" // Don't use Context.getPackageName!
    private val expectedSigningCertificateHashBase64 = arrayOf(
        "mVr/qQLO8DKTwqlL+B1qigl9NoBnbiUs8b4c2Ewcz0k=",
        "cVr/qQLO8DKTwqlL+B1qigl9NoBnbiUs8b4c2Ewcz0m="
    ) // Replace with your release (!) signing certificate hashes
    private const val watcherMail = "john@example.com" // for Alerts and Reports
    private val supportedAlternativeStores = arrayOf(
        // Google Play Store and Huawei AppGallery are supported out of the box, you can pass empty array or null or add other stores like the Samsung's one:
        "com.sec.android.app.samsungapps" // Samsung Store
    )
    private val isProd = true
}
```

```kt
[TalsecApplication.kt]

override fun onCreate() {
    ...

    // Uncomment the following Log.e(...) to get your expectedSigningCertificateHashBase64
    // Copy the result from logcat and assign to expectedSigningCertificateHashBase64 and
    // Log.e("SigningCertificateHash", Utils.computeSigningCertificateHash(this))

    val config = TalsecConfig(
        expectedPackageName,
        expectedSigningCertificateHashBase64,
        watcherMail,
        supportedAlternativeStores,
        isProd
    )
```

4. Initiate ThreatListener and start Talsec just by adding these two lines below the created config:
```kt
[TalsecApplication.kt]

override fun onCreate() {
    ...

    ThreatListener(this).registerListener(this)
    Talsec.start(this, config)
}
```

### Dev vs Release version
The Dev version is used to not complicate the development process of the application, e.g. if you would implement killing of the application on the debugger callback. It disables some checks which won't be triggered during the development process:
* Emulator
* Debugging
* Tampering
* Unofficial store

## Step 3: Handle detected threats
Implement methods of `ThreatListener.ThreatDetected`. For example, you can kill the app, warn the user or send the event to your backend service. If you decide to kill the application from the callback, make sure that you use an appropriate way of killing it.

To learn more about these checks, visit our [wiki](https://github.com/talsec/Free-RASP-Community/wiki/Threat-detection) page that provides an explanation for them.

```kt
[TalsecApplication.kt]

override fun onRootDetected() {
    TODO("Not yet implemented")
}

override fun onDebuggerDetected() {
    TODO("Not yet implemented")
}

override fun onEmulatorDetected() {
    TODO("Not yet implemented")
}

override fun onTamperDetected() {
    TODO("Not yet implemented")
}

override fun onUntrustedInstallationSourceDetected() {
    TODO("Not yet implemented")
}

override fun onHookDetected() {
    TODO("Not yet implemented")
}

override fun onDeviceBindingDetected() {
    TODO("Not yet implemented")
}
```

### (Optional) Device state information
Optionally you can use a device state listener to get additional information about device state information like device lock and HW-backed Keystore state.
 

```kt
private val deviceStateListener = object : ThreatListener.DeviceState {
    override fun onUnlockedDeviceDetected() {
        // Set your reaction
        TODO("Not yet implemented")
    }
    override fun onHardwareBackedKeystoreNotAvailableDetected() {
        // Set your reaction
        TODO("Not yet implemented")
    }
}
```
and modify initialization of ThreatListener:
```kt
    ...

    ThreatListener(this, deviceStateListener).registerListener(this)
    Talsec.start(this, config)
```

## Step 4: Test it!
The easiest way to produce an incident (trigger local reaction check and create a record in security report) is to install a **release** build on an emulator (i.e., Android Emulator, which comes with Android Studio). Make sure, that you have set up the **isProd** variable to **true**.

## Step 5: Additional note about obfuscation
The freeRASP contains public API, so the integration process is as simple as possible. Unfortunately, this public API also creates opportunities for the attacker to use publicly available information to interrupt freeRASP operations or modify your custom reaction implementation in threat callbacks. In order for freeRASP to be as effective as possible, it is highly recommended to apply obfuscation to the final package/application, making the public API more difficult to find and also partially randomized for each application so it cannot be automatically abused by generic hooking scripts.

The majority of Android projects support code shrinking and obfuscation without any additional need for setup. The owner of the project can define the set of rules that are usually automatically used when the application is built in the release mode. For more information, please visit the official documentation
* https://developer.android.com/studio/build/shrink-code 
* https://www.guardsquare.com/manual/configuration/usage

You can make sure, that the obfuscation is enabled by checking the value of **minifyEnabled** property in your **module's build.gradle** file.
```gradle
android {
    ...

    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}
```

## Step 6: Google Play's Data Safety Policy

See the generic info about freeRASP data collection [here](https://github.com/talsec/Free-RASP-Community/tree/master#data-collection-processing-and-gdpr-compliance).


[Google Play requires](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en) all app publishers to declare how they collect and handle user data for the apps they publish on Google Play. They should inform users properly of the data collected by the apps and how the data is shared and processed. Therefore, Google will reject the apps which do not comply with the policy.

Talsec recommends adding the following statements to the Privacy Policy page dedicated to your app. Also, use the text below while filling in the Google Play Safety Section for publishing.

<i>
For the purpose of Fraud prevention, user safety, and compliance, the dedicated App safety SDK needs to send the following anonymous diagnostic data off the device for detection of security issues. Thus the application collects the following data:

* Category: App info and performance
    * Data Type: Diagnostics
    * Information about the integrity of the app and the operating system. For example, rooting, running in an emulator, hooking framework usage, etc...
* Category: Device or other identifiers
    * Data Type: Device or other identifiers
    * Information that relates to an individual device. For example, a device model and anonymous identifier to control that app instance executed on the original device that it was initially installed on. It is needed to combat threats like bots and API abuse.
</i>

All the data collected by the freeRASP Talsec Security SDK is considered non user sensitive. Also, there is no technical way to identify the real person by the identifiers collected by freeRASP SDK.

Please follow the recommendations and data collection specifications indicated [here](https://github.com/talsec/Free-RASP-Community#data-collection-processing-and-gdpr-compliance).

After installation, please go through this [checklist](https://github.com/talsec/Free-RASP-Community/wiki/Installation-checklist) to avoid potential issues or solve them quickly.

And you're done 🎉! You can open an issue if you get stuck anywhere in the guide or show your appreciation by starring this repository ⭐!

# About Us
Talsec is an academic-based and community-driven mobile security company. We deliver in-App Protection and a User Safety suite for Fintechs. We aim to bridge the gaps between the user's perception of app safety and the strong security requirements of the financial industry. 

Talsec offers a wide range of security solutions, such as App and API protection SDK, Penetration testing, monitoring services, and the User Safety suite. You can check out offered products at [our web](https://www.talsec.app).

# License
This project is provided as freemium software i.e. there is a fair usage policy that impose some limitations on the free usage. The SDK software consists of opensource and binary part which is property of Talsec. The opensource part is  licensed under the MIT License - see the [LICENSE](https://github.com/talsec/Free-RASP-Community/blob/master/LICENSE) file for details.
