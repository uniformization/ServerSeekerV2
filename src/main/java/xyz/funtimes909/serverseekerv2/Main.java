package xyz.funtimes909.serverseekerv2;

import xyz.funtimes909.serverseekerv2.builders.Config;
import xyz.funtimes909.serverseekerv2.database.Database;
import xyz.funtimes909.serverseekerv2.database.DatabaseConnectionPool;
import xyz.funtimes909.serverseekerv2.util.ConfigParser;
import xyz.funtimes909.serverseekerv2.util.MasscanUtils;
import xyz.funtimes909.serverseekerv2.util.ScanManager;

public class Main {
    public static void main(String[] args) {
        String config;

        // Enforce setting config on launch
        if (args.length == 0) {
            System.out.println("Usage: java -jar serverseekerv2.jar --config <file>");
            return;
        } else {
            config = args[1];
        }

        // Parse config file and init database connection pool
        Config configFile = ConfigParser.parse(config);
        DatabaseConnectionPool.initPool(configFile);

        // Create the required database tables if they don't exist
        Database.createIfNotExist();

        MasscanUtils masscanRunner = new MasscanUtils(configFile);
        ScanManager scanner = new ScanManager(configFile);

        // TODO Make this async with a callback
        while (true) {
            masscanRunner.run();
            scanner.scan();
        }

    }
}