package me.melijn.dhs.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sun.management.OperatingSystemMXBean
import io.jooby.Context
import io.jooby.Jooby
import io.jooby.MediaType
import io.jooby.json.JacksonModule
import kotlinx.coroutines.runBlocking
import me.melijn.dhs.Container
import me.melijn.dhs.utils.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.util.concurrent.ThreadPoolExecutor

val OBJECT_MAPPER = jacksonObjectMapper()

class RestServer(private val container: Container) : Jooby() {

    private val logger: Logger = LoggerFactory.getLogger(RestServer::class.java.name)
    private val cacheManager = container.cacheManager
    private val taskManager = container.taskManager


    init {
        startServer()
    }

    private fun startServer() {
        install(JacksonModule())
        get("/stats") { ctx ->
            val bean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
            val totalMem = bean.totalPhysicalMemorySize shr 20

            val usedMem = if (OSValidator.isUnix) {
                totalMem - getUnixRam()
            } else {
                totalMem - (bean.freeSwapSpaceSize shr 20)
            }
            val totalJVMMem = ManagementFactory.getMemoryMXBean().heapMemoryUsage.max shr 20
            val usedJVMMem = ManagementFactory.getMemoryMXBean().heapMemoryUsage.used shr 20

            var dumbHomeThreads = 0
            dumbHomeThreads += (container.taskManager.executorService as ThreadPoolExecutor).activeCount
            dumbHomeThreads += container.serviceManager.services.size

            ctx.setResponseType(MediaType.json)
                .send(OBJECT_MAPPER.createObjectNode()
                    .put("jvmUptime", System.currentTimeMillis() - container.startTime)
                    .put("uptime", getSystemUptime())
                    .put("jvmThreads", Thread.activeCount())
                    .put("dumbhomeThreads", dumbHomeThreads)
                    .put("jvmramUsage", usedJVMMem)
                    .put("jvmramTotal", totalJVMMem)
                    .put("ramUsage", usedMem)
                    .put("ramTotal", totalMem)
                    .put("cpuUsage", bean.processCpuLoad * 100).toString()
                )
        }

        get("/switches/{id}/state") { ctx ->
            val user = getAndVerifyUserFromHeader(ctx) ?: return@get 0
            val switchComponent = cacheManager.getSwitchComponentById(ctx.path("id").intValue())
            if (switchComponent == null) {
                send400(ctx)
                return@get 0
            }

            ctx.setResponseType(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("state", switchComponent.isOn)
                .put("status", "success").toString()
            )

            log(user, ctx)
        }


        get("/switches/states") { ctx ->
            val user = getAndVerifyUserFromHeader(ctx) ?: return@get 0

            val switchArray: ArrayNode = OBJECT_MAPPER.createArrayNode()
            val switchComponents = cacheManager.switchComponentList
            for (switchComponent in switchComponents) {
                switchArray.add(switchComponent.toObjectNode())
            }
            ctx.setResponseType(MediaType.json).send(
                OBJECT_MAPPER.createObjectNode()
                    .put("status", "success")
                    .set<JsonNode>("switches", switchArray)
                    .toString()
            )

            log(user, ctx)
        }


        post("/switches/{id}/state") { ctx ->
            val user = getAndVerifyUserFromHeader(ctx) ?: return@post 0

            val switchComponent = RCSwitchUtil.updateSwitchState(
                cacheManager, ctx.path("id").intValue(), ctx.form("state").booleanValue()
            )

            if (switchComponent == null) {
                send400(ctx)
                return@post 0
            }

            ctx.setResponseType(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("state", switchComponent.isOn)
                .put("status", "success")
                .toString()
            )

            log(user, ctx)
        }


        get("/views/{id}") { ctx ->
            ctx.setResponseType(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("data", ctx.path("id").intValue())
                .put("status", "success")
                .toString()
            )
        }


        post("/irsender/{id}") { ctx ->
            ctx.setResponseType(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("status", "success")
                .toString()
            )
            logger.info(ctx.form("codeId").value())
            logger.info(ctx.form("code").value())
        }


        get("/presets/list") { ctx ->
            if (ctx.query("global").isMissing) return@get 0

            val globalPresets = ctx.query("global").booleanValue()
            val user = if (!globalPresets) {
                getAndVerifyUserFromHeader(ctx) ?: return@get 0
            } else "global"

            ctx.setResponseType(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("status", "success")
                .set<JsonNode>("presets", ComponentUtil.getPresets(cacheManager, if (globalPresets) "global" else user))
                .toString()
            )
        }


        post("/refreshcache") { ctx ->
            val user = getAndVerifyUserFromHeader(ctx) ?: return@post "a"

            runBlocking {
                cacheManager.refreshCaches()
            }

            ctx.setResponseType(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("status", "success")
                .toString()
            )

            log(user, ctx)
        }


        get("*") { ctx ->
            ctx.send("blub")
        }
    }


    private fun send400(rsp: Context) {
        rsp.setResponseCode(400).send("Bad Request")
    }


    private fun getAndVerifyUserFromHeader(ctx: Context): String? {
        val user = getUserFromHeader(ctx)
        return if (failAuth(ctx, user)) null
        else user
    }


    private fun failAuth(ctx: Context, user: String?): Boolean {
        if (user == null) {
            try {
                ctx.setResponseType("application/json").send(
                    OBJECT_MAPPER.createObjectNode()
                        .put("status", "failed authentication")
                        .toString()
                )
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
            return true
        }
        return false
    }


    private fun getUserFromHeader(ctx: Context): String? {
        var user: String? = null
        if (ctx.headerMap().containsKey("token")) {
            user = cacheManager.getUsernameFromToken(ctx.header("token").value())
        }
        logger.info("{}/{} - {} - {}",
            ctx.remoteAddress,
            ctx.method,
            user ?: "unauthorized",
            ctx.path())
        return user
    }


    private fun log(user: String, ctx: Context) {
        taskManager.async {
            container.dbManager.logWrapper.log(user, ctx.remoteAddress + "/" + ctx.method + " - " + ctx.path())
        }
    }
}