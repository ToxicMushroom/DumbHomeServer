package me.melijn.dhs;

import org.jooby.Jooby;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application extends Jooby {

    private final Config config;
    private final Database database;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    {
        get("/switches/{id}/state", (req, rsp) -> {
            rsp.type("application/json").send(new JSONObject()
                    .put("state", req.param("id").intValue())
                    .put("status", "success")
            );
        });


        post("/switches/{id}/state", (req, rsp) -> {

            rsp.type("application/json").send(new JSONObject()
                    .put("status", "success"));
        });

        use("*", (req, rsp) -> {
            logger.info(req.ip() + "/" + req.method() + " - " + req.path());
            rsp.send("blub");
        });
    }

    public Application() {
        config = new Config();
        database = new Database(
                config.getSubString("mysql", "host"),
                config.getSubString("mysql", "port"),
                config.getSubString("mysql", "user"),
                config.getSubString("mysql", "password"),
                config.getSubString("mysql", "database")
        );
    }
}
