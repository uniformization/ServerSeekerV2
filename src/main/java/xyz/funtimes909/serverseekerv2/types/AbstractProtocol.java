package xyz.funtimes909.serverseekerv2.types;

import xyz.funtimes909.serverseekerv2.types.protocols.Error;

public interface AbstractProtocol<S> {
    AbstractProtocol<?> INSTANCE = new Error();
    int PROTOCOL();
    S decode(byte[] in);
}
