package xyz.funtimes909.serverseekerv2.builders;

public class Player {
    private final String name;
    private final String uuid;
    private final long timestamp;

    public Player(String name, String uuid, long timestamp) {
        this.name = name;
        this.uuid = uuid;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
