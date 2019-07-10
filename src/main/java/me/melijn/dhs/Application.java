package me.melijn.dhs;

import me.melijn.dhs.components.SwitchComponent;
import me.melijn.dhs.storage.CacheManager;
import me.melijn.dhs.storage.Config;
import me.melijn.dhs.storage.Database;
import me.melijn.dhs.utils.Helpers;
import me.melijn.dhs.utils.TaskManager;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application extends Jooby {

    private final Config config;
    private final Database database;
    private final Helpers helpers;
    private final CacheManager cacheManager;
    private final Logger logger;
    private final TaskManager taskManager;

    private void startServer() {
        get("/switches/{id}/state", (req, rsp) -> {
            String user = getUserFromHeader(req);
            if (failAuth(rsp, user)) return;

            rsp.type("application/json").send(new JSONObject()
                    .put("state", cacheManager.getSwitchComponentById(req.param("id").intValue()).isOn())
                    .put("status", "success")
            );

            helpers.log(user, req);
        });


        post("/switches/{id}/state", (req, rsp) -> {
            String user = getUserFromHeader(req);
            if (failAuth(rsp, user)) return;

            SwitchComponent switchComponent = helpers.updateSwitchState(req.param("id").intValue(), req.param("state").booleanValue());

            rsp.type("application/json").send(new JSONObject()
                    .put("state", switchComponent.isOn())
                    .put("status", "success")
            );

            helpers.log(user, req);
        });

        get("/views/{id}", (req, rsp) -> {
            rsp.type("application/json").send(new JSONObject()
                    .put("data", req.param("id").intValue())
                    .put("status", "success")
            );
        });

        post("/irsender/{id}", (req, rsp) -> {
            rsp.type("application/json").send(new JSONObject()
                    .put("status", "success")
            );
            logger.info(req.param("codeId").value());
            logger.info(req.param("code").value());
        });

        get("/presets/list", (req, rsp) -> {
            if (!req.param("global").isSet()) return;
            String user = "";
            boolean globalPresets = req.param("global").booleanValue();
            if (!globalPresets) {
                user = getUserFromHeader(req);
                if (failAuth(rsp, user)) return;
            }

            rsp.type("application/json").send(new JSONObject()
                    .put("presets", helpers.getPresets(globalPresets ? "global" : user))
                    .put("status", "success")
            );
        });

        use("*", (req, rsp) -> {
            rsp.send("blub");
        });
    }

    private boolean failAuth(Response rsp, String user) {
        if (user == null) {
            try {
                rsp.type("application/json").send(new JSONObject()
                        .put("status", "failed authentication")
                );
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private String getUserFromHeader(Request req) {
        String user = null;
        if (req.headers().containsKey("token")) {
            user = cacheManager.getUsernameFromToken(req.header("token").value());
        }

        logger.info(req.ip() + "/" + req.method() + " - " + (user == null ? "unauthorized" : user) + " - " + req.path());

        return user;
    }

    public Application() {
        logger = LoggerFactory.getLogger(this.getClass().getName());
        taskManager = new TaskManager();
        config = new Config();
        database = new Database(
                config.getSubString("mysql", "host"),
                config.getSubString("mysql", "port"),
                config.getSubString("mysql", "user"),
                config.getSubString("mysql", "password"),
                config.getSubString("mysql", "database")
        );
        cacheManager = new CacheManager(database);
        helpers = new Helpers(database, cacheManager, taskManager);
        startServer();
    }
}
