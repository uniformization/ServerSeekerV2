package xyz.funtimes909.serverseekerv2.types.protocols.login.incoming;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.funtimes909.serverseekerv2.types.protocols.AbstractProtocol;
import xyz.funtimes909.serverseekerv2.types.varlen.VarString;

public class Disconnect implements AbstractProtocol<Disconnect> {
    public static final AbstractProtocol<?> INSTANCE = new Disconnect();
    @Override
    public final int PROTOCOL() { return 0; }

    public JsonObject reason = new JsonObject();

    public Disconnect() {}
    private Disconnect(JsonObject reason) {
        this.reason = reason;
    }


    public static Disconnect decode(byte[] in) {
        String reason = VarString.decode(in, 1).get();
        try {
            return new Disconnect(
                    JsonParser.parseString(reason).getAsJsonObject()
            );
        } catch (Exception ignored) {
            JsonObject json = new JsonObject();
            // TODO: Could doing it this way cause issues down the line?
            // FIXME: Find a better way to do it
            json.addProperty("text", reason);
            return new Disconnect(
                    json
            );
        }
    }
}
