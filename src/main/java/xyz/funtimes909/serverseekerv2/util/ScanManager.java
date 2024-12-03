package xyz.funtimes909.serverseekerv2.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import xyz.funtimes909.serverseekerv2.Main;
import xyz.funtimes909.serverseekerv2.builders.Masscan;
import xyz.funtimes909.serverseekerv2.builders.Mod;
import xyz.funtimes909.serverseekerv2.builders.Player;
import xyz.funtimes909.serverseekerv2.builders.Server;
import xyz.funtimes909.serverseekerv2.network.Connect;
import xyz.funtimes909.serverseekerv2.network.HttpUtils;
import xyz.funtimes909.serverseekerv2.network.protocols.Handshake;
import xyz.funtimes909.serverseekerv2.network.protocols.QuickLogin;
import xyz.funtimes909.serverseekerv2.types.LoginAttempt;

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
    private static final Semaphore lock = new Semaphore(2500);

    public static void scan() {
        List<Masscan> serverList = MasscanUtils.parse(Main.masscanOutput);
        if (serverList == null) return;

        final int[] count = {serverList.size()};
        for (Masscan server : serverList) {
            Runnable task = () -> {
                try {
                    String ip = server.ip();
                    short port = server.ports().getFirst().port();

                    JsonObject parsedJson;
                    try (Socket so = Connect.connect(ip, port)) {
                        String json = Handshake.ping(so);
                        parsedJson = JsonParser.parseString(json).getAsJsonObject();
                    }
                    if (parsedJson == null) return;

                    // Servers close connection after handshake, we need to make a new socket
                    LoginAttempt loginAttempt = LoginAttempt.UNKNOWN;
                    try (Socket so = Connect.connect(ip, port)) {
                        loginAttempt = QuickLogin.quickLogin(
                                so,
                                // Get the protocol version of the server from the handshake we did with it
                                parsedJson.get("version").getAsJsonObject().get("protocol").getAsInt()
                        );
                    } catch (Exception ignored) { } // Even if the login method failed, still log the rest of the info

                    buildServer(server, parsedJson, loginAttempt);
                    if (!MasscanUtils.masscanRunning) {
                        Main.logger.debug("Added {} to the database! {} Remaining servers!", ip, count[0]);
                    }
                } catch (Exception ignored) {
                } finally {
                    lock.release();
                    count[0] = count[0] - 1;
                }
            };
            lock.acquireUninterruptibly();
            executor.execute(task);
        }
    }

    public static void buildServer(Masscan masscan, JsonObject parsedJson, LoginAttempt loginAttempt) {
        try {
            // Define variables as wrappers to allow null values
            String version = null;
            StringBuilder motd = new StringBuilder();
            String asn = null;
            String country = null;
            String reverseDns = null;
            String organization = null;
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

            // Description can be either an object or a string
            if (parsedJson.has("description")) {
                if (parsedJson.get("description").isJsonObject()) {
                    parseMOTD(parsedJson.get("description").getAsJsonObject(), 10, motd);
                } else {
                    motd.append(parsedJson.get("description").getAsString());
                }
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
                            if (name.contains(" ") || name.isBlank() && Main.ignoreBots) continue;

                            for (Map.Entry<String, String> trackedPlayer : PlayerTracking.playerTracker.entrySet()) {
                                if (trackedPlayer.getKey().equalsIgnoreCase(name) && UUID.fromString(uuid).version() == 4) {
                                    HttpUtils.sendWebhook(trackedPlayer.getValue(), trackedPlayer.getKey(), address);
                                    Main.logger.info("{} found in tracks.json, sending POST to webhook", trackedPlayer.getKey());
                                }
                            }

                            playerList.add(new Player(name, uuid, timestamp));
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
                    .setTimesSeen(1)
                    .setIcon(parsedJson.has("icon") ? parsedJson.get("icon").getAsString() : null)
                    .setPreventsReports(parsedJson.has("preventsChatReports") ? parsedJson.get("preventsChatReports").getAsBoolean() : null)
                    .setEnforceSecure(parsedJson.has("enforcesSecureChat") ? parsedJson.get("enforcesSecureChat").getAsBoolean() : null)
                    .setCracked(!loginAttempt.online)
                    .setWhitelist(loginAttempt.whitelist)
                    .setMaxPlayers(maxPlayers)
                    .setOnlinePlayers(onlinePlayers)
                    .setPlayers(playerList)
                    .setMods(modsList)
                    .build();

            Database.updateServer(server);
        } catch (JsonSyntaxException | IllegalStateException ignored) {}
    }

    private static void parseMOTD(JsonElement element, int limit, StringBuilder motd) {
        if (limit == 0) return;

        if (element.isJsonObject()) {
            Map<String, JsonElement> map = element.getAsJsonObject().asMap();

            if (map.containsKey("text")) {
                if (map.containsKey("color")) {
                    motd.append('§').append(AnsiCodes.codes.get(map.get("color").getAsString()).c);
                }
                motd.append(map.get("text").getAsString());
            }

            if (map.containsKey("extra")) {
                parseMOTD(map.get("extra"), limit, motd);
            }
        } else {
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                if (jsonElement.isJsonPrimitive()) {
                    motd.append(jsonElement.getAsString());
                } else {
                    parseMOTD(jsonElement, limit, motd);
                }
            }
        }
    }
}
