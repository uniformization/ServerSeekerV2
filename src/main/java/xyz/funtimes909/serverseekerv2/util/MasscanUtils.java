package xyz.funtimes909.serverseekerv2.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import xyz.funtimes909.serverseekerv2.builders.Config;
import xyz.funtimes909.serverseekerv2.builders.Masscan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class MasscanUtils {
    private final Config config;

    public MasscanUtils(Config config) {
        this.config = config;
    }

    public void run() {
        // Build command to run
        StringBuilder command = new StringBuilder("masscan -c" + config.getMasscanConfigLocation());
        if (config.getMasscanSudo()) {
            command.insert(0, "sudo ");
        }

        // Create process and run
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", command.toString());
        processBuilder.inheritIO();

        // Run the process
        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            // Proper logging here
            e.printStackTrace();
        }
    }

    public static List<Masscan> parse(String path) {
        Gson gson = new Gson();

        // Parse masscan output directly to a Masscan object
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            Type serverList = new TypeToken<List<Masscan>>() {}.getType();
            return gson.fromJson(reader, serverList);
        } catch (IOException e) {
            // Proper logging here
            System.out.println("File not found or malformed JSON input!");
        }
        return null;
    }
}
