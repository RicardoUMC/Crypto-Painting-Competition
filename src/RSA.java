package src;
import java.security.*;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

public class RSA {

    // Método para cifrar un mensaje con la clave pública
    public static String encrypt(String message, String publicKeyPath) {
        try {
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get(publicKeyPath));
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBytes)));

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedMessage = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptedMessage);
        } catch (Exception e) {
            System.err.println("Error al cifrar: " + e.getMessage());
            return null;
        }
    }

    // Método para descifrar un mensaje con la clave privada
    public static String decrypt(String encryptedMessage, String privateKeyPath) {
        try {
            byte[] privateKeyBytes = Files.readAllBytes(Paths.get(privateKeyPath));
            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBytes)));

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedMessage = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            return new String(decryptedMessage);
        } catch (Exception e) {
            System.err.println("Error al descifrar: " + e.getMessage());
            return null;
        }
    }

}
