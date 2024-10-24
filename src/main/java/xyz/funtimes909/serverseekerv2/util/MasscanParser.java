package xyz.funtimes909.serverseekerv2.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import xyz.funtimes909.serverseekerv2.builders.Masscan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class MasscanParser {
    public static List<Masscan> parse(String path) {
        Gson gson = new Gson();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            Type serverList = new TypeToken<List<Masscan>>() {}.getType();
            return gson.fromJson(reader, serverList);
        } catch (IOException e) {
            System.out.println("File not found or malformed JSON input!");
        }
        return null;
    }
}
