package org.hawk0120

import kotlinx.coroutines.sync.Mutex
import java.time.LocalTime


// Data Models
data class Station(val name: String)
data class Route(val start: Station, val end: Station, val distanceKm: Int, val track: Mutex = Mutex())
data class Train(val id: String, val type: TrainType, val speedKmH: Int, val capacity: Int)
data class Schedule(val train: Train, val route: Route, val departure: LocalTime, val arrival: LocalTime)
data class TrainEvent(val time: LocalTime, val schedule: Schedule, val type: String)


enum class TrainType { PASSENGER, FREIGHT }
