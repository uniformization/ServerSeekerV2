package xyz.funtimes909.serverseekerv2.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import xyz.funtimes909.serverseekerv2.Main;
import xyz.funtimes909.serverseekerv2.builders.Masscan;
import xyz.funtimes909.serverseekerv2.network.Connect;
import xyz.funtimes909.serverseekerv2.network.protocols.Handshake;
import xyz.funtimes909.serverseekerv2.network.protocols.QuickLogin;
import xyz.funtimes909.serverseekerv2_core.database.Database;
import xyz.funtimes909.serverseekerv2_core.records.Server;
import xyz.funtimes909.serverseekerv2_core.types.LoginAttempt;
import xyz.funtimes909.serverseekerv2_core.util.ServerObjectBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ScanManager {
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private static final Semaphore lock = new Semaphore(2500);
    public static int size;

    private static void scanServer(Masscan server) {
        Runnable task = () -> {
            try {
                String address = server.ip();
                short port = server.ports().getFirst().port();

                JsonObject parsedJson;
                try (Socket so = Connect.connect(address, port)) {
                    String json = Handshake.ping(so);
                    parsedJson = JsonParser.parseString(json).getAsJsonObject();
                }

                // Servers close connection after handshake, we need to make a new socket
                LoginAttempt loginAttempt = null;
                // TODO: uncomment if the jvm is given more than 2gb of ram
                /*try (Socket so = Connect.connect(address, port)) {
                    loginAttempt = QuickLogin.quickLogin(
                            so,
                            // Get the protocol version of the server from the handshake
                            parsedJson.get("version").getAsJsonObject().get("protocol").getAsInt()
                    );
                }*/ // Even if the login method failed, still log the rest of the info

                // Build server using server ping and the loginAttempt
                Server builtServer = ServerObjectBuilder.buildServerFromPing(
                        address,
                        port,
                        parsedJson,
                        loginAttempt
                );

                if (builtServer == null) return;

                Connection conn = ConnectionPool.getConnection();
                Database.updateServer(conn, builtServer);
            } catch (Exception ignored) {
            } finally {
                lock.release();
                size -= 1;
            }
        };
        lock.acquireUninterruptibly();
        executor.execute(task);
    }

    private static int countLines(String path) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(Main.masscanOutput))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.equals("[") || line.equals("]")) continue;
                count++;
            }
        } catch (IOException e) {
            Main.logger.info("Failed to read masscan output file!");
            e.printStackTrace();
        }
        return count;
    }

    public static void scan() {
        int serverCount = countLines(Main.masscanOutput);
        size = serverCount;

        ProgressBarStyle style = ProgressBarStyle.builder()
                .leftBracket("[")
                .rightBracket("]")
                .block('=')
                .rightSideFractionSymbol('>')
                .build();

        ProgressBarBuilder builder = new ProgressBarBuilder()
                .clearDisplayOnFinish()
                .setStyle(style)
                .showSpeed()
                .setTaskName("Scanning")
                .continuousUpdate()
                .setInitialMax(serverCount);

        try (ProgressBar bar = builder.build()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(Main.masscanOutput))) {
                int chunkSize = 16;
                String[] lines = new String[chunkSize];

                int count = 0;

                String line;
                while ((line = reader.readLine()) != null) {
                    bar.step();

                    // masscan doesn't use JSONL anymore, so we need to fix every line
                    // before using it
                    if (line.isEmpty() || line.equals("[") || line.equals("]")) continue;
                    if (line.startsWith("[")) line = line.substring(1);
                    if (line.endsWith("]") || line.endsWith(",")) {
                        line = line.substring(0, line.length() - 1);
                    }

                    lines[count] = line;
                    count++;

                    if (count == chunkSize) {
                        for (int i = 0; i < count; i++) {
                            Masscan server = MasscanUtils.parseServer(lines[i]);
                            scanServer(server);
                        }
                        count = 0;
                    }
                }

                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        Masscan server = MasscanUtils.parseServer(lines[i]);
                        scanServer(server);
                    }
                }
            } catch (IOException e) {
                Main.logger.info("Failed to read masscan output file!");
                e.printStackTrace();
            }
        }
    }
}