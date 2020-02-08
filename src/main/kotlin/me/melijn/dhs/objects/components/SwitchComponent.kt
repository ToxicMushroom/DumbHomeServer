package me.melijn.dhs.objects.components

import com.fasterxml.jackson.databind.node.ObjectNode
import me.melijn.dhs.rest.OBJECT_MAPPER

class SwitchComponent(name: String, location: Location, val id: Int, var isOn: Boolean, val onCode: Int, val offCode: Int) : Component(name, location, ComponentType.SWITCH) {

    override fun toObjectNode(): ObjectNode? {
        return OBJECT_MAPPER.createObjectNode()
            .put("id", id)
            .put("state", isOn)
    }
}