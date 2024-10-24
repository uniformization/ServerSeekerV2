package xyz.funtimes909.serverseekerv2.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import xyz.funtimes909.serverseekerv2.builders.Config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ConfigParser {
    public static Config parse(String path) {
        Gson gson = new GsonBuilder().create();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            return gson.fromJson(reader, Config.class);
        } catch (IOException e) {
            throw new RuntimeException("Config file not found or malformed json!!");
        }
    }
}
