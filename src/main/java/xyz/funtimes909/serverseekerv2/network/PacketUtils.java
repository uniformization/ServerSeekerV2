package xyz.funtimes909.serverseekerv2.network;

import kotlin.Pair;
import xyz.funtimes909.serverseekerv2.types.varlen.VarInt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketUtils {
    public static byte[] readStream(InputStream io) throws IOException {
        int size = VarInt.decode(io);
        return io.readNBytes(size);
    }

    /**
     * Decrypts an encrypted input stream
     * @param cipher The Cipher object already initialized with key & algorithm (used to decode)
     * @return List of packets. While normal servers *should* only ever return one packet per request,
     * it is possible to send back multiple. So this returns a list of the byte arrays which contains each packet.
     */
    public static List<byte[]> readEncryptedStream(InputStream io, Cipher cipher)
            throws IOException, IllegalBlockSizeException, BadPaddingException
    {
        List<byte[]> result = new ArrayList<>();
        byte[] encryptedPacket = io.readAllBytes();
        byte[] packet = cipher.doFinal(encryptedPacket);

        int packetStartIndex = 0;

        while (packetStartIndex < packet.length) {
            VarInt packetSize = VarInt.decode(packet, packetStartIndex);
            packetStartIndex += packetSize.get() + packetSize.getSize();
            result.add(Arrays.copyOfRange(
                    packet,
                    packetStartIndex - packetSize.get() - 1,
                    packetStartIndex - 1
            ));
        }

        return result;
    }
}