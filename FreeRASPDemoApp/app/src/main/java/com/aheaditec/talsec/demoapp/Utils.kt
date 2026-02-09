package com.aheaditec.talsec.demoapp

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import java.security.MessageDigest

object Utils {

    private fun provideSignatureFlagsBaseOnSdk(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES
        }
    }

    /**
     * Computes the signing certificate hash of the current application.
     *
     * Returns a Base64-encoded SHA-256 hash of the app's signing certificate.
     * This value is used to initialize [TalsecConfig] for security validation.
     *
     * @param context The application context used to retrieve package information.
     * @return Base64-encoded SHA-256 hash of the first signing certificate.
     */
    fun computeSigningCertificateHash(context: Context): String {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            provideSignatureFlagsBaseOnSdk()
        )
        return getApkSigningCertificate(packageInfo)[0]
    }

    private fun getApkSigningCertificate(packageInfo: PackageInfo): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.let { signingInfo ->
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners?.map(::hashCertificate).orEmpty()
                } else {
                    signingInfo.signingCertificateHistory?.map(::hashCertificate).orEmpty()
                }
            }.orEmpty()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures?.map(::hashCertificate).orEmpty()
        }
    }

    private fun hashCertificate(signature: Signature): String {
        val hash = MessageDigest.getInstance("SHA-256").run {
            digest(signature.toByteArray())
        }
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}