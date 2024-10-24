package xyz.funtimes909.serverseekerv2.builders;

import java.util.List;

@SuppressWarnings("unused")
public class Masscan {
    private String ip;
    private int timestamp;
    private List<Port> ports;

    // Getters
    public String getIp() {
        return ip;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public List<Port> getPorts() {
        return ports;
    }
}
