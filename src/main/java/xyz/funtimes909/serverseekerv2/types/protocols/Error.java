package xyz.funtimes909.serverseekerv2.types.protocols;

import xyz.funtimes909.serverseekerv2.types.AbstractProtocol;

/**
 * A dummy protocol
 */
public class Error implements AbstractProtocol<Error> {
    @Override
    public int PROTOCOL() { return -1; }
}
