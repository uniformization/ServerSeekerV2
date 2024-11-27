package xyz.funtimes909.serverseekerv2.types.protocols;

import xyz.funtimes909.serverseekerv2.types.AbstractProtocol;

public class LoginSuccess implements AbstractProtocol<LoginSuccess> {
    @Override
    public int PROTOCOL() {
        return 2;
    }

    @Override
    public LoginSuccess decode(byte[] in) {
        return null;
    }
}
