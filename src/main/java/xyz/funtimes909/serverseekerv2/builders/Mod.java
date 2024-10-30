package xyz.funtimes909.serverseekerv2.builders;

import com.google.gson.annotations.SerializedName;

public class Mod {
    private final String modId;
    @SerializedName("modmarker")
    private final String modMarker;

    public String getModId() {
        return modId;
    }

    public String getModMarker() {
        return modMarker;
    }

    public Mod(String modId, String modMarker) {
        this.modId = modId;
        this.modMarker = modMarker;
    }
}
