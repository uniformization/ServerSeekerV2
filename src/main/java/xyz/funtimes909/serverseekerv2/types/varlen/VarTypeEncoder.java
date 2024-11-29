package xyz.funtimes909.serverseekerv2.types.varlen;

import java.util.List;

public class VarTypeEncoder {
    public static List<AbstractVarType<?, ?>> varTypes = List.of(
            new VarInt(),
            new VarByteArray(),
            new VarString(),
            new VarUUID()
    );

    public static List<Byte> encode(Object obj) {
        for (AbstractVarType<?, ?> type: varTypes) {
            if (obj.getClass().equals(type.getType()))
                return type.encodeValue(obj);
        }
        return null;
    }
}
