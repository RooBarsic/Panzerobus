import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.math.sqrt


data class Point(val latitude: Float, val longitude: Float)
data class Participants(val passengers: Collection<Person>, val drivers: Collection<Person>)
data class Person(val id: UUID, val finishPoint: Point)
val startPoint = Point(0F,0F)

fun main() {
    val (passengers, drivers) = readPoints()
    for (passenger in passengers) {
        val suggestedDrivers = suggestDrivers(passenger, drivers)
        println("Passenger point: ${passenger.finishPoint.latitude}, ${passenger.finishPoint.longitude}")
        for (driver in suggestedDrivers) {
            println("  ${driver.finishPoint.latitude}, ${driver.finishPoint.longitude}")
        }
    }
}

fun countDistance(point1: Point, point2: Point): Float{
    return sqrt(((point1.latitude - point2.latitude) * (point1.latitude - point2.latitude)) +
            ((point1.longitude - point2.longitude) * (point1.longitude - point2.longitude)))
}

fun getDriverComfort(passenger: Person, driver: Person): Float {
    return countDistance(startPoint, passenger.finishPoint) +
            countDistance(passenger.finishPoint, driver.finishPoint) -
            countDistance(startPoint, driver.finishPoint)
}

fun suggestDrivers(passenger: Person, drivers: Collection<Person>): Collection<Person> {
    val comparator = Comparator<Person> {driver1: Person, driver2: Person ->
        when {
            getDriverComfort(passenger, driver1) > getDriverComfort(passenger, driver2) -> 1
            getDriverComfort(passenger, driver1) < getDriverComfort(passenger, driver2) -> -1
            else -> 0
        }
    }
    return drivers.sortedWith(comparator)
}

private fun readPoints(): Participants {
    val pathToResource = Paths.get(Point::class.java.getResource("latlons").toURI())
    val allPoints = Files.readAllLines(pathToResource).map { asPoint(it) }.shuffled()
    val passengers = allPoints.slice(0..9).map { Person(UUID.randomUUID(), it) }
    val drivers = allPoints.slice(10..19).map { Person(UUID.randomUUID(), it) }
    return Participants(passengers, drivers)
}

private fun asPoint(it: String): Point {
    val (lat, lon) = it.split(", ")
    return Point(lat.toFloat(), lon.toFloat())
}
