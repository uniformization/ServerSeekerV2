package xyz.funtimes909.serverseekerv2.types.protocols.login;

import xyz.funtimes909.serverseekerv2.types.protocols.AbstractProtocol;
import xyz.funtimes909.serverseekerv2.types.protocols.Error;
import xyz.funtimes909.serverseekerv2.types.protocols.login.incoming.*;

public enum LoginPacketType {
    ERROR(Error.INSTANCE),

    DISCONNECT(Disconnect.INSTANCE),
    ENCRYPTION(Encryption.INSTANCE),
    LOGIN_SUCCESS(LoginSuccess.INSTANCE),
    COMPRESSION(Compression.INSTANCE);


    private final int protocol;
    private final AbstractProtocol<?> instance;

    LoginPacketType(AbstractProtocol<?> protocolInstance) {
        int protocol = -1;
        AbstractProtocol<?> instance = AbstractProtocol.INSTANCE;

        try {
            instance = protocolInstance;
            protocol = protocolInstance.PROTOCOL();
        } catch (Exception ignored) { }

        this.protocol = protocol;
        this.instance = instance;
    }

    public int getProtocol() {
        return protocol;
    }
    public AbstractProtocol<?> getInstance() {
        return instance;
    }

    public static LoginPacketType getType(int protocol) {
        for (LoginPacketType t: LoginPacketType.values()) {
            if (t.protocol == protocol)
                return t;
        }
        return ERROR;
    }
}
