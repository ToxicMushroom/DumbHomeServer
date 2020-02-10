package me.melijn.dhs.utils

import com.pi4j.io.gpio.RaspiPin
import me.melijn.dhs.database.CacheManager
import me.melijn.dhs.objects.components.SwitchComponent
import me.melijn.dhs.objects.rcswitch.Protocol
import me.melijn.dhs.objects.rcswitch.RCSwitch
import me.melijn.dhs.threading.TaskManager

object RCSwitchUtil {

    private val rcSwitch = RCSwitch(RaspiPin.GPIO_00, Protocol.PROTOCOL_433)

    fun sendRCSwitchCode(cacheManager: CacheManager, code: Int) {
        val switchComponent: SwitchComponent? = cacheManager.getSwitchComponentByCode(code)
        var rcCode = Integer.toBinaryString(code)
        rcCode = String(CharArray(24 - rcCode.length)).replace("\u0000", "0") + rcCode
        rcSwitch.send(rcCode)

        if (switchComponent != null) {
            cacheManager.switchComponentList.remove(switchComponent)
            switchComponent.isOn = code == switchComponent.onCode
            cacheManager.switchComponentList.add(switchComponent)
        }
    }

    fun updateSwitchState(cacheManager: CacheManager, taskManager: TaskManager, id: Int, state: Boolean): SwitchComponent? {
        val switchComponent = cacheManager.getSwitchComponentById(id) ?: return null
        taskManager.async {
            val decimal = if (state) switchComponent.onCode else switchComponent.offCode
            sendRCSwitchCode(cacheManager, decimal) //GPIO is GPIO17 but undercover, pls save me from this suffering
        }

        return switchComponent
    }
}