package com.apex.testandroid

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    trackPoints: List<Pair<Double, Double>>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)

    LaunchedEffect(trackPoints) {
        // Remove previous polyline and marker overlays, keeping tile overlays
        mapView.overlays.removeAll { it is Polyline || it is Marker }

        if (trackPoints.isNotEmpty()) {
            val geoPoints = trackPoints.map { (lat, lng) -> GeoPoint(lat, lng) }

            // Draw polyline for the track
            val polyline = Polyline().apply {
                setPoints(geoPoints)
                outlinePaint.color = Color.parseColor("#FF4081")
                outlinePaint.strokeWidth = 8f
            }
            mapView.overlays.add(polyline)

            // Add a marker at the current (latest) position
            val currentPos = geoPoints.last()
            val currentMarker = Marker(mapView).apply {
                position = currentPos
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Current Position"
            }
            mapView.overlays.add(currentMarker)

            // Center map on the latest point
            mapView.controller.animateTo(currentPos)
        }

        mapView.invalidate()
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun rememberMapViewWithLifecycle(context: Context): MapView {
    val mapView = remember {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(17.0)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }

    return mapView
}
