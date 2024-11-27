package xyz.funtimes909.serverseekerv2.types.varlen;

import java.nio.charset.StandardCharsets;

public class VarString extends AbstractVarType<String, VarString> {
    protected VarString(String value, int size) {
        super(value, size);
    }


    public static VarString decode(byte[] in, int index) {
        VarByteArray packet = VarByteArray.decode(in, index);
        return new VarString(
                new String(packet.value),
                packet.size
        );
    }

    public static byte[] encode(String in) {
        return prefixSize(in.getBytes(StandardCharsets.UTF_8));
    }
}
