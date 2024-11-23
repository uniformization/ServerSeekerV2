package xyz.funtimes909.serverseekerv2.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class VarInt {
    public static int decode(InputStream is) throws IOException {
        int val = 0;
        for (int i = 0; i < (7 /* size */ * 5 /* max size */); i += 7) {
            byte byte_read = (byte) is.read();
            // NOTE: 0x7f is 7 ones
            val |= (byte_read & 0b0111_1111) << i;

            if ((byte_read >> 7) == 0)
                break;
        }
        return val;
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