package me.melijn.dhs.objects

data class Settings @JvmOverloads constructor(
    val port: Int,
    val location: Location,
    val database: Database
) {

    data class Location @JvmOverloads constructor(
        var timeZone: String,
        var latitude: Double,
        var longitude: Double
    )

    data class Database @JvmOverloads constructor(
        var database: String,
        var password: String,
        var user: String,
        var host: String,
        var port: Int
    )
}