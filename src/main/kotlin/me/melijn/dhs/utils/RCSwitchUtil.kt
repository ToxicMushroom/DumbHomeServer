package me.melijn.dhs.utils

import bot.zerotwo.helper.threading.TaskManager
import com.pi4j.io.gpio.RaspiPin
import me.melijn.dhs.database.CacheManager
import me.melijn.dhs.objects.components.SwitchComponent
import me.melijn.dhs.objects.rcswitch.Protocol
import me.melijn.dhs.objects.rcswitch.RCSwitch

object RCSwitchUtil {

    private val rcSwitch = RCSwitch(RaspiPin.GPIO_00, Protocol.PROTOCOL_433)

    fun sendRCSwitchCode(code: Int) {
        var rcCode = Integer.toBinaryString(code)
        rcCode = String(CharArray(24 - rcCode.length)).replace("\u0000", "0") + rcCode
        rcSwitch.send(rcCode)
    }

    fun updateSwitchState(cacheManager: CacheManager, taskManager: TaskManager, id: Int, state: Boolean): SwitchComponent? {
        val switchComponent = cacheManager.getSwitchComponentById(id) ?: return null
        taskManager.async {
            val decimal = if (state) switchComponent.onCode else switchComponent.offCode
            sendRCSwitchCode(decimal) //GPIO is GPIO17 but undercover, pls save me from this suffering
        }

        cacheManager.switchComponentList.remove(switchComponent)
        switchComponent.isOn = state
        cacheManager.switchComponentList.add(switchComponent)
        return switchComponent
    }
}