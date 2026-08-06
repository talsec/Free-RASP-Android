package com.aheaditec.talsec.demoapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import app.talsec.rasp.security.api.ScreenProtector
import app.talsec.rasp.security.api.SuspiciousAppInfo
import app.talsec.rasp.security.api.Talsec
import app.talsec.rasp.security.api.TalsecConfig
import app.talsec.rasp.security.api.ThreatListener

class TalsecApplication : Application() {

    // Listener for security threat detection events (root, debugger, emulator, tampering, etc.)
    private val threatDetected = object : ThreatListener.ThreatDetected() {
        override fun onPrivilegedAccess() {
            // Set your reaction
            println("onPrivilegedAccess")
        }

        override fun onDebug() {
            // Set your reaction
            // Triggered only in release build
            println("onDebug")
        }

        override fun onSimulator() {
            // Set your reaction
            // Triggered only in release build
            println("onSimulator")
        }

        override fun onAppIntegrity() {
            // Set your reaction
            // Triggered only in release build
            println("onAppIntegrity")
        }

        override fun onUnofficialStore() {
            // Set your reaction
            // Triggered only in release build
            println("onUnofficialStore")
        }

        override fun onHooks() {
            // Set your reaction
            println("onHooks")
        }

        override fun onDeviceBinding() {
            // Set your reaction
            println("onDeviceBinding")
        }

        override fun onObfuscationIssues() {
            // Set your reaction
            println("onObfuscationIssues")
        }

        override fun onMalware(suspiciousApps: List<SuspiciousAppInfo>) {
            // Set your reaction
            println("onMalware")
            suspiciousApps.forEach {
                println("Suspicious app: ${it.packageInfo.packageName}, reasons: ${it.reasons}")
            }
        }

        override fun onAutomation() {
            // Set your reaction
            println("onAutomation")
        }

        override fun onScreenshot() {
            // Set your reaction
            println("onScreenshot")
        }

        override fun onScreenRecording() {
            // Set your reaction
            println("onScreenRecording")
        }

        override fun onMultiInstance() {
            // Set your reaction
            println("onMultiInstance")
        }

        override fun onUnsecureWifi() {
            // Set your reaction
            println("onUnsecureWifi")
        }

        override fun onTimeSpoofing() {
            // Set your reaction
            println("onTimeSpoofing")
        }

        override fun onLocationSpoofing() {
            // Set your reaction
            println("onLocationSpoofing")
        }

        override fun onBootloader() {
            // Set your reaction
            println("onBootloader")
        }
    }

    // This is optional. Use only if you are interested in device state information like device lock and HW backed keystore state
    private val deviceState = object : ThreatListener.DeviceState() {
        override fun onPasscode() {
            // Set your reaction
            println("onPasscode")
        }

        override fun onSecureHardwareNotAvailable() {
            // Set your reaction
            println("onSecureHardwareNotAvailable")
        }

        override fun onDevMode() {
            // Set your reaction
            println("onDevMode")
        }

        override fun onAdbEnabled() {
            // Set your reaction
            println("onAdbEnabled")
        }

        override fun onSystemVpn() {
            // Set your reaction
            println("onSystemVpn")
        }
    }

    // This is optional. Use only if you are interested in RASP execution state information
    private val raspExecutionState = object : ThreatListener.RaspExecutionState() {
        override fun onAllChecksFinished() {
            println("onAllChecksFinished")
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Uncomment the following Log.e(...) to get your expectedSigningCertificateHashBase64
        // Copy the result from logcat and assign to expectedSigningCertificateHashBase64
        // Log.e("SigningCertificateHash", Utils.computeSigningCertificateHash(this))

        val config = TalsecConfig.Builder(EXPECTED_PACKAGE_NAME, EXPECTED_SIGNING_CERTIFICATE_HASH_BASE64)
            .watcherMail(WATCHER_MAIL)
            .supportedAlternativeStores(SUPPORTED_ALTERNATIVE_STORES)
            .prod(IS_PROD)
            .killOnBypass(true) // determines if the app should be killed within the SDK if the callbacks are hooked/modified by an attacker
            .build()

        ThreatListener(threatDetected, deviceState, raspExecutionState).registerListener(this)
        Talsec.start(this, config)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                Talsec.blockScreenCapture(activity, false)
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                ScreenProtector.registerScreenCallbacks(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                ScreenProtector.unregisterScreenCallbacks(activity)
            }

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private companion object {
        private const val EXPECTED_PACKAGE_NAME = "com.aheaditec.talsec.demoapp" // Don't use Context.getPackageName!
        private val EXPECTED_SIGNING_CERTIFICATE_HASH_BASE64 = arrayOf(
            "mVr/qQLO8DKTwqlL+B1qigl9NoBnbiUs8b4c2Ewcz0k=",
            "cVr/qQLO8DKTwqlL+B1qigl9NoBnbiUs8b4c2Ewcz0m="
        ) // Replace with your release (!) signing certificate hashes
        private const val WATCHER_MAIL = "john@example.com" // for Alerts and Reports
        private val SUPPORTED_ALTERNATIVE_STORES = arrayOf(
            // Google Play Store and Huawei AppGallery are supported out of the box, you can pass empty array or null or add other stores like the Samsung's one:
            "com.sec.android.app.samsungapps" // Samsung Store
        )
        private const val IS_PROD = true
    }
}