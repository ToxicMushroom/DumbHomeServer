package me.melijn.dhs.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.regex.Pattern

val linuxUptimePattern: Pattern = Pattern.compile(
    "(?:\\s+)?\\d+:\\d+:\\d+ up(?: (\\d+) days?,)?(?:\\s+(\\d+):(\\d+)|\\s+?(\\d+)\\s+?min).*"
)

//Thx xavin
val linuxRamPattern: Pattern = Pattern.compile("([0-9]+$)")


fun getUnixUptime(): Long {
    val uptimeProc = Runtime.getRuntime().exec("uptime") //Parse time to groups if possible
    val `in` = BufferedReader(InputStreamReader(uptimeProc.inputStream))
    val line = `in`.readLine() ?: return -1
    val matcher = linuxUptimePattern.matcher(line)

    if (!matcher.find()) return -1 //Extract ints out of groups
    val days2 = matcher.group(1)
    val hours2 = matcher.group(2)
    val minutes2 = if (matcher.group(3) == null) {
        matcher.group(4)
    } else {
        matcher.group(3)
    }
    val days = if (days2 != null) Integer.parseInt(days2) else 0
    val hours = if (hours2 != null) Integer.parseInt(hours2) else 0
    val minutes = if (minutes2 != null) Integer.parseInt(minutes2) else 0
    return (minutes * 60000 + hours * 60000 * 60 + days * 60000 * 60 * 24).toLong()
}

fun getUnixRam(): Int {
    val uptimeProc = Runtime.getRuntime().exec("free -m") //Parse time to groups if possible
    val `in` = BufferedReader(InputStreamReader(uptimeProc.inputStream))
    `in`.readLine() ?: return -1
    val lineTwo = `in`.readLine() ?: return -1

    val matcher = linuxRamPattern.matcher(lineTwo)

    if (!matcher.find()) return -1 //Extract ints out of groups
    val group = matcher.group(1)
    return group.toInt()
}


fun getWindowsUptime(): Long {
    val uptimeProc = Runtime.getRuntime().exec("net stats workstation")
    val `in` = BufferedReader(InputStreamReader(uptimeProc.inputStream))
    for (line in `in`.readLines()) {
        if (line.startsWith("Statistieken vanaf")) {
            val format = SimpleDateFormat("'Statistieken vanaf' dd/MM/yyyy hh:mm:ss") //Dutch windows version
            val bootTime = format.parse(line.remove("?"))
            return System.currentTimeMillis() - bootTime.time

        } else if (line.startsWith("Statistics since")) {
            val format = SimpleDateFormat("'Statistics since' MM/dd/yyyy hh:mm:ss") //English windows version
            val bootTime = format.parse(line.remove("?"))
            return System.currentTimeMillis() - bootTime.time

        }
    }
    return -1
}
fun getSystemUptime(): Long {
    return try {
        var uptime: Long = -1
        val os = System.getProperty("os.name").toLowerCase()
        if (os.contains("win")) {
            uptime = getWindowsUptime()
        } else if (os.contains("mac") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            uptime = getUnixUptime()
        }
        uptime
    } catch (e: Exception) {
        -1
    }
}
