package xyz.funtimes909.serverseekerv2.types;

import xyz.funtimes909.serverseekerv2.types.protocols.Compression;
import xyz.funtimes909.serverseekerv2.types.protocols.Disconnect;
import xyz.funtimes909.serverseekerv2.types.protocols.Encryption;
import xyz.funtimes909.serverseekerv2.types.protocols.Error;
import xyz.funtimes909.serverseekerv2.types.protocols.LoginSuccess;
import xyz.funtimes909.serverseekerv2.types.varlen.VarInt;

public enum IncomingPacketType {
    ERROR(Error.INSTANCE),

    DISCONNECT(Disconnect.INSTANCE),
    ENCRYPTION(Encryption.INSTANCE),
    LOGIN_SUCCESS(LoginSuccess.INSTANCE),
    COMPRESSION(Compression.INSTANCE);


    private final int protocol;
    private final AbstractProtocol<?> instance;

    IncomingPacketType(AbstractProtocol<?> protocolInstance) {
        int protocol = -1;
        AbstractProtocol<?> instance = new Error();

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

    public static IncomingPacketType getType(int protocol) {
        for (IncomingPacketType t: IncomingPacketType.values()) {
            if (t.protocol == protocol)
                return t;
        }
        return ERROR;
    }
}
