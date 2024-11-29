package xyz.funtimes909.serverseekerv2.util;

import com.google.common.primitives.Bytes;
import xyz.funtimes909.serverseekerv2.types.varlen.VarInt;
import xyz.funtimes909.serverseekerv2.types.VarTypeEncoder;

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
            Class<?> objClass = obj.getClass();
            { // Attempt to encode it depending on it's class
                if (objClass.equals(Boolean.class)) { // Bool
                    arr.add((byte) ((boolean) obj ? 0x01 : 0x00));
                    continue;
                }
                if (objClass.equals(Byte.class)) { // Byte
                    arr.add((byte) obj);
                    continue;
                }
                if (objClass.equals(Short.class)) { // Short
                    arr.add((byte) (((short) obj) >> 8));
                    arr.add((byte) ((short) obj));
                    continue;
                }
            }

            { // First try to see if there is a VarType for it
                List<Byte> val = VarTypeEncoder.encode(obj);
                if (val != null) {
                    arr.addAll(val);
                    continue;
                }
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
