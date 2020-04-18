package me.melijn.dhs.services.chacon

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import me.melijn.dhs.database.CacheManager
import me.melijn.dhs.objects.Settings
import me.melijn.dhs.services.Service
import me.melijn.dhs.threading.Task
import me.melijn.dhs.utils.RCSwitchUtil
import java.util.*
import java.util.concurrent.TimeUnit

class ChaconService(
    private val cacheManager: CacheManager,
    private val locationSettings: Settings.Location
) : Service("Chacon", 10, unit = TimeUnit.SECONDS) {

    override val service = Task {
        val timeZone = TimeZone.getTimeZone(locationSettings.timeZone)
        val calendar = Calendar.getInstance(timeZone)
        val timeInSeconds = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60
        val switchServiceActions = cacheManager.dbManager.switchServiceWrapper.getEnabledSwitchServices().toMutableList()

        val location = Location(locationSettings.latitude, locationSettings.longitude)

        val ssc = SunriseSunsetCalculator(location, timeZone)
        val sunrise = getSecondsFromHoursMinute(ssc.getOfficialSunriseForDate(calendar))
        val sunset = getSecondsFromHoursMinute(ssc.getOfficialSunsetForDate(calendar))


        val filteredSwitchServiceActionList = switchServiceActions.filter {
            it.days.contains(calendar.get(Calendar.DAY_OF_WEEK))
                && ((it.time.toIntOrNull()?.let { time ->
                time > (timeInSeconds - 30) && time <= (timeInSeconds + 30)
            } ?: false) ||
                (it.time == "sunset" && timeInSeconds == sunset) ||
                (it.time == "sunrise" && timeInSeconds == sunrise))
        }

        for (serviceAction in filteredSwitchServiceActionList) {
            for (code in serviceAction.codes) {
                RCSwitchUtil.sendRCSwitchCode(cacheManager, code)
                logger.info("Sent via rc: $code")
            }
        }
    }

    private fun getSecondsFromHoursMinute(hoursMinutes: String): Int {
        val parts = hoursMinutes.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        return hours * 3600 + minutes * 60
    }
}