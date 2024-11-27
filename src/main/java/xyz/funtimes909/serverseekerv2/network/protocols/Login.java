package xyz.funtimes909.serverseekerv2.network.protocols;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kotlin.Pair;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import xyz.funtimes909.serverseekerv2.network.Connect;
import xyz.funtimes909.serverseekerv2.network.PacketUtils;
import xyz.funtimes909.serverseekerv2.types.IncomingPacketType;
import xyz.funtimes909.serverseekerv2.types.protocols.Encryption;
import xyz.funtimes909.serverseekerv2.types.varlen.VarByteArray;
import xyz.funtimes909.serverseekerv2.types.varlen.VarInt;
import xyz.funtimes909.serverseekerv2.types.varlen.VarString;
import xyz.funtimes909.serverseekerv2.util.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Login {
    // The default username & uuid to attempt to login to servers if none is given
    public static final String username = "Herobrine";
    public static final UUID uuid = UUID.fromString("f84c6a79-0a4e-45e0-879b-cd49ebd4c4e2");

    public static final List<Byte> REQUEST = getLoginStart(username, uuid);

    // NOTE: This is only for testing. Once deployed, the BC provider will be added in the main function
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());
    }


    public static String login(String ip, short port) {
        int protocol = 0;

        // First ping the server to get the protocol version
        try (Socket so = Connect.connect(ip, port)) {
            String status = Handshake.ping(so);
            JsonObject pingJson = JsonParser.parseString(status).getAsJsonObject();
            protocol = pingJson.get("version").getAsJsonObject().get("protocol").getAsInt();
        } catch (Exception ignored) {}

//        System.out.println("Protocol version: " + protocol);

        // Then try to login
        try (Socket so = Connect.connect(ip, port)) {
            return login(so, protocol);
        } catch (Exception ignored) {}

        return null;
    }


    public static String login(Socket so, int protocol) {
        return login(so, protocol, REQUEST, "");
    }
    public static String login(Socket so, int protocol, String username, UUID uuid, String accessToken) {
        return login(so, protocol, getLoginStart(username, uuid), accessToken);
    }
    public static String login(Socket so, int protocol, List<Byte> loginRequest, String accessToken) {
        try (
                OutputStream out = so.getOutputStream();
                InputStream in = so.getInputStream();
                ) {
            // The login request starts off with the Handshake and Login Start
            List<Byte> request = Handshake.getHandshake(protocol, "", (short) 0, (byte) 2);
            request.addAll(loginRequest);

            // Write the things to the server
            out.write(Bytes.toArray(request));

            // And get its response
            byte[] packet = PacketUtils.readStream(in);

            Encryption encryptionPacket = (Encryption) IncomingPacketType.ENCRYPTION.getInstance().decode(packet);


            byte[] sharedSecret = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(sharedSecret);
            SecretKeySpec sharedSecretKey = new SecretKeySpec(sharedSecret, "AES");
            IvParameterSpec sharedSecretIv = new IvParameterSpec(sharedSecret);
//            System.out.println("Secret: " + Bytes.asList(sharedSecret));


            Cipher decryptCipher = Cipher.getInstance("AES/CFB8/NoPadding", BouncyCastleProvider.PROVIDER_NAME);
            decryptCipher.init(Cipher.DECRYPT_MODE, sharedSecretKey, sharedSecretIv);
            Cipher encryptCipher = Cipher.getInstance("AES/CFB8/NoPadding", BouncyCastleProvider.PROVIDER_NAME);
            encryptCipher.init(Cipher.ENCRYPT_MODE, sharedSecretKey, sharedSecretIv);


            Cipher serverEncryptCipher = Cipher.getInstance("RSA/None/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
            serverEncryptCipher.init(Cipher.ENCRYPT_MODE, encryptionPacket.publicKey);


            ArrayList<Byte> encryptionResponse = new ArrayList<>();
            // Protocol
            encryptionResponse.add((byte) 1);

            // Shared secret
            byte[] encryptedSharedSecret = serverEncryptCipher.doFinal(sharedSecret);

            encryptionResponse.addAll(VarInt.encode(encryptedSharedSecret.length));
            encryptionResponse.addAll(Bytes.asList(encryptedSharedSecret));

            // Token
            byte[] encryptedVerifyToken = serverEncryptCipher.doFinal(encryptionPacket.verifyToken);

            encryptionResponse.addAll(VarInt.encode(encryptedVerifyToken.length));
            encryptionResponse.addAll(Bytes.asList(encryptedVerifyToken));

            // Prefix with size
            encryptionResponse.addAll(0, VarInt.encode(encryptionResponse.size()));

            out.write(Bytes.toArray(encryptionResponse));



            byte[] serverReturn = PacketUtils.readEncryptedStream(in, decryptCipher).getFirst();
            System.out.println(Bytes.asList(serverReturn));
            System.out.println(new String(serverReturn));
//            byte[] buffer = new byte[1024];
//            in.read(buffer);
//            System.out.println(Bytes.asList(buffer));


            return "no errors";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Byte> getLoginStart(String name, UUID id) {
        List<Byte> arr = new ArrayList<>(List.of(
                // Packet ID
                (byte) 0
        ));

        // Username
        if (name.isEmpty())
            arr.add((byte) 0);
        else {
            arr.addAll(VarInt.encode(name.length()));
            arr.addAll(Bytes.asList(name.getBytes(StandardCharsets.UTF_8)));
        }

        // UUID
        arr.addAll(Bytes.asList(Longs.toByteArray(id.getMostSignificantBits())));
        arr.addAll(Bytes.asList(Longs.toByteArray(id.getLeastSignificantBits())));

        // Finally start it with the size
        arr.addAll(0, VarInt.encode(arr.size()));

        return arr;
    }

    public static void main(String[] args) {
        System.out.println(IncomingPacketType.COMPRESSION.getProtocol());
        System.out.println(login("127.0.0.1", (short) 25565));
    }
}
