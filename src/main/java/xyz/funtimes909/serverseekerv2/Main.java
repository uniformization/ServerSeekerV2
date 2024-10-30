package xyz.funtimes909.serverseekerv2;

import xyz.funtimes909.serverseekerv2.builders.Config;
import xyz.funtimes909.serverseekerv2.util.ConfigParser;
import xyz.funtimes909.serverseekerv2.util.Database;
import xyz.funtimes909.serverseekerv2.util.MasscanUtils;
import xyz.funtimes909.serverseekerv2.util.ScanManager;

public class Main {
    public static int connection_timeout;
    public static boolean ignore_bots;
    public static String token;
    public static String postgres_url;
    public static String postgres_user;
    public static String postgres_password;
    public static String masscan_conf;
    public static String masscan_output;

    public static void main(String[] args) {
        String configFile;

        // Set config file
        if (args.length == 0) {
            System.out.println("Usage: java -jar serverseekerv2.jar --config <file>");
            return;
        } else {
            configFile = args[1];
        }

        // Parse config file and set attributes
        Config config = ConfigParser.parse(configFile);
        connection_timeout = config.getConnectionTimeout();
        ignore_bots = config.getIgnoreBots();
        token = config.getToken();
        postgres_url = config.getPostgresUrl();
        postgres_user = config.getPostgresUser();
        postgres_password = config.getPostgresPassword();
        masscan_conf = config.getMasscanConfigLocation();
        masscan_output = config.getMasscanOutput();

        // Init database connection pool and create tables if they don't exist
        Database.initPool();
        Database.createIfNotExist();

        // TODO Make this not bad
        while (true) {
            MasscanUtils.run();
            ScanManager.scan();
        }

    }
}