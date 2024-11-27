package xyz.funtimes909.serverseekerv2.types.varlen;

import java.util.Arrays;

public class VarByteArray extends AbstractVarType<byte[], VarByteArray> {
    protected VarByteArray(byte[] value, int size) {
        super(value, size);
    }


    public static VarByteArray decode(byte[] in, int index) {
        VarInt packetSize = VarInt.decode(in, index);
        return new VarByteArray(
                Arrays.copyOfRange(in, packetSize.size + index, packetSize.value + packetSize.size + index),
                packetSize.value + packetSize.size
        );
    }

    public static byte[] encode(byte[] in) {
        return prefixSize(in);
    }
}
