package xyz.funtimes909.serverseekerv2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.funtimes909.serverseekerv2.builders.Config;
import xyz.funtimes909.serverseekerv2.network.HttpUtils;
import xyz.funtimes909.serverseekerv2.util.*;

import java.io.File;

public class Main {
    public static int connectionTimeout;
    public static boolean ignoreBots;
    public static boolean ipLookups;
    public static boolean playerTracking;
    public static String token;
    public static String postgresUrl;
    public static String postgresUser;
    public static String postgresPassword;
    public static String masscanConf;
    public static String masscanOutput;
    public static final Logger logger = LoggerFactory.getLogger("ServerSeekerV2");

    public static void main(String[] args) throws InterruptedException {
        String configFile;

        // Set config file
        if (args.length == 0) {
            logger.error("Usage: java -jar serverseekerv2.jar --config <file>");
            return;
        } else {
            configFile = args[1];
        }

        // Parse config file and set attributes
        Config config = ConfigParser.parse(configFile);
        connectionTimeout = config.getConnectionTimeout();
        ignoreBots = config.getIgnoreBots();
        ipLookups = config.getIpLookups();
        playerTracking = config.getPlayerTracking();
        token = config.getToken();
        postgresUrl = config.getPostgresUrl();
        postgresUser = config.getPostgresUser();
        postgresPassword = config.getPostgresPassword();
        masscanConf = config.getMasscanConfigLocation();
        masscanOutput = config.getMasscanOutput();

        // Warn user about configs should some of them not exist
        if (postgresUser.isBlank()) logger.warn("Warning! No postgres username specified. Attempting to use default username \"postgres\"");
        if (postgresPassword.isBlank()) logger.warn("Warning! No postgres password specified. You should setup a password for your database");
        if (postgresUrl.isBlank()) throw new RuntimeException("Error! No postgres URL specified!");
        if (masscanConf.isBlank()) throw new RuntimeException("Error! No masscan configuration specified!");

        // Init database connection pool and create tables if they don't exist
        Database.initPool();
        Database.createIfNotExist();

        // TODO Make this not bad
        while (true) {
            MasscanUtils.run();
            logger.debug("Masscan finished running, Paused for 5 seconds...");
            if (playerTracking) {
                PlayerTracking.parseList(new File("tracks.json"));
                logger.debug("Loading {} players from tracks.json", PlayerTracking.playerTracker.size());
            }
            ScanManager.scan();
            Thread.sleep(5000);
        }
    }
}
