package xyz.funtimes909.serverseekerv2.util;

import com.google.common.primitives.Bytes;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.List;

public class VarTypes {
    public static Pair<String, Integer> readString(byte[] in) {
        return readString(in, 0);
    }
    public static Pair<String, Integer> readString(byte[] in, int i) {
        Pair<List<Byte>, Integer> ba = readByteArray(in, i);
        return new Pair<>(new String(Bytes.toArray(ba.component1())), ba.component2());
    }

    public static Pair<List<Byte>, Integer> readByteArray(byte[] in) {
        return readByteArray(in, 0);
    }
    public static Pair<List<Byte>, Integer> readByteArray(byte[] in, int i) {
        Pair<Integer, Byte> packetSize = VarInt.decode(in, i);
        return new Pair<>(Bytes.asList(in).subList(packetSize.component2() + i, packetSize.component1() + packetSize.component2() + i), packetSize.component1() + packetSize.getSecond());
    }
}