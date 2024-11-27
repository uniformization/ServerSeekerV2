package xyz.funtimes909.serverseekerv2.types.protocols;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.funtimes909.serverseekerv2.types.AbstractProtocol;
import xyz.funtimes909.serverseekerv2.types.varlen.VarString;

public class Disconnect implements AbstractProtocol<Disconnect> {
    public JsonObject reason = new JsonObject();

    public Disconnect() {}
    private Disconnect(JsonObject reason) {
        this.reason = reason;
    }

    @Override
    public int PROTOCOL() {
        return 0;
    }

    @Override
    public Disconnect decode(byte[] in) {
        return new Disconnect(
                JsonParser.parseString(VarString.decode(in, 1).get()).getAsJsonObject()
        );
    }
}
