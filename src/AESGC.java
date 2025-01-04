package src;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.Base64;

public class AESGC {

    // Archivos de entrada y salida predefinidos
    private static final String ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // Longitud del tag de autenticación

    // Método para generar una clave AES aleatoria
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256); // Usando una clave de 256 bits
        return keyGen.generateKey();
    }

    // Método para decodificar una clave AES desde Base64
    public static SecretKey decodeAESKeyFromBase64(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, ALGORITHM);
    }

    // Método para cifrar un archivo usando AES-GCM
    public static void encryptFileToBase64(SecretKey secretKey, String inputFilePath) throws Exception {
        // Leer el archivo de entrada
        byte[] inputFileBytes = Files.readAllBytes(Paths.get(inputFilePath));

        // Generar un IV (Vector de Inicialización) aleatorio
        byte[] iv = new byte[12]; // GCM recomienda un IV de 12 bytes
        new SecureRandom().nextBytes(iv);

        // Crear el objeto Cipher para cifrado AES-GCM
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        // Cifrar los datos
        byte[] encryptedBytes = cipher.doFinal(inputFileBytes);

        // Combinar IV y datos cifrados
        byte[] ivAndEncrypted = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, ivAndEncrypted, 0, iv.length); // Copiar el IV
        System.arraycopy(encryptedBytes, 0, ivAndEncrypted, iv.length, encryptedBytes.length); // Copiar los datos
                                                                                               // cifrados

        // Convertir a Base64
        String base64Encoded = Base64.getEncoder().encodeToString(ivAndEncrypted);

        // Imprimir en consola
        System.out.println("Archivo cifrado en Base64: ");
        System.out.println(base64Encoded);
    }

    // Método para descifrar un archivo cifrado con AES-GCM
    public static void decryptBase64ToFile(SecretKey secretKey, String base64EncryptedData, String decryptedFilePath)
            throws Exception {
        // Decodificar el string Base64 a un arreglo de bytes
        byte[] encryptedFileBytes = Base64.getDecoder().decode(base64EncryptedData);

        // Obtener el IV del principio de los datos
        byte[] iv = new byte[12]; // El IV tiene 12 bytes
        System.arraycopy(encryptedFileBytes, 0, iv, 0, iv.length);

        // Los datos cifrados están después del IV
        byte[] encryptedBytes = new byte[encryptedFileBytes.length - iv.length];
        System.arraycopy(encryptedFileBytes, iv.length, encryptedBytes, 0, encryptedBytes.length);

        // Crear el objeto Cipher para descifrado AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 128 bits para el GCM TAG
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        // Descifrar los datos
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // Escribir los datos descifrados en un archivo
        try (FileOutputStream fos = new FileOutputStream(decryptedFilePath)) {
            fos.write(decryptedBytes);
        }
    }
}
