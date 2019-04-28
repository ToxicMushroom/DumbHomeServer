package me.melijn.dhs.storage;

import me.melijn.dhs.components.SwitchComponent;
import me.melijn.dhs.components.User;

import java.util.ArrayList;
import java.util.List;

public class CacheManager {

    private Database database;
    public final List<User> users = new ArrayList<>();
    public final List<SwitchComponent> switchComponentList = new ArrayList<>();

    public CacheManager(Database database) {
        this.database = database;
        refreshCaches();
    }

    public void refreshCaches() {
        switchComponentList.clear();
        switchComponentList.addAll(database.getSwitchList());

        database.getUserTokens().forEach((name, token) -> {
            User user = new User(name, token);
            user.initOverrides(database);
            users.add(user);
        });
    }

    public String getUsernameFromToken(String token) {
        return users.stream().filter(user ->
                user.getToken().equals(token)
        ).findAny().map(User::getName).orElse(null);
    }

    public SwitchComponent getSwitchComponentById(int id) {
        return switchComponentList.stream().filter(switchComponent -> switchComponent.getId() == id).findAny().orElse(null);
    }
}
