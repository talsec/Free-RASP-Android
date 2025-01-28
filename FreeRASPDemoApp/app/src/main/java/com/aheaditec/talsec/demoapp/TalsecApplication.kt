package com.aheaditec.talsec.demoapp

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager.SCREEN_RECORDING_STATE_VISIBLE
import com.aheaditec.talsec_security.security.api.SuspiciousAppInfo
import com.aheaditec.talsec_security.security.api.Talsec
import com.aheaditec.talsec_security.security.api.TalsecConfig
import com.aheaditec.talsec_security.security.api.ThreatListener
import java.util.function.Consumer

class TalsecApplication : Application(), ThreatListener.ThreatDetected {

    private var currentActivity: Activity? = null
    private var screenCaptureCallback: Activity.ScreenCaptureCallback? = null
    private val screenRecordCallback: Consumer<Int> = Consumer<Int> { state ->
        if (state == SCREEN_RECORDING_STATE_VISIBLE) {
            Talsec.onScreenRecordingDetected()
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Uncomment the following Log.e(...) to get your expectedSigningCertificateHashBase64
        // Copy the result from logcat and assign to expectedSigningCertificateHashBase64
        // Log.e("SigningCertificateHash", Utils.computeSigningCertificateHash(this))

        val config = TalsecConfig.Builder(
            expectedPackageName,
            expectedSigningCertificateHashBase64)
            .watcherMail(watcherMail)
            .supportedAlternativeStores(supportedAlternativeStores)
            .prod(isProd)
            .build()
        
        ThreatListener(this, deviceStateListener).registerListener(this)
        Talsec.start(this, config)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, bundle: Bundle?) {

                // Set to 'true' to block screen capture
                Talsec.blockScreenCapture(activity, false)
            }

            override fun onActivityStarted(activity: Activity) {
                unregisterCallbacks()
                currentActivity = activity
                registerCallbacks(activity)
            }

            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                if (activity == currentActivity) {
                    unregisterCallbacks()
                    currentActivity = null
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun registerCallbacks(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            screenCaptureCallback = Activity.ScreenCaptureCallback {
                Talsec.onScreenshotDetected()
            }
            activity.registerScreenCaptureCallback(
                baseContext.mainExecutor, screenCaptureCallback!!
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val initialState = activity.windowManager.addScreenRecordingCallback(
                mainExecutor, screenRecordCallback
            )
            screenRecordCallback.accept(initialState)
        }
    }

    private fun unregisterCallbacks() {
        currentActivity?.let { activity ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && screenCaptureCallback != null) {
                activity.unregisterScreenCaptureCallback(screenCaptureCallback!!)
                screenCaptureCallback = null
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                activity.windowManager.removeScreenRecordingCallback(screenRecordCallback)
            }
        }
    }

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

    override fun onMalwareDetected(p0: MutableList<SuspiciousAppInfo>?) {
        // Set your reaction
        println("onMalwareDetected")
    }

    override fun onScreenshotDetected() {
        println("onScreenshotDetected")
    }

    override fun onScreenRecordingDetected() {
        println("onScreenRecordingDetected")
    }

    // This is optional. Use only if you are interested in device state information like device lock and HW backed keystore state
    private val deviceStateListener = object : ThreatListener.DeviceState {
        override fun onUnlockedDeviceDetected() {
            // Set your reaction
            println("onUnlockedDeviceDetected")
        }

        override fun onHardwareBackedKeystoreNotAvailableDetected() {
            // Set your reaction
            println("onHardwareBackedKeystoreNotAvailableDetected")
        }

        override fun onDeveloperModeDetected() {
            println("onDeveloperModeDetected")
        }

        override fun onADBEnabledDetected() {
            println("onADBEnabledDetected")
        }

        override fun onSystemVPNDetected() {
            println("onSystemVPNDetected")
        }
    }

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
}
