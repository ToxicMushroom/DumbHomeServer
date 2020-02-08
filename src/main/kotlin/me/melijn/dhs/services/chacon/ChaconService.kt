package me.melijn.dhs.services.chacon

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import kotlinx.coroutines.runBlocking
import me.melijn.dhs.database.DBManager
import me.melijn.dhs.objects.Settings
import me.melijn.dhs.services.Service
import me.melijn.dhs.utils.RCSwitchUtil
import java.util.*
import java.util.concurrent.TimeUnit

class ChaconService(
    private val dbManager: DBManager,
    private val locationSettings: Settings.Location
) : Service("Chacon", 5, unit = TimeUnit.SECONDS) {

    override val service = Runnable {
        runBlocking {
            val timeZone = TimeZone.getTimeZone(locationSettings.timeZone)
            val calendar = Calendar.getInstance(timeZone)
            val timeInSeconds = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60
            val switchServiceActions = dbManager.switchServiceWrapper.getEnabledSwitchServices().toMutableList()

            val location = Location(locationSettings.latitude, locationSettings.longitude)

            val ssc = SunriseSunsetCalculator(location, timeZone)
            val sunrise = getSecondsFromHoursMinute(ssc.getOfficialSunriseForDate(calendar))
            val sunset = getSecondsFromHoursMinute(ssc.getOfficialSunsetForDate(calendar))


            val filteredSwitchServiceActionList = switchServiceActions.filter {
                it.days.contains(calendar.get(Calendar.DAY_OF_WEEK))
                    && (it.time == "$timeInSeconds" ||
                    (it.time == "sunset" && timeInSeconds == sunset) ||
                    (it.time == "sunrise" && timeInSeconds == sunrise))
            }

            for (serviceAction in filteredSwitchServiceActionList) {
                for (code in serviceAction.codes) {
                    RCSwitchUtil.sendRCSwitchCode(code)
                    logger.info("Sent via rc: $code")
                }
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