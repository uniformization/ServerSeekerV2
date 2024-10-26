package xyz.funtimes909.serverseekerv2.util;

import xyz.funtimes909.serverseekerv2.builders.Masscan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MasscanParser {
    public static List<Masscan> parse(String path) {
        try {
            List<Masscan> list = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(path));
            reader.lines().forEach(line -> {
                String[] array = line.split(" ");
                // Ignore if the line commented out
                if (!(array[0].charAt(0) == '#')) {
                    Masscan server = new Masscan(array[3], Short.parseShort(array[2]));
                    list.add(server);
                }
            });
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
