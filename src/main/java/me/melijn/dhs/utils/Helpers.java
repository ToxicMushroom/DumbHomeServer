package me.melijn.dhs.utils;

import me.melijn.dhs.components.SwitchComponent;
import me.melijn.dhs.storage.CacheManager;
import me.melijn.dhs.storage.Database;
import org.json.JSONArray;
import org.json.JSONObject;

public class Helpers {


    private final Database database;
    private final CacheManager cacheManager;


    public Helpers(Database database, CacheManager cacheManager) {
        this.database = database;
        this.cacheManager = cacheManager;
    }


    public JSONObject getPresets(String user) {
        JSONObject jsonObject = new JSONObject();
        JSONArray switchArray = new JSONArray();
        for (SwitchComponent switchComponent : database.getSwitchPresets(user)) {
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

        database.updateSwitchState(id, state);
        //TODO add switch sending

        return switchComponent;
    }
}