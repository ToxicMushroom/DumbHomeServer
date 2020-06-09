package me.melijn.dhs

import io.jooby.ServerOptions
import me.melijn.dhs.rest.RestServer

fun main() {
    val container = Container()
    container.serviceManager.startServices()

    val restServer = RestServer(container)


    container.taskManager.async {
        restServer.apply {
            serverOptions = ServerOptions()
                .setPort(container.settings.port)
                .setIoThreads(2)
                .setWorkerThreads(4)
        }.start()
    }
}

