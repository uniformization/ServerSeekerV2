package xyz.funtimes909.serverseekerv2.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import xyz.funtimes909.serverseekerv2.Main;
import xyz.funtimes909.serverseekerv2.builders.Masscan;
import xyz.funtimes909.serverseekerv2.builders.Port;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MasscanUtils {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Masscan.class, (JsonDeserializer<Masscan>) (jsonElement, type, ctx) -> {
                JsonObject obj = jsonElement.getAsJsonObject();
                String ip = obj.get("ip").getAsString();
                JsonArray portArr = obj.get("ports").getAsJsonArray();
                List<Port> ports = new ArrayList<>(portArr.size());
                ports.add(ctx.deserialize(portArr.get(0), Port.class));
                return new Masscan(ip, ports);
            })
            .registerTypeAdapter(Port.class, (JsonDeserializer<Port>) (jsonElement, type, ctx) -> new Port(jsonElement.getAsJsonObject().get("port").getAsShort()))
            .create();

    public static void run() {
        // Create process and modify attributes
        ProcessBuilder processBuilder;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder = new ProcessBuilder("cmd.exe", "masscan -c " + Main.masscanConf);
        } else {
            processBuilder = new ProcessBuilder("/bin/sh", "-c", "sudo masscan -c " + Main.masscanConf);
        }
        processBuilder.inheritIO();

        try {
            Main.logger.info("Starting masscan. Press Control+C to stop the scan");
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkInstalled() {
        String os = System.getProperty("os.name").toLowerCase();
        String checkCommand = (os.contains("win")) ? "where" : "which";

        try {
            Process process = new ProcessBuilder(checkCommand, "masscan").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public static List<Masscan> parse(String path) {
        // Parse masscan output to a list of Masscan objects
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            Type serverList = new TypeToken<List<Masscan>>() {}.getType();
            return GSON.fromJson(reader, serverList);
        } catch (IOException e) {
            throw new RuntimeException("Masscan output not found or no servers were found, Aborting scan!");
        }
    }

    public static Masscan parseServer(String json) {
        Type server = new TypeToken<Masscan>(){}.getType();
        return GSON.fromJson(json, server);
    }
}