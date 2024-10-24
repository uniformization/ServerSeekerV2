package xyz.funtimes909.serverseekerv2.network;

import io.ipinfo.api.IPinfo;
import io.ipinfo.api.errors.RateLimitedException;

public class IpLookup {
    public static String lookup(String ip, String token) {
        IPinfo ipInfo = new IPinfo.Builder()
                .setToken(token)
                .build();
        try {
            return ipInfo.lookupIP(ip).getCountryName();
        } catch (RateLimitedException e) {
            System.out.println("GeoIP lookup rate limited! Trying secondary API");
            return "No country information avaliable";
            // TODO More stuff here
        }
    }
}
