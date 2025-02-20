package org.hawk0120

import org.hawk0120.*

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random
import java.time.LocalTime

object TrainGenerator {
    private val stationNames = listOf("Amsterdam", "Rotterdam", "Utrecht", "Eindhoven",
                                    "Groningen", "Maastricht", "Den Haag", "Leiden",
                                    "Delft", "Arnhem", "Nijmegen", "Enschede", "Zwolle",
                                    "Breda", "Tilburg", "Almere", "Haarlem", "Leeuwarden",
                                    "Middelburg", "Lelystad")

    fun randomStation(): Station = Station(stationNames.random())

    fun randomRoute(): Route {
        val start = randomStation()
        var end = randomStation()
        while (end == start) {
            end = randomStation()
        }
        val distance = Random.nextInt(50, 10000) 
        return Route(start, end, distance)
    }

    fun randomTrain(): Train {
        val id = "T-${Random.nextInt(1000, 9999)}" // train id
        val type = if (Random.nextBoolean()) TrainType.PASSENGER else TrainType.FREIGHT // Random train type
        val speed = Random.nextInt(80, 200) // Speed in km/h
        val capacity = if (type == TrainType.PASSENGER) Random.nextInt(100, 500) else Random.nextInt(500, 2000) // Capacity in passengers or tons
        return Train(id, type, speed, capacity)
    }

    fun randomSchedule(train: Train, route: Route): Schedule {
        val departure = LocalTime.of(Random.nextInt(0, 23), Random.nextInt(0, 59)) // Random departure time
        val travelTimeH = route.distanceKm / train.speedKmH.toDouble() // Travel time in hours
        val arrival = departure.plusMinutes((travelTimeH * 60).toLong()) // Calculate arrival time
        return Schedule(train, route, departure, arrival)
    }
}


suspend fun runSimulation() = coroutineScope {
    val routes = List(200) { TrainGenerator.randomRoute() } // Generate random shared routes
    val schedules = List(250) {
        val train = TrainGenerator.randomTrain()
        val route = routes.random() // Assign a train to a random existing route
        TrainGenerator.randomSchedule(train, route)
    }

    val events = mutableListOf<TrainEvent>()


    schedules.sortedBy { it.departure }.forEach { schedule ->
        events.add(TrainEvent(schedule.departure, schedule, "departure"))
    }

    schedules.sortedBy { it.arrival }.forEach { schedule ->
        events.add(TrainEvent(schedule.arrival, schedule, "arrival"))
    }

    val sortedEvents = events.sortedBy { it.time }

    sortedEvents.forEach { event ->
        launch {
            val schedule = event.schedule
            val route = schedule.route

            if (event.type == "departure") {
                println("${schedule.train.id} waiting to use track from ${route.start.name} to ${route.end.name}")
                route.track.withLock {
                    println("${schedule.train.id} departing from ${route.start.name} at ${schedule.departure}")
                    delay((route.distanceKm / schedule.train.speedKmH.toDouble() * 1000).toLong())
                }
            } else {
                route.track.withLock {
                    println("${schedule.train.id} arriving at ${route.end.name} at ${schedule.arrival}")
                }
            }
        }
    }
}

fun main() = runBlocking {
    println("Starting Train Simulation with Track Availability...")
    runSimulation()
}

