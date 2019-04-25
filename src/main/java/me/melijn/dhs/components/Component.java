package me.melijn.dhs.components;

public abstract class Component {


    String name;
    ComponentType componentType;

    public Component(String name, ComponentType type) {
        this.name = name;
        this.componentType = type;
    }


    abstract public String getName();

    abstract public void setName(String name);

    abstract public ComponentType getComponentType();

    abstract public void setComponentType(ComponentType componentType);
}
