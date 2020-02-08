package me.melijn.dhs.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import me.melijn.dhs.database.CacheManager
import me.melijn.dhs.objects.components.SwitchComponent
import me.melijn.dhs.rest.OBJECT_MAPPER

object ComponentUtil {

    fun getPresets(cacheManager: CacheManager, username: String): ObjectNode? {
        val jsonObject = OBJECT_MAPPER.createObjectNode()
        val switchArray = OBJECT_MAPPER.createArrayNode()
        val switchComponents: MutableList<SwitchComponent> = cacheManager.switchComponentList
        if (username != "global") {
            val user = cacheManager.getUserByName(username) ?: return null
            switchComponents.addAll(user.getSwitchComponentOverrides())
        }
        for (switchComponent in switchComponents) {
            switchArray.add(OBJECT_MAPPER.createObjectNode()
                .put("id", switchComponent.id)
                .put("name", switchComponent.name)
                .put("location", switchComponent.location.toString()))
        }
        jsonObject.set<JsonNode>("switches", switchArray)
        return jsonObject
    }
}

