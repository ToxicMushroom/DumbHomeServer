package me.melijn.dhs.utils;

import com.pi4j.io.gpio.RaspiPin;
import me.melijn.dhs.components.SwitchComponent;
import me.melijn.dhs.rcswitch.Protocol;
import me.melijn.dhs.rcswitch.RCSwitch;
import me.melijn.dhs.storage.CacheManager;
import me.melijn.dhs.storage.Database;
import org.jooby.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Helpers {

    private final RCSwitch rcSwitch = new RCSwitch(RaspiPin.GPIO_00, Protocol.PROTOCOL_433);
    private final Database database;
    private final CacheManager cacheManager;
    private final TaskManager taskManager;
    private final Logger logger = LoggerFactory.getLogger(Helpers.class.getName());


    public Helpers(Database database, CacheManager cacheManager, TaskManager taskManager) {
        this.database = database;
        this.cacheManager = cacheManager;
        this.taskManager = taskManager;
    }


    public JSONObject getPresets(String username) {
        JSONObject jsonObject = new JSONObject();
        JSONArray switchArray = new JSONArray();

        List<SwitchComponent> switchComponents = cacheManager.switchComponentList;
        if (!username.equals("global")) {
            switchComponents = cacheManager.getUserByName(username).getSwitchComponentOverrides();
        }

        for (SwitchComponent switchComponent : switchComponents) {
            switchArray.put(new JSONObject()
                    .put("id", switchComponent.getId())
                    .put("name", switchComponent.getName())
                    .put("location", switchComponent.getLocation()));

        }

        jsonObject.put("switches", switchArray);

        return jsonObject;
    }

    public SwitchComponent updateSwitchState(int id, boolean state) {
        SwitchComponent switchComponent = cacheManager.getSwitchComponentById(id);
        if (switchComponent == null) return null;

        taskManager.async(() -> {
            int decimal = state ? switchComponent.getOnCode() : switchComponent.getOffCode();
            String code = Integer.toBinaryString(decimal);
            code = new String(new char[(24 - code.length())]).replace("\0", "0") + code;
            logger.info("rf code sent: " + decimal + "/" + code);
            rcSwitch.send(code); //GPIO is GPIO17 but undercover, pls save me from this suffering

            database.updateSwitchState(id, state);
        });

        cacheManager.switchComponentList.remove(switchComponent);
        switchComponent.setOn(state);
        cacheManager.switchComponentList.add(switchComponent);

        return switchComponent;
    }


    public void log(String user, Request req) {
        taskManager.async(() -> database.log(user, req));
    }
}
