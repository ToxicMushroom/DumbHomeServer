package me.melijn.dhs.utils;

import me.melijn.dhs.components.SwitchComponent;
import me.melijn.dhs.storage.CacheManager;
import me.melijn.dhs.storage.Database;
import org.jooby.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class Helpers {


    private final Database database;
    private final CacheManager cacheManager;
    private final TaskManager taskManager;


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

        cacheManager.switchComponentList.remove(switchComponent);
        switchComponent.setOn(state);
        cacheManager.switchComponentList.add(switchComponent);

        taskManager.async(() -> {
            String code = Integer.toBinaryString(state ? switchComponent.getOnCode() : switchComponent.getOffCode());
            //new RCSwitch(RaspiPin.GPIO_07, Protocol.PROTOCOL_433).send(code);
            database.updateSwitchState(id, state);
        });

        return switchComponent;
    }


    public void log(String user, Request req) {
        taskManager.async(() -> database.log(user, req));
    }
}
