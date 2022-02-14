# freeRASP for Android

freeRASP for Android is a part of security SDK for the app shielding and security monitoring. Learn more about provided features on the [freeRASP's main repository](https://github.com/talsec/Free-RASP-Community) first.

# Usage

We will guide you step-by-step, but you can always check the expected result in the demo app. This is how final files should look like: 
* [build.gradle (:app)](https://github.com/talsec/Free-RASP-Android/blob/master/FreeRASPDemoApp/app/build.gradle)
* [build.gradle (project)](https://github.com/talsec/Free-RASP-Android/blob/master/FreeRASPDemoApp/build.gradle)
* [TalsecApplication.kt](https://github.com/talsec/Free-RASP-Android/blob/master/FreeRASPDemoApp/app/src/main/java/com/aheaditec/talsec/demoapp/TalsecApplication.kt)
* [AndroidManifest.xml](https://github.com/talsec/Free-RASP-Android/blob/master/FreeRASPDemoApp/app/src/main/AndroidManifest.xml)

## Step 1: Add Talsec to your Gradle
Set our nexus artifact repository in your project's `build.gradle`:
```gradle
[build.gradle (NameOfProject)]
...

allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://nexus3-public.monetplus.cz/repository/ahead-talsec-free-rasp" }
    }
}
```

Set release and debug dependencies in your :app module's `build.gradle`:
```gradle
[build.gradle (:app)]
...

dependencies {
    // Talsec Release
    releaseImplementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:3.3.2-release'
    // Talsec Debug
    debugImplementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:3.3.2-dev'
    ...
```

### Dev vs. Release version
Dev version is used during the development of application. It separates development and production data and disables some checks which won't be triggered during development process:
* Emulator-usage
* Debugging
* Signing

## Step 2: Setup the Configuration for your App

1. Create arbitrary subclass of `Application()`, override it's `onCreate()` and implement interface of `ThreatListener.ThreatDetected`. You can of course use your Application subclass, if you already have one in your project.
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
3. Setup the Configuration for your App. Set up with your values 😉 . 

You must get your expected signing certificate hash (in Base64 form). You can *(if you use Google's Play App Signing)* follow [this manual](https://github.com/talsec/Free-RASP-Android/wiki/Getting-your-signing-certificate-hash-of-app). Alternatively, you can use already prepared helper function `Log.e(..)` in the `onCreate()` to get expectedSigningCertificateHashBase64 easily (helper functions are in the `Utils.kt`):

```kt
[TalsecApplication.kt]

override fun onCreate() {
    super.onCreate()

    // Uncomment the following Log.e(...) to get your expectedSigningCertificateHashBase64
    // Copy the result from logcat and assign to expectedSigningCertificateHashBase64
    Log.e("SigningCertificateHash", Utils.computeSigningCertificateHash(this))
    ...
```
The value of watcherMail is automatically used as target address for your security reports. Mail has a strict form `'name@domain.com'`. You can assign just `emptyArray()` to `supportedAlternativeStores` if you publish on the Google Play Store and Huawei AppGallery as these are already included internally.
```kt
[TalsecApplication.kt]

companion object {
    private const val expectedPackageName = "com.aheaditec.talsec.demoapp" // Don't use Context.getPackageName!
    private const val expectedSigningCertificateHashBase64 = "mVr/qQLO8DKTwqlL+B1qigl9NoBnbiUs8b4c2Ewcz0k=" // Replace with your release (!) signing certificate hash
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

    // Uncomment the following Log.e(...) to get your expectedSigningCertificateHash
    // Copy the result from logcat and assign to expectedSigningCertificateHash and
    //Log.e("SigningCertificateHash", Utils.computeSigningCertificateHash(this))

    val config = TalsecConfig(
        expectedPackageName,
        expectedSigningCertificateHash,
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
Implement methods of `ThreatListener.ThreatDetected`. For example you can kill app, warn user or send the event to your backend service.
```kt
[TalsecApplication.kt]

override fun onRootDetected() {
    // Set your reaction
    TODO("Not yet implemented")
}

override fun onDebuggerDetected() {
    // Set your reaction
    // Triggered only in release build
    TODO("Not yet implemented")
}

override fun onEmulatorDetected() {
    // Set your reaction
    // Triggered only in release build
    TODO("Not yet implemented")
}

override fun onTamperDetected() {
    // Set your reaction
    // Triggered only in release build
    TODO("Not yet implemented")
}

override fun onUntrustedInstallationSourceDetected() {
    // Set your reaction
    // Triggered only in release build
    TODO("Not yet implemented")
}

override fun onHookDetected() {
    // Set your reaction
    TODO("Not yet implemented")
}

override fun onDeviceBindingDetected() {
    // Set your reaction
    TODO("Not yet implemented")
}
```

## Step 4: Test it!
The easiest way to produce an incident (trigger local reaction check and create a record in security report) is to install a **release** build on an emulator (i.e., Android Emulator, which comes with Android Studio). Both app and freeRASP must be in release mode. You can also use a rooted Android device/emulator, in which case you create an incident even in debug mode.

**freeRASP copies build type of application:**
* application in debug mode = freeRASP in dev mode
* application in release mode = freeRASP in release mode

You can simply override this behaviour to run release freeRASP in debug mode. In your project, navigate to `build.gradle`. At the bottom of the file, you should see:

```
dependencies {

    ... some other imports ...
    
    // Talsec Release
    releaseImplementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:x.x.x-release'

    // Talsec Debug
    debugImplementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:x.x.x-dev'
}
```

You can edit those lines to import dev and/or release version as you need. This can be used to trigger incidents during the development/testing phase:
```
dependencies {

    ... some other imports ...
    
    // Just for testing of freeRASP reactions
    implementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:x.x.x-release'
}
```

## Step 5: Google Play's Data Safety Policy
By April 2022 [Google Play requires](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en) all app publishers to declare how they collect and handle user data for the apps they publish on Google Play. They should inform users properly of the data collected by the apps and how the data is shared and processed. Therefore, Google will reject the apps which do not comply with the policy.

Please follow the recommendations and data collection specifications indicated [here](https://github.com/talsec/Free-RASP-Community#data-collection-processing-and-gdpr-compliance).


And you're done 🎉! You can open issue if you get stuck anywhere in the guide or show your appreciation by starring this repository ⭐!
