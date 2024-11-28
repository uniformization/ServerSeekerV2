package xyz.funtimes909.serverseekerv2.types.protocols;

import xyz.funtimes909.serverseekerv2.types.AbstractProtocol;
import xyz.funtimes909.serverseekerv2.types.varlen.VarInt;

public class Compression implements AbstractProtocol<Compression> {
    public static final AbstractProtocol<?> INSTANCE = new Compression();
    @Override
    public int PROTOCOL() { return 3; }

    public int threshold = -1;

    public Compression() {}

    private Compression(int threshold) {
        this.threshold = threshold;
    }


    public static Compression decode(byte[] in) {
        return new Compression(
                VarInt.decode(in, 1).get()
        );
    }
}
