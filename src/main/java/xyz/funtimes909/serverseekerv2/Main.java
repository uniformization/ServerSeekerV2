package xyz.funtimes909.serverseekerv2;

import xyz.funtimes909.serverseekerv2.builders.Config;
import xyz.funtimes909.serverseekerv2.database.Database;
import xyz.funtimes909.serverseekerv2.database.DatabaseConnectionPool;
import xyz.funtimes909.serverseekerv2.util.ConfigParser;
import xyz.funtimes909.serverseekerv2.util.ScanManager;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        String config;

        if (args.length == 0) {
            System.out.println("Usage: java -jar serverseekerv2.jar --config <file>");
            return;
        } else {
            config = args[1];
        }

        Config configFile = ConfigParser.parse(config);
        ScanManager scanner = new ScanManager(configFile);
        try {
            DatabaseConnectionPool.initPool(configFile);
            Database.createIfNotExist();
        } catch (Exception e) {
            System.out.println("Database exists! Proceeding with scanning");
        }
        scanner.scan();
    }
}