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

# Usage
The installation guide will lead you through the following steps:
* [Add Talsec to Gradle](#step-1-add-talsec-to-your-gradle)
	+ [Dev vs Release version](#dev-vs-release-version)
* [Setup the configuration](#step-2-setup-the-configuration-for-your-app)
* [Handle detected threats](#step-3-handle-detected-threats)
* [Test it](#step-4-test-it)
* [Google Play Data Safety Policy](#step-5-google-plays-data-safety-policy)

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
    maven { url "https://developer.huawei.com/repo/" }
    maven { url "https://jitpack.io" }
}
```

Set release and debug dependencies in your :app module's `build.gradle`:
```gradle
[build.gradle (: app)]
...

dependencies {
    // Talsec Release
    releaseImplementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:6.0.0-release'
    // Talsec Debug
    debugImplementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:6.0.0-dev'
    ...
```

### Dev vs Release version
The Dev version is used to not complicate the development process of the application, e.g. if you would implement killing of the application on the debugger callback. It disables some checks which won't be triggered during the development process:
* Emulator
* Debugging
* Tampering
* Unofficial store


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
3. Setup the Configuration for your app. Set up with your values 😉.

You must get your expected signing certificate hashes in Base64 form.

You can go through [this manual](https://github.com/talsec/Free-RASP-Community/wiki/Getting-your-signing-certificate-hash-of-app) to learn how to sign your app in more detail, including manual signing and using Google's Play app signing.

Alternatively, you can use already prepared helper function `Log.e(..)` in the `onCreate()` to get a hash of the signing certificate easily. The `expectedSigningCertificateHashBase64` is an array of certificate hashes, as the support of multiple certificate hashes is included (e.g. if you are using a different certificate hash for Huawei App Gallery). The Helper functions are located in the `Utils.kt`:

```kt
[TalsecApplication.kt]

override fun onCreate() {
    super.onCreate()

    // Uncomment the following Log.e(...) to get your expectedSigningCertificateHashBase64
    // Copy the result from logcat and assign to expectedSigningCertificateHashBase64
    Log.e("SigningCertificateHash", Utils.computeSigningCertificateHash(this))
    ...
```
The value of watcherMail is automatically used as the target address for your security reports. Mail has a strict form `'name@domain.com'`. You can assign just `emptyArray()` to `supportedAlternativeStores` if you publish on the Google Play Store and Huawei AppGallery, as these are already included internally.
```kt
[TalsecApplication.kt]

companion object {
    private const val expectedPackageName = "com.aheaditec.talsec.demoapp" // Don't use Context.getPackageName!
    private const val expectedSigningCertificateHashBase64 = arrayOf(
        "mVr/qQLO8DKTwqlL+B1qigl9NoBnbiUs8b4c2Ewcz0k=",
        "cVr/qQLO8DKTwqlL+B1qigl9NoBnbiUs8b4c2Ewcz0m="
    ) // Replace with your release (!) signing certificate hashes
    private const val watcherMail = "john@example.com" // for Alerts and Reports
    private val supportedAlternativeStores = arrayOf(
        // Google Play Store and Huawei AppGallery are supported out of the box, you can pass empty array or null or add other stores like the Samsung's one:
        "com.sec.android.app.samsungapps" // Samsung Store
    )
}
```

```kt
[TalsecApplication.kt]

override fun onCreate() {
    ...

    // Uncomment the following Log.e(...) to get your expectedSigningCertificateHashBase64
    // Copy the result from logcat and assign to expectedSigningCertificateHashBase64 and
    //Log.e("SigningCertificateHash", Utils.computeSigningCertificateHash(this))

    val config = TalsecConfig(
        expectedPackageName,
        expectedSigningCertificateHashBase64,
        watcherMail,
        supportedAlternativeStores
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

### [Optional] Device state information
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
The easiest way to produce an incident (trigger local reaction check and create a record in security report) is to install a **release** build on an emulator (i.e., Android Emulator, which comes with Android Studio). Both app and freeRASP must be in release mode. You can also use a rooted Android device/emulator, in which case you create an incident even in debug mode.

**freeRASP copies build type of application:**
* application in debug mode = freeRASP in dev mode
* application in release mode = freeRASP in release mode

You can simply override this behavior to run release freeRASP in debug mode. In your project, navigate to `build.gradle`. At the bottom of the file, you should see:

```gradle
dependencies {

    ... some other imports ...
    
    // Talsec Release
    releaseImplementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:x.x.x-release'

    // Talsec Debug
    debugImplementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:x.x.x-dev'
}
```

You can edit those lines to import the dev and/or release version as you need. This can be used to trigger incidents during the development/testing phase:
```gradle
dependencies {

    ... some other imports ...
    
    // Just for testing of freeRASP reactions
    implementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:x.x.x-release'
}
```

## Step 5: Google Play's Data Safety Policy
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

None of the data collected by the freeRASP Talsec Security SDK is considered personal or sensitive. Also, there is no technical way to identify the real person by the identifiers collected by freeRASP SDK.

Please follow the recommendations and data collection specifications indicated [here](https://github.com/talsec/Free-RASP-Community#data-collection-processing-and-gdpr-compliance).

After installation, please go through this [checklist](https://github.com/talsec/Free-RASP-Community/wiki/Installation-checklist) to avoid potential issues or solve them quickly.

And you're done 🎉! You can open an issue if you get stuck anywhere in the guide or show your appreciation by starring this repository ⭐!
