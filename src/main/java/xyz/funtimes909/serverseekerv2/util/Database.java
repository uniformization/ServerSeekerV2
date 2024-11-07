package xyz.funtimes909.serverseekerv2.util;

import org.apache.commons.dbcp2.BasicDataSource;
import xyz.funtimes909.serverseekerv2.Main;
import xyz.funtimes909.serverseekerv2.builders.Mod;
import xyz.funtimes909.serverseekerv2.builders.Player;
import xyz.funtimes909.serverseekerv2.builders.Server;

import java.sql.*;
import java.util.List;

public class Database{
    private static final BasicDataSource dataSource = new BasicDataSource();

    public static void initPool() {
        dataSource.setUrl("jdbc:postgresql://" + Main.postgres_url);
        dataSource.setPassword(Main.postgres_password);
        dataSource.setUsername(Main.postgres_user);
    }

    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database!", e);
        }
    }

    public static void createIfNotExist() {
        Main.logger.info("Attempting to create database tables...");
        try (Connection conn = getConnection()) {
            Statement tables = conn.createStatement();
            // Servers
            tables.addBatch("CREATE TABLE IF NOT EXISTS Servers (" +
                    "Address TEXT," +
                    "Port NUMERIC," +
                    "FirstSeen INT," +
                    "LastSeen INT," +
                    "Country TEXT," +
                    "Asn TEXT," +
                    "ReverseDNS TEXT," +
                    "Organization TEXT," +
                    "Version TEXT," +
                    "Protocol INT," +
                    "FmlNetworkVersion INT," +
                    "Motd TEXT," +
                    "Icon TEXT," +
                    "TimesSeen INT," +
                    "PreventsReports BOOLEAN DEFAULT NULL," +
                    "EnforceSecure BOOLEAN DEFAULT NULL," +
                    "Whitelist BOOLEAN DEFAULT NULL," +
                    "Cracked BOOLEAN DEFAULT NULL ," +
                    "MaxPlayers INT," +
                    "OnlinePlayers INT," +
                    "PRIMARY KEY (Address, Port))");

            // Player History
            tables.addBatch("CREATE TABLE IF NOT EXISTS PlayerHistory (" +
                    "Address TEXT," +
                    "Port INT," +
                    "PlayerUUID TEXT," +
                    "PlayerName TEXT," +
                    "FirstSeen INT," +
                    "LastSeen INT," +
                    "PRIMARY KEY (Address, Port, PlayerUUID)," +
                    "FOREIGN KEY (Address, Port) REFERENCES Servers(Address, Port))");

            // Mods
            tables.addBatch("CREATE TABLE IF NOT EXISTS Mods (" +
                    "Address TEXT," +
                    "Port INT," +
                    "ModID TEXT," +
                    "ModMarker TEXT," +
                    "PRIMARY KEY (Address, Port, ModId)," +
                    "FOREIGN KEY (Address, Port) REFERENCES Servers(Address, Port))");

            // Indexes
            tables.addBatch("CREATE INDEX IF NOT EXISTS ServersIndex ON Servers (Motd, maxplayers, version, onlineplayers, country, icon, cracked, preventsreports)");

            tables.addBatch("CREATE INDEX IF NOT EXISTS PlayersIndex ON PlayerHistory (playername)");

            tables.addBatch("CREATE INDEX IF NOT EXISTS ModsIndex ON Mods (modid)");

            tables.executeBatch();
            tables.close();
        } catch (SQLException e) {
            Main.logger.error("Failed to create database tables!", e);
        }
    }

    public static void updateServer(Server server) {
        try (Connection conn = getConnection()) {
            String address = server.getAddress();
            short port = server.getPort();
            long timestamp = server.getTimestamp();
            String country = server.getCountry();
            String asn = server.getAsn();
            String reverseDns = server.getReverseDns();
            String organization = server.getOrganization();
            String version = server.getVersion();
            Integer protocol = server.getProtocol();
            Integer fmlNetworkVersion = server.getFmlNetworkVersion();
            String motd = server.getMotd();
            String icon = server.getIcon();
            int timesSeen = server.getTimesSeen();
            Boolean preventsReports = server.getPreventsReports();
            Boolean enforceSecure = server.getEnforceSecure();
            Boolean whitelist = server.getWhitelist();
            Boolean cracked = server.getCracked();
            Integer maxPlayers = server.getMaxPlayers();
            Integer onlinePlayers = server.getOnlinePlayers();
            List<Player> players = server.getPlayers();
            List<Mod> mods = server.getMods();

            // Attempt to insert new server, if address and port already exist, update relevant information
            PreparedStatement insertServer = conn.prepareStatement("INSERT INTO Servers " +
                    "(Address," +
                    "Port," +
                    "FirstSeen," +
                    "LastSeen," +
                    "Country," +
                    "Asn," +
                    "ReverseDNS," +
                    "Organization," +
                    "Version," +
                    "Protocol," +
                    "FmlNetworkVersion," +
                    "Motd," +
                    "Icon," +
                    "TimesSeen," +
                    "PreventsReports," +
                    "EnforceSecure," +
                    "Whitelist," +
                    "Cracked," +
                    "MaxPlayers," +
                    "OnlinePlayers)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                    "ON CONFLICT (Address, Port) DO UPDATE SET " +
                    "LastSeen = EXCLUDED.LastSeen," +
                    "Country = EXCLUDED.Country," +
                    "Asn = EXCLUDED.Asn," +
                    "ReverseDNS = EXCLUDED.ReverseDNS," +
                    "Organization = EXCLUDED.Organization," +
                    "Version = EXCLUDED.Version," +
                    "Protocol = EXCLUDED.Protocol," +
                    "FmlNetworkVersion = EXCLUDED.FmlNetworkVersion," +
                    "Motd = EXCLUDED.Motd," +
                    "Icon = EXCLUDED.Icon," +
                    "TimesSeen = Servers.TimesSeen + 1," +
                    "PreventsReports = EXCLUDED.PreventsReports," +
                    "EnforceSecure = EXCLUDED.EnforceSecure," +
                    "Whitelist = EXCLUDED.Whitelist," +
                    "Cracked = EXCLUDED.Cracked," +
                    "MaxPlayers = EXCLUDED.MaxPlayers," +
                    "OnlinePlayers = EXCLUDED.OnlinePlayers");

            // Set most values as objects to insert a null if value doesn't exist
            insertServer.setString(1, address);
            insertServer.setInt(2, port);
            insertServer.setLong(3, timestamp);
            insertServer.setLong(4, timestamp);
            insertServer.setString(5, country);
            insertServer.setString(6, asn);
            insertServer.setString(7, reverseDns);
            insertServer.setString(8, organization);
            insertServer.setString(9, version);
            insertServer.setObject(10, protocol, Types.INTEGER);
            insertServer.setObject(11, fmlNetworkVersion, Types.INTEGER);
            insertServer.setString(12, motd);
            insertServer.setString(13, icon);
            insertServer.setInt(14, timesSeen);
            insertServer.setObject(15, preventsReports, Types.BOOLEAN);
            insertServer.setObject(16, enforceSecure, Types.BOOLEAN);
            insertServer.setObject(17, whitelist, Types.BOOLEAN);
            insertServer.setObject(18, cracked, Types.BOOLEAN);
            insertServer.setObject(19, maxPlayers, Types.INTEGER);
            insertServer.setObject(20, onlinePlayers, Types.INTEGER);
            insertServer.executeUpdate();
            insertServer.close();

            // Add players, update LastSeen and Name (Potential name change) if duplicate
            if (!players.isEmpty()) {
                PreparedStatement updatePlayers = conn.prepareStatement("INSERT INTO PlayerHistory (Address, Port, PlayerUUID, PlayerName, FirstSeen, LastSeen) VALUES (?, ?, ?, ?, ?, ?) " +
                        "ON CONFLICT (Address, Port, PlayerUUID) DO UPDATE SET " +
                        "LastSeen = EXCLUDED.LastSeen," +
                        "PlayerName = EXCLUDED.PlayerName");

                // Constants
                updatePlayers.setString(1, address);
                updatePlayers.setShort(2, port);

                for (Player player : players) {
                    updatePlayers.setString(3, player.getUuid());
                    updatePlayers.setString(4, player.getName());
                    updatePlayers.setLong(5, player.getTimestamp());
                    updatePlayers.setLong(6, player.getTimestamp());
                    updatePlayers.addBatch();
                }

                // Execute and close
                updatePlayers.executeBatch();
                updatePlayers.close();
            }

            // Add mods, do nothing if duplicate
            if (!mods.isEmpty()) {
                PreparedStatement updateMods = conn.prepareStatement("INSERT INTO Mods (Address, Port, ModId, ModMarker) " +
                        "VALUES (?, ?, ?, ?)" +
                        "ON CONFLICT (Address, Port, ModId) DO NOTHING");

                // Constants
                updateMods.setString(1, address);
                updateMods.setShort(2, port);

                for (Mod mod : mods) {
                    updateMods.setString(3, mod.modId());
                    updateMods.setString(4, mod.modMarker());
                    updateMods.addBatch();
                }

                // Execute and close
                updateMods.executeBatch();
                updateMods.close();
            }
            System.gc();
            Main.logger.info("Added {} to the database!", server.getAddress());
        } catch (SQLException e) {
            Main.logger.error("There was an error during a database transaction!", e);
        }
    }
}
