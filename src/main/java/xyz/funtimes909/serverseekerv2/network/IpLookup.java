package xyz.funtimes909.serverseekerv2.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class IpLookup {
    public static JsonObject run(String ip) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .version(HttpClient.Version.HTTP_2)
                .uri(URI.create("http://ip-api.com/json/" + ip + "?fields=status,message,continent,country,org,as,reverse,query"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }
}
