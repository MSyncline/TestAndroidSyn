package com.apex.testandroid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apex.testandroid.service.TrackingService
import com.apex.testandroid.ui.RouteDetailScreen
import com.apex.testandroid.ui.RouteListScreen
import com.apex.testandroid.ui.TrackingScreen
import com.apex.testandroid.ui.theme.TestAndroidTheme
import com.apex.testandroid.viewmodel.Screen
import com.apex.testandroid.viewmodel.TrackingViewModel

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            Log.d(TAG, "Location permission granted")
            startTrackingService()
        } else {
            Log.w(TAG, "Location permission denied")
        }
    }

    private var pendingViewModel: TrackingViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Test log: MainActivity onCreate called")
        enableEdgeToEdge()
        setContent {
            TestAndroidTheme {
                val vm: TrackingViewModel = viewModel()
                val currentScreen by vm.currentScreen.collectAsState()
                val routes by vm.routes.collectAsState()
                val pointCount by vm.livePointCount.collectAsState()
                val selectedRoute by vm.selectedRoute.collectAsState()
                val selectedPoints by vm.selectedRoutePoints.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        Screen.RouteList -> RouteListScreen(
                            routes = routes,
                            onStartTracking = {
                                pendingViewModel = vm
                                requestLocationAndStart(vm)
                            },
                            onRouteClick = { routeId -> vm.selectRoute(routeId) },
                            onDeleteRoute = { routeId -> vm.deleteRoute(routeId) },
                            modifier = Modifier.padding(innerPadding)
                        )

                        Screen.Tracking -> TrackingScreen(
                            pointCount = pointCount,
                            onStopTracking = {
                                stopTrackingService()
                                vm.onTrackingStopped()
                            },
                            modifier = Modifier.padding(innerPadding)
                        )

                        Screen.RouteDetail -> RouteDetailScreen(
                            route = selectedRoute,
                            points = selectedPoints,
                            onBack = { vm.navigateTo(Screen.RouteList) },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    private fun requestLocationAndStart(vm: TrackingViewModel) {
        val fineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (fineLocation == PackageManager.PERMISSION_GRANTED) {
            startTrackingService()
            vm.onTrackingStarted()
        } else {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            locationPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun startTrackingService() {
        val intent = Intent(this, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START
        }
        ContextCompat.startForegroundService(this, intent)
        pendingViewModel?.onTrackingStarted()
        pendingViewModel = null
    }

    private fun stopTrackingService() {
        val intent = Intent(this, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        }
        startService(intent)
    }
}
