package xyz.funtimes909.serverseekerv2.util;

import kotlin.Pair;

import java.util.ArrayList;

public class VarInt {
    /**
     * Attempts to decode the varint at the start of a byte array
     * @return A Pair containing the value and the n bytes needed to get it in the byte array
     */
    public static Pair<Integer, Byte> decode(byte[] in) {
        return decode(in, 0);
    }
    /**
     * Attempts to decode the varint at the start of a byte array
     * @param i Starting index to the byte array
     * @return A Pair containing the value and the n bytes needed to get it in the byte array
     */
    public static Pair<Integer, Byte> decode(byte[] in, int i) {
        int val = 0;
        byte count = 0;

        for (; i < in.length; i++) {
            byte b = in[i];
            val |= (b & 0b0111_1111) << count;
            if (((b >> 7) != -1) || (count > (7 /* size */ * 5 /* max size */)))
                break;
            count += 7;
        }

        return new Pair<>(val, (byte) ((count/7) + 1));
    }

    public static ArrayList<Byte> encode(int v) {
        ArrayList<Byte> res = new ArrayList<>();
        do {
            res.add((byte) (((v >> 7 == 0)? 0: 0b1000_0000) | (v & 0b0111_1111)));
            v >>= 7;
        } while (v != 0);
        return res;
    }
}