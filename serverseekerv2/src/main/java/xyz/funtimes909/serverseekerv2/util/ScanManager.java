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
import xyz.funtimes909.serverseekerv2_core.types.LoginAttempt;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ScanManager {
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private static final Semaphore lock = new Semaphore(2500);
    public static int size;

    public static void scan() {
        List<Masscan> serverList = MasscanUtils.parse(Main.masscanOutput);
        if (serverList == null) return;
        size = serverList.size();

        ProgressBarStyle style = ProgressBarStyle.builder()
                .leftBracket("[")
                .rightBracket("]")
                .block('=')
                .rightSideFractionSymbol('>')
                .build();

        ProgressBarBuilder bar = new ProgressBarBuilder()
                .clearDisplayOnFinish()
                .setStyle(style)
                .showSpeed()
                .setTaskName("Scanning")
                .continuousUpdate()
                .setInitialMax(size);

        for (Masscan server : ProgressBar.wrap(serverList, bar)) {
            Runnable task = () -> {
                try {
                    String address = server.ip();
                    short port = server.ports().getFirst().port();

                    JsonObject parsedJson;
                    try (Socket so = Connect.connect(address, port)) {
                        String json = Handshake.ping(so);
                        parsedJson = JsonParser.parseString(json).getAsJsonObject();
                    }
                    if (parsedJson == null) return;

                    // Servers close connection after handshake, we need to make a new socket
                    LoginAttempt loginAttempt = LoginAttempt.UNKNOWN;
                    try (Socket so = Connect.connect(address, port)) {
                        loginAttempt = QuickLogin.quickLogin(
                                so,
                                // Get the protocol version of the server from the handshake
                                parsedJson.get("version").getAsJsonObject().get("protocol").getAsInt()
                        );
                    } catch (Exception ignored) { } // Even if the login method failed, still log the rest of the info
                } catch (Exception ignored) {
                } finally {
                    lock.release();
                    size -= 1;
                }
            };
            lock.acquireUninterruptibly();
            executor.execute(task);
        }
    }
}
