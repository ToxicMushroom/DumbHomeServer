package me.melijn.dhs.components;

public abstract class Component {


    public String name;
    public ComponentType componentType;
    public Location location;

    public Component(String name, Location location, ComponentType type) {
        this.name = name;
        this.location = location;
        this.componentType = type;
    }

    public String getName() {
        return name;
    }
    public Location getLocation() {
        return location;
    }
    public ComponentType getComponentType() {
        return componentType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
