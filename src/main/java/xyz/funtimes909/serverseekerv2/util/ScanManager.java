package xyz.funtimes909.serverseekerv2.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.funtimes909.serverseekerv2.Main;
import xyz.funtimes909.serverseekerv2.builders.Masscan;
import xyz.funtimes909.serverseekerv2.builders.Mod;
import xyz.funtimes909.serverseekerv2.builders.Player;
import xyz.funtimes909.serverseekerv2.builders.Server;
import xyz.funtimes909.serverseekerv2.network.Connect;
import xyz.funtimes909.serverseekerv2.network.IpLookup;
import xyz.funtimes909.serverseekerv2.network.Pinger;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanManager {
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public static void scan() {
        List<Masscan> serverList = MasscanUtils.parse(Main.masscan_output);
        if (serverList == null) return;

        List<Runnable> tasks = new ArrayList<>();

        for (Masscan server : serverList) {
            Runnable task = () -> {
                Socket connection = Connect.connect(server.getIp(), server.getPorts().getFirst().getPort());
                if (connection == null) return;
                String json = Pinger.ping(connection);
                if (json != null) buildServer(json, server);
            };
            tasks.add(task);
        }

        for (Runnable task : tasks) {
            executor.execute(task);
        }
    }

    public static void buildServer(String json, Masscan masscan) {
        JsonObject parsedJson = JsonParser.parseString(json).getAsJsonObject();

        // Define variables as wrappers to allow null values
        String version = null;
        String motd = null;
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
        String address = masscan.getIp();
        short port = masscan.getPorts().getFirst().getPort();
        long timestamp = System.currentTimeMillis() / 1000;

        JsonObject ipLookupResponse = IpLookup.run(address);
        if (ipLookupResponse.has("reverse")) reverseDns = ipLookupResponse.get("reverse").getAsString();
        if (ipLookupResponse.has("country")) country = ipLookupResponse.get("country").getAsString();
        if (ipLookupResponse.has("org")) organization = ipLookupResponse.get("org").getAsString();
        if (ipLookupResponse.has("as")) asn = ipLookupResponse.get("as").getAsString();

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
                        if (uuid.equals("00000000-0000-0000-0000-000000000000") || name.contains(" ") || name.isBlank() && Main.ignore_bots) continue;

                        // Offline mode servers use v3 UUID's for players, while regular servers use v4, this is a really easy way to check if a server is offline mode
                        if (UUID.fromString(uuid).version() == 3) cracked = true;

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
                .setMotd(motd)
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

        Main.logger.info("built server " + server.getAddress());
        Database.updateServer(server);
    }
}
