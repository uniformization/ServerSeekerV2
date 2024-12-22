package xyz.funtimes909.serverseekerv2;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.funtimes909.serverseekerv2.util.ConnectionPool;
import xyz.funtimes909.serverseekerv2.util.MasscanUtils;
import xyz.funtimes909.serverseekerv2.util.PlayerTracking;
import xyz.funtimes909.serverseekerv2.util.ScanManager;
import xyz.funtimes909.serverseekerv2_core.database.Database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static int connectionTimeout;
    public static boolean ignoreBots;
    public static boolean ipLookups;
    public static boolean playerTracking;
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
        try {
            String content = Files.readString(Paths.get(configFile), StandardCharsets.UTF_8);
            JsonObject config = JsonParser.parseString(content).getAsJsonObject();

            connectionTimeout = config.get("connection_timeout").getAsInt();
            ignoreBots = config.get("ignore_bots").getAsBoolean();
            ipLookups = config.get("ip_lookups").getAsBoolean();
            playerTracking = config.get("player_tracking").getAsBoolean();
            postgresUrl = config.get("postgres_url").getAsString();
            postgresUser = config.get("postgres_user").getAsString();
            postgresPassword = config.get("postgres_password").getAsString();
            masscanConf = config.get("masscan_conf").getAsString();
            masscanOutput = config.get("masscan_output").getAsString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Warn user about configs should some of them not exist
        if (postgresUrl.isBlank()) throw new RuntimeException("Error! No postgres URL specified!");
        if (masscanConf.isBlank()) throw new RuntimeException("Error! No masscan configuration specified!");
        if (!MasscanUtils.checkInstalled()) throw new RuntimeException("Error! masscan not found! Try installing masscan and adding it to your $PATH");
        if (postgresUser.isBlank()) logger.warn("Warning! No postgres username specified. Attempting to use default username \"postgres\"");
        if (postgresPassword.isBlank()) logger.warn("Warning! No postgres password specified. You should setup a password for your database");

        // Add the bouncy castle provider
        Security.addProvider(new BouncyCastleProvider());

        // Initialize database Tables and Indexes
        try (Connection conn = ConnectionPool.getConnection()) {
            Database.init(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // TODO Make this not bad
        while (true) {
            MasscanUtils.run();
            if (playerTracking) {
                PlayerTracking.parseList("tracks.json");
                logger.debug("Loading {} players from tracks.json", PlayerTracking.playerTracker.size());
            }
            ScanManager.scan();
            logger.debug("Scan finished. Paused for 5 seconds...");
            Thread.sleep(5000);
        }
    }
}
