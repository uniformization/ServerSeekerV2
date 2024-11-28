package xyz.funtimes909.serverseekerv2.types.protocols;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.funtimes909.serverseekerv2.types.AbstractProtocol;
import xyz.funtimes909.serverseekerv2.types.varlen.VarString;

public class Disconnect implements AbstractProtocol<Disconnect> {
    public static final AbstractProtocol<?> INSTANCE = new Disconnect();
    @Override
    public int PROTOCOL() { return 0; }

    public JsonObject reason = new JsonObject();

    public Disconnect() {}
    private Disconnect(JsonObject reason) {
        this.reason = reason;
    }


    public static Disconnect decode(byte[] in) {
        return new Disconnect(
                JsonParser.parseString(VarString.decode(in, 1).get()).getAsJsonObject()
        );
    }
}
