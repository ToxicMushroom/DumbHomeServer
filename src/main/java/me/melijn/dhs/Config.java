package me.melijn.dhs;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {

    private final File configFile = new File("config.json");
    private final JSONObject configObject;

    Config() {
        if (!configFile.exists()) {
            try {
                Files.write(Paths.get(configFile.getPath()), new JSONObject()
                        .put("mysql", new JSONObject()
                                .put("host", "")
                                .put("port", 3306)
                                .put("database", "dumb_home")
                                .put("user", "Dumb")
                                .put("password", "")
                        ).toString(4).getBytes());

                throw new RuntimeException("Go fill in the new config.json file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        configObject = read(configFile);
    }

    private JSONObject read(File file) {
        try {
            return new JSONObject(new String(Files.readAllBytes(Paths.get(file.getPath()))));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String getValue(String key) {
        return configObject == null ? null : (configObject.has(key) ? configObject.get(key).toString() : null);
    }

    public String getSubString(String section, String key) {
        if (configObject == null) return null;
        if (!configObject.has(section)) return null;
        if (!configObject.getJSONObject(section).has(key)) return null;
        return configObject.getJSONObject(section).get(key).toString();
    }

}
