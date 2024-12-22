package xyz.funtimes909.serverseekerv2.types.protocols;

import java.util.List;

public interface AbstractProtocol<S> {
    static final AbstractProtocol<?> INSTANCE = new Error();
    int PROTOCOL();


    static <S> S decode(byte[] in) {
        throw new RuntimeException("Decode packet not implemented");
    };
    default List<Byte> encode() {
        throw new RuntimeException("Encode packet not implemented");
    };
}
