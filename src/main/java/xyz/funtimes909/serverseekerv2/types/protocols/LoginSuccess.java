package xyz.funtimes909.serverseekerv2.types.protocols;

import xyz.funtimes909.serverseekerv2.types.AbstractProtocol;

public class LoginSuccess implements AbstractProtocol<LoginSuccess> {
    public static final AbstractProtocol<?> INSTANCE = new LoginSuccess();
    @Override
    public int PROTOCOL() { return 2; }


    public static LoginSuccess decode(byte[] in) {
        return null;
    }
}
