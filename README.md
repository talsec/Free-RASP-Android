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
    releaseImplementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:3.1.0-release'
    // Talsec Debug
    debugImplementation 'com.aheaditec.talsec.security:TalsecSecurity-Community:3.1.0-dev'
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
3. Setup the Configuration for your App. Set up with your values 😉 . You can uncomment prepared helper function `Log.e(..)` in the `onCreate()` to get expectedSigningCertificateHash easily (helper functions are in the `Utils.kt`):

```kt
[TalsecApplication.kt]

override fun onCreate() {
    super.onCreate()

    // Uncomment the following Log.e(...) to get your expectedSigningCertificateHash
    // Copy the result from logcat and assign to expectedSigningCertificateHash
    //Log.e("SigningCertificateHash", Utils.computeSigningCertificateHash(this))
    ...
```
The value of watcherMail is automatically used as target address for your security reports. Mail has a strict form `'name@domain.com'`. You can assign just `emptyArray()` to `supportedAlternativeStores` if you publish on the Google Play Store and Huawei AppGallery as these are already included internally.
```kt
[TalsecApplication.kt]

companion object {
    private const val expectedPackageName = "com.aheaditec.talsec.demoapp" // Don't use Context.getPackageName!
    private const val expectedSigningCertificateHash = "mVr/qQLO8DKTwqlL+B1qigl9NoBnbiUs8b4c2Ewcz0k=" // Replace with your release (!) signing certificate hash
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
    TODO("Not yet implemented")
}

override fun onEmulatorDetected() {
    // Set your reaction
    TODO("Not yet implemented")
}

override fun onTamperDetected() {
    // Set your reaction
    TODO("Not yet implemented")
}

override fun onUntrustedInstallationSourceDetected() {
    // Set your reaction
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

## Step 4: Google Play's User Data policy
Google Play’s User Data policy indicates that applications should inform users properly of the data that they are collecting and processing, and therefore rejects the apps which do not comply with the policy. To comply with the policy, in the App content section, under Data Safety, it is important to check following:
* Data types:
    * App Activity -> Installed apps
    * App info and performance -> Diagnostics, Other app performance data
    * Device or other identifiers -> Device or other identifiers
* Data usage and handling:
    * App Activity -> Installed apps -> Collected, Shared -> Fraud prevention, security, and compliance
    * App info and performance -> Diagnostics, Other app performance data -> Collected, Shared -> Fraud prevention, security, and compliance
    * Device or other identifiers -> Device or other identifiers -> Collected, Shared -> Fraud prevention, security, and compliance

It is also important to include the information in the privacy policy of the application, see the [Processed data and GDPR compliancy](https://github.com/talsec/Free-RASP-Community#processed-data-and-gdpr-compliancy).

Google Play’s User Data policy also indicates that a prominent disclosure should be presented to the users, in case of an app collecting personal or sensitive data. Therefore the application must include a disclosure screen, describing why the data is needed, what data, and how the data is used. [Link to best practices and guidelines](https://support.google.com/googleplay/android-developer/answer/11150561?hl=en&ref_topic=2364761)

An example of a disclosure screen:
<h1 align=left>
<img src="https://github.com/talsec/Free-RASP-Community/blob/master/visuals/disclosure_screen.png" width=25%>
</h1>

And you're done 🎉! You can open issue if you get stuck anywhere in the guide or show your appreciation by starring this repository ⭐!
