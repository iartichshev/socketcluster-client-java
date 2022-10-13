
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;


public class EncryptHelper {
    private final static String publicKey =
            "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA23nOll7PB0h2ySZacJxU\n" +
            "VV3xpHtJUUGB7ufWxgvTP//xm4dBdyFMexM4EfR/GruX/VmhD5Hwq7+0JLBrO90M\n" +
            "mthKFXvogmQHBqeLdrYXN3qcDeax0hnFE3b2KRrBzjsXVWAO+35boCHSMBS/llUd\n" +
            "wlr+tLLCqqaf+6jk3z2DzEUijBwWnhiBOpZTA0Ocp/3aveJowblnOq6lc9afpIej\n" +
            "d2oJ3QPhvoqfwI8LAJnWEBuQhB5kPIBhGMH+UM66RPP3zd1NNTMEV62PPKs6deAv\n" +
            "L2ESDsKQYZ1gEtoIy0odHYQgvSATQti5vrbf/uh5FeN1i1JZitBSeCHwazrK1p3R\n" +
            "pQIDAQAB\n" +
            "-----END PUBLIC KEY-----";

    public static String encrypt(String pData) throws InvalidKeyException, Exception {
        return encryptRSA(pData, getPublicKeyFromString(publicKey));
    }

    public static String encrypt_test(String pData) throws InvalidKeyException, Exception {
        return encryptRSA(pData, getPublicKeyFromFile("/app/pr/js_keys/pub.pem"));
    }

    public static String encryptRSA(String pData, PublicKey pPublicKey) throws InvalidKeyException, Exception {
        Cipher vCipher = Cipher.getInstance("RSA"); // do not use /ECB/NoPadding !
        vCipher.init(Cipher.ENCRYPT_MODE, pPublicKey);
        byte[] vEncryptedData = vCipher.doFinal(pData.getBytes("UTF8"));
        return Base64.getEncoder().encodeToString(vEncryptedData);
    }

    public static PublicKey getPublicKeyFromString(String pKey) throws Exception {
        String vKey = pKey
                .replace("\n", "")
                .replace("\r", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .trim();
        byte[] vKeyBytes = Base64.getDecoder().decode(vKey);
        X509EncodedKeySpec vX509EncodedKeySpec =  new X509EncodedKeySpec(vKeyBytes);
        KeyFactory vKeyFactory = KeyFactory.getInstance("RSA");
        return vKeyFactory.generatePublic(vX509EncodedKeySpec);
    }

    public static PublicKey getPublicKeyFromFile(String pFilename) throws Exception {
        File vFile = new File(pFilename);
        FileInputStream vFileInputStream = new FileInputStream(vFile);
        DataInputStream vDataInputStream = new DataInputStream(vFileInputStream);
        byte[] vKeyBytes = new byte[(int) vFile.length()];
        vDataInputStream.readFully(vKeyBytes);
        vDataInputStream.close();
        return getPublicKeyFromString(new String(vKeyBytes));
    }


}
