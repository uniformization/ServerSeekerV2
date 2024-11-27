package xyz.funtimes909.serverseekerv2.types.protocols;

import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.bike.BCBIKEPublicKey;
import xyz.funtimes909.serverseekerv2.types.AbstractProtocol;
import xyz.funtimes909.serverseekerv2.types.varlen.VarByteArray;
import xyz.funtimes909.serverseekerv2.types.varlen.VarString;

import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

public class Encryption implements AbstractProtocol<Encryption> {
    public String serverID = "";
    public BCRSAPublicKey publicKey = null;
    public byte[] verifyToken = new byte[0];
    public boolean shouldAuth = false;

    public Encryption() {}
    private Encryption(String serverID, BCRSAPublicKey publicKey, byte[] verifyToken, boolean shouldAuth) {
        this.serverID = serverID;
        this.publicKey = publicKey;
        this.verifyToken = verifyToken;
        this.shouldAuth = shouldAuth;
    }

    @Override
    public int PROTOCOL() {
        return 1;
    }

    @Override
    public Encryption decode(byte[] in) {
        try {
            int pointer = 1;

            VarString serverID = VarString.decode(in, pointer);
            pointer += serverID.getSize();

            VarByteArray publicKey = VarByteArray.decode(in, pointer);
            pointer += publicKey.getSize();

            VarByteArray verifyToken = VarByteArray.decode(in, pointer);
            pointer += verifyToken.getSize();

            KeyFactory keyFactory = KeyFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey.get());
            BCRSAPublicKey serverPublicKey = (BCRSAPublicKey) keyFactory.generatePublic(keySpec);

            return new Encryption(
                    serverID.get(),
                    serverPublicKey,
                    verifyToken.get(),
                    in[pointer] == 1
            );
        } catch (Exception ignored) { }
        return new Encryption();
    }
}
