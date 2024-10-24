package xyz.funtimes909.serverseekerv2.builders;

import com.google.gson.annotations.SerializedName;

public class Config {
    @SerializedName("connection_timeout")
    private int connectionTimeout;
    @SerializedName("default_port")
    private int defaultPort;
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
    private boolean rescan;
    @SerializedName("rescan_range")
    private String rescanRange;
    @SerializedName("rescan_subnet")
    private String rescanSubnet;
    @SerializedName("masscan_packet_limit")
    private int masscanPacketLimit;
    @SerializedName("masscan_sudo")
    private boolean masscanSudo;
    @SerializedName("masscan_exclude_file")
    private String masscanExcludeFile;

    // Getters
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getDefaultPort() {
        return defaultPort;
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

    public boolean getRescan() {
        return rescan;
    }

    public String getRescanRange() {
        return rescanRange;
    }

    public String getRescansubnet() {
        return rescanSubnet;
    }

    public int getMasscanPacketLimit() {
        return masscanPacketLimit;
    }

    public boolean getMasscanSudo() {
        return masscanSudo;
    }

    public String getMasscanExcludeFile() {
        return masscanExcludeFile;
    }

}
