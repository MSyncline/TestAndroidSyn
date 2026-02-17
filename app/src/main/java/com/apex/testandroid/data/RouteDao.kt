package com.apex.testandroid.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Insert
    suspend fun insertRoute(route: Route): Long

    @Update
    suspend fun updateRoute(route: Route)

    @Insert
    suspend fun insertPoint(point: RoutePoint)

    @Query("SELECT * FROM routes ORDER BY startTime DESC")
    fun getAllRoutes(): Flow<List<Route>>

    @Query("SELECT * FROM routes WHERE id = :routeId")
    suspend fun getRouteById(routeId: Long): Route?

    @Query("SELECT * FROM route_points WHERE routeId = :routeId ORDER BY timestamp ASC")
    fun getPointsForRoute(routeId: Long): Flow<List<RoutePoint>>

    @Query("SELECT COUNT(*) FROM route_points WHERE routeId = :routeId")
    fun getPointCountForRoute(routeId: Long): Flow<Int>

    @Query("DELETE FROM routes WHERE id = :routeId")
    suspend fun deleteRoute(routeId: Long)
}
