package xyz.funtimes909.serverseekerv2.types.varlen;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class VarUUID extends AbstractVarType<UUID, VarUUID> {
    public VarUUID() {
        super(null, 16);
    }

    @Override
    public Class<?> getType() {
        return UUID.class;
    }


    public static UUID decode(byte[] in, int index) {
        ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(in, index, 16));
        return new UUID(bb.getLong(), bb.getLong());
    }

    public static List<Byte> encode(UUID in) {
        ArrayList<Byte> ba = new ArrayList<>(Bytes.asList(Longs.toByteArray(in.getMostSignificantBits())));
        ba.addAll(Bytes.asList(Longs.toByteArray(in.getLeastSignificantBits())));
        return ba;
    }

    @Override
    public List<Byte> encodeSelf() {
        return encode(this.value);
    }
    @Override
    public List<Byte> encodeValue(Object in) {
        return encode((UUID) in);
    }
}
