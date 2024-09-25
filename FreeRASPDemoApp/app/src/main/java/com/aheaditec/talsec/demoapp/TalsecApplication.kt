package com.aheaditec.talsec.demoapp

import android.app.Application
import android.util.Log
import com.aheaditec.talsec_security.security.api.SuspiciousAppInfo
import com.aheaditec.talsec_security.security.api.Talsec
import com.aheaditec.talsec_security.security.api.TalsecConfig
import com.aheaditec.talsec_security.security.api.ThreatListener

class TalsecApplication : Application(), ThreatListener.ThreatDetected {

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
