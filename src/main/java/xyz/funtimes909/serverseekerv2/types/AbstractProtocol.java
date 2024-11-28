package xyz.funtimes909.serverseekerv2.types;

import xyz.funtimes909.serverseekerv2.types.protocols.Error;

public interface AbstractProtocol<S> {
    static final AbstractProtocol<?> INSTANCE = new Error();
    int PROTOCOL();

    static <S> S decode(byte[] in) {
        throw new RuntimeException("Decode packet not implemented");
    };
}
