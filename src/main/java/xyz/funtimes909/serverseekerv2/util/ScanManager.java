package xyz.funtimes909.serverseekerv2.util;

import com.google.gson.*;
import xyz.funtimes909.serverseekerv2.Main;
import xyz.funtimes909.serverseekerv2.builders.Masscan;
import xyz.funtimes909.serverseekerv2.builders.Mod;
import xyz.funtimes909.serverseekerv2.builders.Player;
import xyz.funtimes909.serverseekerv2.builders.Server;
import xyz.funtimes909.serverseekerv2.network.Connect;
import xyz.funtimes909.serverseekerv2.network.HttpUtils;
import xyz.funtimes909.serverseekerv2.network.protocols.Handshake;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ScanManager {
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private static final Semaphore lock = new Semaphore(100);
    private static final StringBuilder motd = new StringBuilder();

    public static void scan() {
        List<Masscan> serverList = MasscanUtils.parse(Main.masscanOutput);
        if (serverList == null) return;

        final int[] count = {serverList.size()};
        for (Masscan server : serverList) {
            Runnable task = () -> {
                try {
                    Socket connection = Connect.connect(server.ip(), server.ports().getFirst().port());

                    if (connection == null) return;
                    String json = Handshake.ping(connection);
                    connection.close();

                    if (json == null) return;
                    buildServer(json, server);
                    if (!MasscanUtils.masscanRunning) Main.logger.debug("Added {} to the database! {} Remaining servers!", server.ip(), count[0]);
                } catch (Exception ignored) {
                } finally {
                    count[0] = count[0] - 1;
                    lock.release();
                }
            };
            lock.acquireUninterruptibly();
            executor.execute(task);
        }
    }

    public static void buildServer(String json, Masscan masscan) {
        try {
            JsonObject parsedJson = JsonParser.parseString(json).getAsJsonObject();

            // Define variables as wrappers to allow null values
            String version = null;
            String icon = null;
            String asn = null;
            String country = null;
            String reverseDns = null;
            String organization = null;
            Boolean preventsChatReports = null;
            Boolean enforcesSecureChat = null;
            Boolean cracked = null;
            Integer protocol = null;
            Integer fmlNetworkVersion = null;
            Integer maxPlayers = null;
            Integer onlinePlayers = null;
            List<Player> playerList = new ArrayList<>();
            List<Mod> modsList = new ArrayList<>();

            // Server information
            String address = masscan.ip();
            short port = masscan.ports().getFirst().port();
            long timestamp = System.currentTimeMillis() / 1000;

            // Country and ASN information
            if (Main.ipLookups) {
                String primaryResponse = HttpUtils.run(address);

                if (primaryResponse != null) {
                    JsonObject parsedPrimaryResponse = JsonParser.parseString(primaryResponse).getAsJsonObject();
                    if (parsedPrimaryResponse.has("reverse")) reverseDns = parsedPrimaryResponse.get("reverse").getAsString();
                    if (parsedPrimaryResponse.has("countryCode")) country = parsedPrimaryResponse.get("countryCode").getAsString();
                    if (parsedPrimaryResponse.has("org")) organization = parsedPrimaryResponse.get("org").getAsString();
                    if (parsedPrimaryResponse.has("as")) asn = parsedPrimaryResponse.get("as").getAsString();
                } else if (!Main.token.isBlank()) {
                    String secondaryResponse = HttpUtils.ipinfo(address);
                    if (secondaryResponse != null) {
                        JsonObject parsedSecondaryResponse = JsonParser.parseString(secondaryResponse).getAsJsonObject();
                        if (parsedSecondaryResponse.has("hostname"))
                            reverseDns = parsedSecondaryResponse.get("hostname").getAsString();
                        if (parsedSecondaryResponse.has("country"))
                            country = parsedSecondaryResponse.get("country").getAsString();
                    }
                }
            }

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
                if (parsedJson.get("description").isJsonObject()) {
                    parseObject(parsedJson.get("description").getAsJsonObject());
                } else {
                    motd.append(parsedJson.get("description").getAsString());
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
                if (parsedJson.get("forgeData").getAsJsonObject().has("mods")) {
                    for (JsonElement modJson : parsedJson.get("forgeData").getAsJsonObject().get("mods").getAsJsonArray().asList()) {
                        String modId = modJson.getAsJsonObject().get("modId").getAsString();
                        String modmarker = modJson.getAsJsonObject().get("modmarker").getAsString();

                        Mod mod = new Mod(modId, modmarker);
                        modsList.add(mod);
                    }
                }
            }

            // Check for players
            if (parsedJson.has("players")) {
                maxPlayers = parsedJson.get("players").getAsJsonObject().get("max").getAsInt();
                onlinePlayers = parsedJson.get("players").getAsJsonObject().get("online").getAsInt();
                if (parsedJson.get("players").getAsJsonObject().has("sample")) {
                    for (JsonElement playerJson : parsedJson.get("players").getAsJsonObject().get("sample").getAsJsonArray().asList()) {
                        if (playerJson.getAsJsonObject().has("name") && playerJson.getAsJsonObject().has("id")) {
                            String name = playerJson.getAsJsonObject().get("name").getAsString();
                            String uuid = playerJson.getAsJsonObject().get("id").getAsString();

                            // Skip building player if uuid is null, has spaces in the name, or has no name
                            if (uuid.equals("00000000-0000-0000-0000-000000000000") || name.contains(" ") || name.isBlank() && Main.ignoreBots) continue;

                            // Offline mode servers use v3 UUID's for players, while regular servers use v4, this is a really easy way to check if a server is offline mode
                            if (UUID.fromString(uuid).version() == 3) cracked = true;

                            for (Map.Entry<String, String> trackedPlayer : PlayerTracking.playerTracker.entrySet()) {
                                if (trackedPlayer.getKey().equalsIgnoreCase(name) && UUID.fromString(uuid).version() == 4) {
                                    HttpUtils.sendWebhook(trackedPlayer.getValue(), trackedPlayer.getKey(), address);
                                    Main.logger.info("{} found in tracks.json, sending POST to webhook", trackedPlayer.getKey());
                                }
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
                    .setReverseDns(reverseDns)
                    .setOrganization(organization)
                    .setVersion(version)
                    .setProtocol(protocol)
                    .setFmlNetworkVersion(fmlNetworkVersion)
                    .setMotd(motd.toString())
                    .setIcon(icon)
                    .setTimesSeen(1)
                    .setPreventsReports(preventsChatReports)
                    .setEnforceSecure(enforcesSecureChat)
                    .setCracked(cracked)
                    .setMaxPlayers(maxPlayers)
                    .setOnlinePlayers(onlinePlayers)
                    .setPlayers(playerList)
                    .setMods(modsList)
                    .build();

            motd.replace(0, motd.length(), "");
            Database.updateServer(server);
        } catch (JsonSyntaxException | IllegalStateException ignored) {}
    }

    private static void parseObject(JsonObject object) {
        for (Map.Entry<String, JsonElement> entry : object.asMap().entrySet()) {
            if (entry.getKey().equals("text")) {
                motd.append(entry.getValue().getAsString());
            } else if (entry.getValue().isJsonArray()) {
                parseArray(entry.getValue().getAsJsonArray());
            } else if (entry.getValue().isJsonObject()) {
                parseObject(entry.getValue().getAsJsonObject());
            }
        }
    }

    private static void parseArray(JsonArray array) {
        for (JsonElement jsonElement : array) {
            if (jsonElement.isJsonPrimitive()) {
                motd.append(jsonElement.getAsString());
            } else if (jsonElement.isJsonArray()) {
                parseArray(jsonElement.getAsJsonArray());
            } else if (jsonElement.isJsonObject()) {
                parseObject(jsonElement.getAsJsonObject());
            }
        }
    }
}
