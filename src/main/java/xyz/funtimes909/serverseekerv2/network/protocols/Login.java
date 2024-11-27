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
    public static final String username = "Herobrine";
    public static final UUID uuid = UUID.fromString("f84c6a79-0a4e-45e0-879b-cd49ebd4c4e2");

    public static final List<Byte> REQUEST = getLoginStart(username, uuid);


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
        try (
                OutputStream out = so.getOutputStream();
                InputStream in = so.getInputStream();
                ) {
            // The login request starts off with the Handshake and Login Start
            List<Byte> request = Handshake.getHandshake(protocol, "", (short) 0, (byte) 2);
            request.addAll(REQUEST);

            // Write the things to the server
            out.write(Bytes.toArray(request));

            // And get its response
            byte[] packetBa = PacketUtils.readStream(in);

            int pointer = 1;

            Pair<String, Integer> serverID = VarTypes.readString(packetBa, pointer);
            pointer += serverID.component2();

            Pair<List<Byte>, Integer> publicKey = VarTypes.readByteArray(packetBa, pointer);
            pointer += publicKey.component2();

            Pair<List<Byte>, Integer> verifyToken = VarTypes.readByteArray(packetBa, pointer);
            pointer += verifyToken.component2();


//            System.out.println("Server ID   : " + serverID.component1());
//            System.out.println("Public key  : " + publicKey.component1());
//            System.out.println("Verify token: " + verifyToken.component1());
//            System.out.println("Should AUTH : " + packetBa[pointer]);



            byte[] sharedSecret = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(sharedSecret);
            SecretKeySpec sharedSecretKey = new SecretKeySpec(sharedSecret, "AES");
//            System.out.println("Secret: " + Bytes.asList(sharedSecret));




            // TODO: make this only run once
            Security.addProvider(new BouncyCastleProvider());

            KeyFactory keyFactory = KeyFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Bytes.toArray(publicKey.component1()));
            BCRSAPublicKey serverPublicKey = (BCRSAPublicKey) keyFactory.generatePublic(keySpec);

//            System.out.println(serverPublicKey);
//            System.out.println(Bytes.asList(serverPublicKey.getModulus().toByteArray()));

            String encryptionAlgorithm = "RSA/None/PKCS1Padding";
            byte[] encryptedSharedSecret = encrypt(encryptionAlgorithm, sharedSecret, serverPublicKey);
            byte[] encryptedVerifyToken = encrypt(encryptionAlgorithm, Bytes.toArray(verifyToken.component1()), serverPublicKey);

            // "AES/CFB8/PKCS5Padding"


            ArrayList<Byte> encryptionResponse = new ArrayList<>();
            // Protocol
            encryptionResponse.add((byte) 1);
            // Shared secret
            encryptionResponse.addAll(VarInt.encode(encryptedSharedSecret.length));
            encryptionResponse.addAll(Bytes.asList(encryptedSharedSecret));
            // Token
            encryptionResponse.addAll(VarInt.encode(encryptedVerifyToken.length));
            encryptionResponse.addAll(Bytes.asList(encryptedVerifyToken));

            // Prefix with size
            encryptionResponse.addAll(0, VarInt.encode(encryptionResponse.size()));

            System.out.println("Encryption Response: " + encryptionResponse);
            System.out.println("Encryption Response Size: " + encryptionResponse.size());
            out.write(Bytes.toArray(encryptionResponse));



            byte[] serverReturn = in.readAllBytes();
            System.out.println(Bytes.asList(serverReturn));
            System.out.println(new String(serverReturn));
            byte[] decryptedServerReturn = decrypt("AES/CFB8/NoPadding", serverReturn, sharedSecretKey, new IvParameterSpec(sharedSecret));
            System.out.println(Bytes.asList(decryptedServerReturn));
            System.out.println(new String(decryptedServerReturn));
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
        System.out.println(login("127.0.0.1", (short) 25565));
    }



    public static byte[] encrypt(String algorithm, byte[] input, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, NoSuchProviderException {


        Cipher cipher = Cipher.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(input);
    }
    public static byte[] decrypt(String algorithm, byte[] cipherText, Key key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, NoSuchProviderException {

        Cipher cipher = Cipher.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(cipherText);
    }
}
