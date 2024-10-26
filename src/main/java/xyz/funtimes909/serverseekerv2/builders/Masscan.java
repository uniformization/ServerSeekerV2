package xyz.funtimes909.serverseekerv2.builders;

@SuppressWarnings("unused")
public class Masscan {
    private final String ip;
    private final short port;

    public Masscan(String ip, short port) {
        this.ip = ip;
        this.port = port;
    }

    // Getters
    public String getIp() {
        return ip;
    }

    public short getPort() {
        return port;
    }
}
