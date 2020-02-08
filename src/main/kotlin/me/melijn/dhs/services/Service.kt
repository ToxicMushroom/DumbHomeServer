package me.melijn.dhs.services

import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture

abstract class Service(val name: String) {

    private val threadFactory = ThreadFactoryBuilder().setNameFormat("[$name-Service] ").build()
    val scheduledExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(threadFactory)
    val logger = LoggerFactory.getLogger(name)
    lateinit var future: ScheduledFuture<*>

    abstract fun start()
    abstract fun stop()
}