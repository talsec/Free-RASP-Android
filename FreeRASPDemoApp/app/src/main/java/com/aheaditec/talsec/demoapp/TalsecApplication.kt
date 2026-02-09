package com.aheaditec.talsec.demoapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.aheaditec.talsec_security.security.api.ScreenProtector
import com.aheaditec.talsec_security.security.api.SuspiciousAppInfo
import com.aheaditec.talsec_security.security.api.Talsec
import com.aheaditec.talsec_security.security.api.TalsecConfig
import com.aheaditec.talsec_security.security.api.ThreatListener

class TalsecApplication : Application() {

    // Listener for security threat detection events (root, debugger, emulator, tampering, etc.)
    private val threatDetected = object : ThreatListener.ThreatDetected() {
        override fun onRootDetected() {
            // Set your reaction
            println("onRootDetected")
        }

        override fun onDebuggerDetected() {
            // Set your reaction
            // Triggered only in release build
            println("onDebuggerDetected")
        }

        override fun onEmulatorDetected() {
            // Set your reaction
            // Triggered only in release build
            println("onEmulatorDetected")
        }

        override fun onTamperDetected() {
            // Set your reaction
            // Triggered only in release build
            println("onTamperDetected")
        }

        override fun onUntrustedInstallationSourceDetected() {
            // Set your reaction
            // Triggered only in release build
            println("onUntrustedInstallationSourceDetected")
        }

        override fun onHookDetected() {
            // Set your reaction
            println("onHookDetected")
        }

        override fun onDeviceBindingDetected() {
            // Set your reaction
            println("onDeviceBindingDetected")
        }

        override fun onObfuscationIssuesDetected() {
            // Set your reaction
            println("onObfuscationIssuesDetected")
        }

        override fun onMalwareDetected(suspiciousApps: List<SuspiciousAppInfo>) {
            // Set your reaction
            println("onMalwareDetected")
            suspiciousApps.forEach {
                println("Suspicious app: ${it.packageInfo.packageName}, reason: ${it.reason}")
            }
        }

        override fun onAutomationDetected() {
            // Set your reaction
            println("onAutomationDetected")
        }

        override fun onScreenshotDetected() {
            // Set your reaction
            println("onScreenshotDetected")
        }

        override fun onScreenRecordingDetected() {
            // Set your reaction
            println("onScreenRecordingDetected")
        }

        override fun onMultiInstanceDetected() {
            // Set your reaction
            println("onMultiInstanceDetected")
        }

        override fun onUnsecureWifiDetected() {
            // Set your reaction
            println("onUnsecureWifiDetected")
        }

        override fun onTimeSpoofingDetected() {
            // Set your reaction
            println("onTimeSpoofingDetected")
        }

        override fun onLocationSpoofingDetected() {
            // Set your reaction
            println("onLocationSpoofingDetected")
        }
    }

    // This is optional. Use only if you are interested in device state information like device lock and HW backed keystore state
    private val deviceState = object : ThreatListener.DeviceState() {
        override fun onUnlockedDeviceDetected() {
            // Set your reaction
            println("onUnlockedDeviceDetected")
        }

        override fun onHardwareBackedKeystoreNotAvailableDetected() {
            // Set your reaction
            println("onHardwareBackedKeystoreNotAvailableDetected")
        }

        override fun onDeveloperModeDetected() {
            // Set your reaction
            println("onDeveloperModeDetected")
        }

        override fun onADBEnabledDetected() {
            // Set your reaction
            println("onADBEnabledDetected")
        }

        override fun onSystemVPNDetected() {
            // Set your reaction
            println("onSystemVPNDetected")
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
                ScreenProtector.INSTANCE.registerScreenCallbacks(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                ScreenProtector.INSTANCE.unregisterScreenCallbacks(activity)
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