package xyz.funtimes909.serverseekerv2.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import xyz.funtimes909.serverseekerv2.Main;
import xyz.funtimes909.serverseekerv2.builders.Masscan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class MasscanUtils {
    public static void run() {
        // Create process and modify attributes
        ProcessBuilder processBuilder;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder = new ProcessBuilder("cmd.exe", "masscan -c " + Main.masscan_conf);
        } else {
            processBuilder = new ProcessBuilder("/bin/sh", "-c", "masscan -c " + Main.masscan_conf);
        }
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
