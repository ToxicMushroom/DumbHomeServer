package me.melijn.dhs.components;

public class SwitchComponent extends Component {

    private int id;
    private boolean on;
    private int onCode;
    private int offCode;

    public SwitchComponent(String name, Location location, int id, boolean state, int onCode, int offCode) {
        super(name, location, ComponentType.SWITCH);
        this.id = id;
        this.on = state;
        this.onCode = onCode;
        this.offCode = offCode;
    }

    public int getOffCode() {
        return offCode;
    }

    public int getOnCode() {
        return onCode;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public int getId() {
        return id;
    }
}
