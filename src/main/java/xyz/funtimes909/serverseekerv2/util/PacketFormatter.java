package xyz.funtimes909.serverseekerv2.util;

import com.google.common.primitives.Bytes;
import xyz.funtimes909.serverseekerv2.types.varlen.AbstractVarType;
import xyz.funtimes909.serverseekerv2.types.varlen.VarInt;
import xyz.funtimes909.serverseekerv2.types.varlen.VarTypeEncoder;

import java.util.ArrayList;
import java.util.List;

public class PacketFormatter {
    public static List<Byte> prefixSize(byte[] in) {
        List<Byte> bytes = VarInt.encode(in.length);
        bytes.addAll(Bytes.asList(in));
        return bytes;
    }
    public static List<Byte> prefixSize(List<Byte> in) {
        List<Byte> bytes = VarInt.encode(in.size());
        bytes.addAll(in);
        return bytes;
    }


    public static List<Byte> encode(Object... objs) {
        List<Byte> arr = new ArrayList<>();

        for (Object obj: objs) {
            { // First try to see if there is a VarType for it
                List<Byte> val = VarTypeEncoder.encode(obj);
                if (val != null) {
                    arr.addAll(val);
                    continue;
                }
            }

            Class<?> objClass = obj.getClass();
            if (objClass.equals(Byte.class)) {
                arr.add((byte) obj);
                continue;
            }
            if (objClass.equals(Short.class)) {
                arr.add((byte) (((short) obj) >> 8));
                arr.add((byte) ((short) obj));
                continue;
            }

            throw new RuntimeException("Unable to encode ["+ obj +"] due to its class ["+ objClass +"] not being a recognised type.");
        }

        return arr;
    }
    public static List<Byte> encodePacket(int protocol, Object... objs) {
        // Prefix with the protocol
        List<Byte> arr = VarInt.encode(protocol);
        // Then the rest of the objects
        arr.addAll(encode(objs));
        // And finally prefix with the length
        arr.addAll(0, VarInt.encode(arr.size()));

        return arr;
    }
}
