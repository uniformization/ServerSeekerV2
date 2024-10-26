package xyz.funtimes909.serverseekerv2.builders;

import com.google.gson.annotations.SerializedName;

public class Config {
    @SerializedName("connection_timeout")
    private int connectionTimeout;
    @SerializedName("scan_output")
    private String scanOutput;
    private String token;
    @SerializedName("postgres_url")
    private String postgresUrl;
    @SerializedName("postgres_user")
    private String postgresUser;
    @SerializedName("postgres_password")
    private String postgresPassword;
    @SerializedName("ignore_bots")
    private boolean ignoreBots;

    // Getters
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public String getScanOutput() {
        return scanOutput;
    }

    public String getToken() {
        return token;
    }

    public String getPostgresUrl() {
        return postgresUrl;
    }

    public String getPostgresUser() {
        return postgresUser;
    }

    public String getPostgresPassword() {
        return postgresPassword;
    }

    public boolean getIgnoreBots() {
        return ignoreBots;
    }
}
