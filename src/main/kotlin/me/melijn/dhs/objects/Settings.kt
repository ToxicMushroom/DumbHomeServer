package me.melijn.dhs.objects

data class Settings(
    val port: Int,
    val logDays: Int,
    val location: Location,
    val database: Database
) {

    data class Location(
        var timeZone: String,
        var latitude: Double,
        var longitude: Double
    )

    data class Database(
        var database: String,
        var password: String,
        var user: String,
        var host: String,
        var port: Int
    )
}