package me.melijn.dhs

import me.melijn.dhs.rest.RestServer
import org.jooby.Jooby

fun main() {
    val container = Container()
    container.serviceManager.startServices()

    val restServer = RestServer(container)
    Jooby.run({ restServer }, arrayOf("application.port=${container.settings.port}"))
}

