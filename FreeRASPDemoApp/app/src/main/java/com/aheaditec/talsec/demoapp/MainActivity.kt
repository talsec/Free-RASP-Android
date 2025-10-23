package com.aheaditec.talsec.demoapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestFineLocationPermission()
    }

    private fun requestFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                FINE_LOCATION_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(COARSE_LOCATION_PERMISSION, FINE_LOCATION_PERMISSION),
                100
            )
        }
    }

    private companion object {
        private const val FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val COARSE_LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
    }
}