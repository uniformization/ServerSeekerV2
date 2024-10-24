package xyz.funtimes909.serverseekerv2.builders;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Port {
    private short port;
    @SerializedName("proto")
    private String protocol;
    private String status;
    private String reason;
    private int ttl;

    // Getters and setters
    public short getPort() {
        return port;
    }

    public String getProto() {
        return protocol;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public int getTtl() {
        return ttl;
    }
}
