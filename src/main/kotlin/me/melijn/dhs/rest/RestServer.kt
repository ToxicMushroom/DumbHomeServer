package me.melijn.dhs.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sun.management.OperatingSystemMXBean
import kotlinx.coroutines.runBlocking
import me.melijn.dhs.Container
import me.melijn.dhs.utils.*
import org.jooby.Jooby
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
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
        get("/stats") { _: Request, rsp: Response ->
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

            rsp.type(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("jvmUptime", System.currentTimeMillis() - container.startTime)
                .put("uptime", getSystemUptime())
                .put("jvmThreads", Thread.activeCount())
                .put("dumbhomeThreads", dumbHomeThreads)
                .put("jvmramUsage", usedJVMMem)
                .put("jvmramTotal", totalJVMMem)
                .put("ramUsage", usedMem)
                .put("ramTotal", totalMem)
                .put("cpuUsage", bean.processCpuLoad * 100)
            )
        }

        get("/switches/{id}/state") { req: Request, rsp: Response ->
            val user = getAndVerifyUserFromHeader(req, rsp) ?: return@get
            val switchComponent = cacheManager.getSwitchComponentById(req.param("id").intValue())
            if (switchComponent == null) {
                send400(rsp)
                return@get
            }

            rsp.type(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("state", switchComponent.isOn)
                .put("status", "success")
            )

            log(user, req)
        }


        get("/switches/states") { req: Request, rsp: Response ->
            val user = getAndVerifyUserFromHeader(req, rsp) ?: return@get

            val switchArray: ArrayNode = OBJECT_MAPPER.createArrayNode()
            val switchComponents = cacheManager.switchComponentList
            for (switchComponent in switchComponents) {
                switchArray.add(switchComponent.toObjectNode())
            }
            rsp.type(MediaType.json).send(
                OBJECT_MAPPER.createObjectNode()
                    .put("status", "success")
                    .set<JsonNode>("switches", switchArray)
            )

            log(user, req)
        }


        post("/switches/{id}/state") { req: Request, rsp: Response ->
            val user = getAndVerifyUserFromHeader(req, rsp) ?: return@post

            val switchComponent = RCSwitchUtil.updateSwitchState(
                cacheManager, req.param("id").intValue(), req.param("state").booleanValue()
            )

            if (switchComponent == null) {
                send400(rsp)
                return@post
            }

            rsp.type(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("state", switchComponent.isOn)
                .put("status", "success")
            )

            log(user, req)
        }


        get("/views/{id}") { req: Request, rsp: Response ->
            rsp.type(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("data", req.param("id").intValue())
                .put("status", "success")
            )
        }


        post("/irsender/{id}") { req: Request, rsp: Response ->
            rsp.type(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("status", "success")
            )
            logger.info(req.param("codeId").value())
            logger.info(req.param("code").value())
        }


        get("/presets/list") { req: Request, rsp: Response ->
            if (!req.param("global").isSet) return@get

            val globalPresets = req.param("global").booleanValue()
            val user = if (!globalPresets) {
                getAndVerifyUserFromHeader(req, rsp) ?: return@get
            } else "global"

            rsp.type(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("status", "success")
                .set<JsonNode>("presets", ComponentUtil.getPresets(cacheManager, if (globalPresets) "global" else user))
            )
        }


        post("/refreshcache") { req, rsp ->
            val user = getAndVerifyUserFromHeader(req, rsp) ?: return@post

            runBlocking {
                cacheManager.refreshCaches()
            }

            rsp.type(MediaType.json).send(OBJECT_MAPPER.createObjectNode()
                .put("status", "success")
            )

            log(user, req)
        }


        use("*") { _: Request?, rsp: Response ->
            rsp.send("blub")
        }
    }


    private fun send400(rsp: Response) {
        rsp.status(400).send("Bad Request")
    }


    private fun getAndVerifyUserFromHeader(req: Request, rsp: Response): String? {
        val user = getUserFromHeader(req)
        return if (failAuth(rsp, user)) null
        else user
    }


    private fun failAuth(rsp: Response, user: String?): Boolean {
        if (user == null) {
            try {
                rsp.type("application/json").send(
                    OBJECT_MAPPER.createObjectNode()
                        .put("status", "failed authentication")
                )
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
            return true
        }
        return false
    }


    private fun getUserFromHeader(req: Request): String? {
        var user: String? = null
        if (req.headers().containsKey("token")) {
            user = cacheManager.getUsernameFromToken(req.header("token").value())
        }
        logger.info("{}/{} - {} - {}",
            req.ip(),
            req.method(),
            user ?: "unauthorized",
            req.path())
        return user
    }


    private fun log(user: String, req: Request) {
        taskManager.async {
            container.dbManager.logWrapper.log(user, req.ip() + "/" + req.method() + " - " + req.path())
        }
    }
}