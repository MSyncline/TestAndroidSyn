package com.apex.testandroid.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apex.testandroid.data.AppDatabase
import com.apex.testandroid.data.Route
import com.apex.testandroid.data.RoutePoint
import com.apex.testandroid.service.TrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    RouteList,
    Tracking,
    RouteDetail
}

class TrackingViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).routeDao()

    private val _currentScreen = MutableStateFlow(Screen.RouteList)
    val currentScreen: StateFlow<Screen> = _currentScreen

    private val _selectedRouteId = MutableStateFlow<Long?>(null)
    val selectedRouteId: StateFlow<Long?> = _selectedRouteId

    val routes: StateFlow<List<Route>> = dao.getAllRoutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isTracking: StateFlow<Boolean> = TrackingService.isTracking

    val currentRouteId: StateFlow<Long?> = TrackingService.currentRouteId

    val livePointCount: StateFlow<Int> = TrackingService.pointCount

    val liveTrackingPoints: StateFlow<List<Pair<Double, Double>>> = TrackingService.livePoints

    val selectedRoutePoints: StateFlow<List<RoutePoint>> = _selectedRouteId
        .flatMapLatest { id ->
            if (id != null) dao.getPointsForRoute(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedRoute = MutableStateFlow<Route?>(null)
    val selectedRoute: StateFlow<Route?> = _selectedRoute

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectRoute(routeId: Long) {
        _selectedRouteId.value = routeId
        viewModelScope.launch {
            _selectedRoute.value = dao.getRouteById(routeId)
        }
        _currentScreen.value = Screen.RouteDetail
    }

    fun deleteRoute(routeId: Long) {
        viewModelScope.launch {
            dao.deleteRoute(routeId)
        }
    }

    fun onTrackingStarted() {
        _currentScreen.value = Screen.Tracking
    }

    fun onTrackingStopped() {
        _currentScreen.value = Screen.RouteList
    }
}
