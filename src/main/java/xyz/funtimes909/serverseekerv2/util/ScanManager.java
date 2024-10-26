package xyz.funtimes909.serverseekerv2.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.funtimes909.serverseekerv2.builders.Masscan;
import xyz.funtimes909.serverseekerv2.builders.Player;
import xyz.funtimes909.serverseekerv2.builders.forge.Mod;
import xyz.funtimes909.serverseekerv2.builders.Config;
import xyz.funtimes909.serverseekerv2.builders.Server;
import xyz.funtimes909.serverseekerv2.database.Database;
import xyz.funtimes909.serverseekerv2.network.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScanManager {
    private final int connectionTimeout;
    private final String scanOutput;
    private final String token;
    private final boolean ignoreBots;

    public ScanManager(Config config) {
        this.connectionTimeout = config.getConnectionTimeout();
        this.scanOutput = config.getScanOutput();
        this.token = config.getToken();
        this.ignoreBots = config.getIgnoreBots();
    }

    public void scan() {
        List<Masscan> serverList = MasscanParser.parse(scanOutput);
        iterate(serverList);
    }

    public void iterate(List<Masscan> serverList) {
        for (Masscan server : serverList) {
            Socket connection = Connect.connect(server.getIp(), server.getPort(), connectionTimeout);
            if (connection != null) {
                String ping = Pinger.ping(connection);
                if (ping != null) {
                    buildServer(ping, server, token, ignoreBots);
                }
            }
        }
        // Recall method to scan the file again, assuming constant rescans with masscan are happening and overwriting the existing file
    }

    public static void buildServer(String json, Masscan masscan, String token, boolean ignoreBots) {
        try {
            JsonObject parsedJson = JsonParser.parseString(json).getAsJsonObject();

            // Define variables as wrappers to allow null values
            String version = null;
            String motd = null;
            String icon = null;
            String asn = null;
            String country = null;
            Boolean preventsChatReports = null;
            Boolean enforcesSecureChat = null;
            Boolean whitelist = null;
            Boolean cracked = null;
            Integer protocol = null;
            Integer fmlNetworkVersion = null;
            Integer maxPlayers = null;
            List<Player> playerList = new ArrayList<>();
            List<Mod> modsList = new ArrayList<>();

            // Server information
            String address = masscan.getIp();
            short port = masscan.getPort();
            long timestamp = System.currentTimeMillis() / 1000;

            // IP Information
            asn = AsnLookup.lookup(address, token);
            country = AsnLookup.lookup(address, token);

            // Minecraft server information
            if (parsedJson.has("version")) {
                version = parsedJson.get("version").getAsJsonObject().get("name").getAsString();
                protocol = parsedJson.get("version").getAsJsonObject().get("protocol").getAsInt();
            }

            // Check for icon
            if (parsedJson.has("favicon")) {
                icon = parsedJson.get("favicon").getAsString();
            }

            // Description can be either an object or a string
            if (parsedJson.has("description")) {
                if (parsedJson.get("description").isJsonPrimitive()) {
                    motd = parsedJson.get("description").getAsString();
                } else if (parsedJson.get("description").isJsonObject()) {
                    motd = parsedJson.get("description").getAsJsonObject().get("text").getAsString();
                }
            }

            if (parsedJson.has("enforcesSecureChat")) {
                enforcesSecureChat = parsedJson.get("enforcesSecureChat").getAsBoolean();
            }

            if (parsedJson.has("preventsChatReports")) {
                preventsChatReports = parsedJson.get("preventsChatReports").getAsBoolean();
            }

            // Forge servers send back information about mods
            if (parsedJson.has("forgeData")) {
                fmlNetworkVersion = parsedJson.get("forgeData").getAsJsonObject().get("fmlNetworkVersion").getAsInt();
                // Build Modlist
                if (parsedJson.get("forgeData").getAsJsonObject().has("mods")) {
                    for (JsonElement modJson : parsedJson.get("forgeData").getAsJsonObject().get("mods").getAsJsonArray().asList()) {
                        String modId = modJson.getAsJsonObject().get("modId").getAsString();
                        String modmarker = modJson.getAsJsonObject().get("modmarker").getAsString();

                        Mod mod = new Mod(modId, modmarker);
                        modsList.add(mod);
                    }
                }
            }

            // Check for players and build if found
            if (parsedJson.has("players")) {
                maxPlayers = parsedJson.get("players").getAsJsonObject().get("max").getAsInt();
                if (parsedJson.get("players").getAsJsonObject().has("sample")) {
                    for (JsonElement playerJson : parsedJson.get("players").getAsJsonObject().get("sample").getAsJsonArray().asList()) {
                        if (playerJson.getAsJsonObject().has("name") && playerJson.getAsJsonObject().has("id")) {
                            String name = playerJson.getAsJsonObject().get("name").getAsString();
                            String uuid = playerJson.getAsJsonObject().get("id").getAsString();

                            // Skip building player if uuid is null, likely a bot
                            if (uuid.equals("00000000-0000-0000-0000-000000000000") && ignoreBots) { continue; }

                            // Offline mode servers use v3 UUID's for players, while regular servers use v4, this is a really easy way to check if a server is offline mode
                            if (UUID.fromString(uuid).version() == 3) {
                                cracked = true;
                            }

                            Player player = new Player(name, uuid, timestamp);
                            playerList.add(player);
                        }
                    }
                }
            }

            // Build server
            Server server = new Server.Builder()
                    .setAddress(address)
                    .setPort(port)
                    .setTimestamp(timestamp)
                    .setAsn(asn)
                    .setCountry(country)
                    .setVersion(version)
                    .setProtocol(protocol)
                    .setFmlNetworkVersion(fmlNetworkVersion)
                    .setMotd(motd)
                    .setIcon(icon)
                    .setTimesSeen(1)
                    .setPreventsReports(preventsChatReports)
                    .setEnforceSecure(enforcesSecureChat)
                    .setCracked(cracked)
                    .setMaxPlayers(maxPlayers)
                    .setPlayers(playerList)
                    .setMods(modsList)
                    .build();

            Database.updateServer(server);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
