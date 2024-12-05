package xyz.funtimes909.serverseekerv2.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import xyz.funtimes909.serverseekerv2.Main;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerTracking {
    public static Map<String, String> playerTracker = new HashMap<>();

    public static void parseList(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            for (JsonElement object : JsonParser.parseReader(reader).getAsJsonArray()) {
                playerTracker.put(
                        object.getAsJsonObject().get("player").getAsString(),
                        object.getAsJsonObject().get("webhook").getAsString()
                );
            }
        } catch (IOException e) {
            Main.logger.error("Error parsing tracks.json!");
            throw new RuntimeException(e);
        }
    }
}