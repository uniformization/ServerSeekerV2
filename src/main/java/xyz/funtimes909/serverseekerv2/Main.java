package xyz.funtimes909.serverseekerv2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.funtimes909.serverseekerv2.builders.Config;
import xyz.funtimes909.serverseekerv2.util.ConfigParser;
import xyz.funtimes909.serverseekerv2.util.Database;
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
        connection_timeout = config.getConnectionTimeout();
        ignore_bots = config.getIgnoreBots();
        token = config.getToken();
        postgres_url = config.getPostgresUrl();
        postgres_user = config.getPostgresUser();
        postgres_password = config.getPostgresPassword();
        masscan_conf = config.getMasscanConfigLocation();
        masscan_output = config.getMasscanOutput();

        // Warn user about configs should some of them not exist
        if (token.isBlank())
            logger.warn("Warning! No IpLookup token specified! Information on ip addresses will be limited!");
        if (postgres_user.isBlank())
            logger.warn("Warning! No postgres username specified. Attempting to use default username \"postgres\"");
        if (postgres_password.isBlank())
            logger.warn("Warning! No postgres password specified. You should setup a password for your database");
        if (postgres_url.isBlank()) throw new RuntimeException("Error! No postgres URL specified!");
        if (masscan_conf.isBlank()) throw new RuntimeException("Error! No masscan configuration specified!");

        // Init database connection pool and create tables if they don't exist
        Database.initPool();
        Database.createIfNotExist();

        // TODO Make this not bad
        while (true) {
//            MasscanUtils.run();
            ScanManager.scan();
            Thread.sleep(5000);
        }
    }
}
