package me.melijn.dhs.objects.components

import com.fasterxml.jackson.databind.node.ObjectNode

abstract class Component(var name: String, var location: Location, var componentType: ComponentType) {

    abstract fun toObjectNode(): ObjectNode?

}