package me.melijn.dhs.components;

import me.melijn.dhs.storage.Database;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String name, token;
    private List<Component> componentOverrides = new ArrayList<>();

    public User(String name, String token) {
        this.name = name;
        this.token = token;
    }

    public void initOverrides(Database database) {
        componentOverrides.addAll(database.getSwitchPresets(name));
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public List<Component> getComponentOverrides() {
        return componentOverrides;
    }
}
