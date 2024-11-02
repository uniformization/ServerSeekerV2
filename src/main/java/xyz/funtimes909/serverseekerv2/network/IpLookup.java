package xyz.funtimes909.serverseekerv2.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ipinfo.api.IPinfo;
import io.ipinfo.api.errors.RateLimitedException;
import xyz.funtimes909.serverseekerv2.Main;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class IpLookup {
    public static String run(String ip) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://ip-api.com/json/" + ip + "?fields=status,message,continent,countryCode,org,as,reverse,query"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
        if (response.statusCode() == 429) return null;
        return response.body();
    }

    public static String ipinfo(String ip) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ipinfo.io/" + ip + "/json"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
        if (response.statusCode() == 429) return null;
        return response.body();
    }
}
