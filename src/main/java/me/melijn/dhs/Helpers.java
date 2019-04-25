package me.melijn.dhs;

import me.melijn.dhs.components.SwitchComponent;
import org.json.JSONArray;
import org.json.JSONObject;

public class Helpers {


    private final Database database;

    public Helpers(Database database) {
        this.database = database;
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
}
