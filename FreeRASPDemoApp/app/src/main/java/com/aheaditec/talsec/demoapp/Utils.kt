package com.aheaditec.talsec.demoapp

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import java.security.MessageDigest

object Utils {
    
    // Helper for obtaining your signing certificate hash used to initialize TalsecConfig
    fun computeSigningCertificateHash(context: Context): String {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            provideSignatureFlagsBaseOnSdk()
        )
        return getApkSigningCertificate(packageInfo)[0]
    }

    private fun provideSignatureFlagsBaseOnSdk(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }
    }

    private fun getApkSigningCertificate(packageInfo: PackageInfo): List<String> {
        val signingHashes = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.apply {
                if (hasMultipleSigners()) {
                    apkContentsSigners?.forEach {
                        signingHashes.add(
                            hashCertificate(it)
                        )
                    }
                } else {
                    signingCertificateHistory?.forEach {
                        signingHashes.add(
                            hashCertificate(it)
                        )
                    }
                }
            }
        } else {
            packageInfo.signatures?.forEach {
                signingHashes.add(
                    hashCertificate(it)
                )
            }
        }
        return signingHashes
    }

    private fun hashCertificate(signature: Signature): String {
        val hash = MessageDigest.getInstance("SHA-256").run {
            digest(signature.toByteArray())
        }
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}