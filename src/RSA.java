package src;

import java.security.*;
import java.util.Base64;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.crypto.Cipher;

import java.security.spec.X509EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class RSA {

    /**
     * Generar un par de claves RSA y guardarlas en archivos.
     *
     * @param privateKeyFile Ruta del archivo donde se guardará la clave privada.
     * @param publicKeyFile  Ruta del archivo donde se guardará la clave pública.
     * @return true si se generaron y guardaron correctamente, false en caso
     *         contrario.
     */
    public static boolean generateAndSaveKeyPair(String privateKeyFile, String publicKeyFile) {
        try {
            // Generar el par de claves
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Longitud de clave de 2048 bits
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Obtener las claves pública y privada
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // Codificar las claves en Base64
            String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            // Guardar las claves en archivos
            saveToFile(privateKeyFile, privateKeyBase64);
            saveToFile(publicKeyFile, publicKeyBase64);

            System.out.println("Claves generadas y guardadas correctamente.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al generar las claves RSA: " + e.getMessage());
            return false;
        }
    }

    // Método para cifrar un mensaje con una clave pública en formato Base64
    public static String encrypt(String message, String publicKeyBase64) {
        try {
            // Recuperar la clave pública desde el Base64
            PublicKey publicKey = getPublicKeyFromBase64(publicKeyBase64);
            if (publicKey == null) {
                throw new IllegalArgumentException("La clave pública proporcionada no es válida.");
            }

            // Configurar el cifrado con RSA
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            // Cifrar el mensaje
            byte[] encryptedMessage = cipher.doFinal(message.getBytes());

            // Codificar el mensaje cifrado en Base64
            return Base64.getEncoder().encodeToString(encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cifrar el mensaje: " + e.getMessage());
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

    /**
     * Recuperar una clave pública desde una cadena codificada en Base64.
     *
     * @param publicKeyBase64 Clave pública codificada en Base64.
     * @return Objeto PublicKey si se convierte correctamente, null en caso
     *         contrario.
     */
    public static PublicKey getPublicKeyFromBase64(String publicKeyBase64) {
        try {
            // Decodificar la clave pública desde Base64 a bytes
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);

            // Crear un objeto PublicKey a partir de los bytes
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (IllegalArgumentException e) {
            System.err.println("Error en la codificación Base64: " + e.getMessage());
            throw new RuntimeException("Clave Base64 no válida.", e);
        } catch (InvalidKeySpecException e) {
            System.err.println("Formato de clave no válido: " + e.getMessage());
            throw new RuntimeException("Clave pública en formato incorrecto.", e);
        } catch (Exception e) {
            System.err.println("Error general al decodificar clave: " + e.getMessage());
            throw new RuntimeException("Error desconocido al decodificar clave.", e);
        }
    }

    /**
     * Guardar contenido en un archivo.
     *
     * @param filePath Ruta del archivo.
     * @param content  Contenido a guardar.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    private static void saveToFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
        }
    }
}
