package xyz.funtimes909.serverseekerv2.types.protocols;

import xyz.funtimes909.serverseekerv2.types.AbstractProtocol;

/**
 * A placeholder protocol
 */
public class Error implements AbstractProtocol<Error> {
    @Override
    public int PROTOCOL() {
        return -1;
    }

    @Override
    public Error decode(byte[] in) {
        return new Error();
    }
}
