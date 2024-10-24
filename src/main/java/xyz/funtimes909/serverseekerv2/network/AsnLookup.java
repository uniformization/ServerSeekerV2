package xyz.funtimes909.serverseekerv2.network;

import io.ipinfo.api.IPinfo;
import io.ipinfo.api.errors.RateLimitedException;

public class AsnLookup {
    public static String lookup(String ip, String token) {
        IPinfo asnInfo = new IPinfo.Builder()
                .setToken(token)
                .build();
        try {
            return asnInfo.lookupASN(ip).getAsn();
        } catch (RateLimitedException e) {
            System.out.println("ASN Lookup rate limited! Trying secondary API");
            return "No country information avaliable";
            // TODO More stuff here
        }
    }
}
