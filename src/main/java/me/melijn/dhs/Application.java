package me.melijn.dhs;

import me.melijn.dhs.components.SwitchComponent;
import me.melijn.dhs.storage.CacheManager;
import me.melijn.dhs.storage.Config;
import me.melijn.dhs.storage.Database;
import me.melijn.dhs.utils.Helpers;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Status;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application extends Jooby {

    private final Config config;
    private final Database database;
    private final Helpers helpers;
    private final CacheManager cacheManager;
    private final Logger logger;

    private void startServer() {
        get("/switches/{id}/state", (req, rsp) -> {
            String user = getUserFromHeader(req);
            if (failAuth(rsp, user)) return;

            rsp.type("application/json").send(new JSONObject()
                    .put("state", cacheManager.getSwitchComponentById(req.param("id").intValue()).isOn())
                    .put("status", "success")
            );

            database.log(user, req);
        });


        post("/switches/{id}/state", (req, rsp) -> {

            String user = getUserFromHeader(req);
            if (failAuth(rsp, user)) return;

            if (req.param("state").booleanValue()) {
                SwitchComponent switchComponent = helpers.updateSwitchState(req.param("id").intValue(), req.param("state").booleanValue());


                rsp.type("application/json").send(new JSONObject()
                        .put("state", switchComponent.isOn())
                        .put("status", "success")
                );
            } else {
                rsp.status(Status.BAD_REQUEST)
                        .type("application/json")
                        .send(new JSONObject().put("status", "Bad request"));
            }

            database.log(user, req);
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
            boolean globalPresets = req.param("global").booleanValue();

            rsp.type("application/json").send(new JSONObject()
                    .put("presets", helpers.getPresets(globalPresets ? "global" : req.param("user").value()))
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
        config = new Config();
        database = new Database(
                config.getSubString("mysql", "host"),
                config.getSubString("mysql", "port"),
                config.getSubString("mysql", "user"),
                config.getSubString("mysql", "password"),
                config.getSubString("mysql", "database")
        );
        cacheManager = new CacheManager(database);
        helpers = new Helpers(database, cacheManager);
        startServer();
    }
}
