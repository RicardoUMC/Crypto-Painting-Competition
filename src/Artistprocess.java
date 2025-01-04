package src;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.SecretKey;

public class Artistprocess {
    protected static void firmar() {
        try {
            // Llamar a la función generateAndSaveKeyPair de la clase ECDSA los string de
            // los archivos ya no se usarian una vez que se guarde en la base de datos
            String privateKeyFile = "privateKeyECDSA_usuario.txt";
            String publicKeyFile = "publicKeyECDSA_usuario.txt";
            String MESSAGE_FILE = "message.txt";
            String SIGNATURE_FILE = "signature.txt";
            BigInteger r, s;
            BigInteger[] rsValues;
            ECDSA.generateAndSaveKeyPair(privateKeyFile, publicKeyFile);
            System.out.println("Llaves generadas y guardadas correctamente.");

            PrivateKey privateKey = ECDSA.loadPrivateKeyFromBase64(privateKeyFile);
            Signature ecdsa = Signature.getInstance("SHA256withECDSA");
            byte[] messageBytes = ECDSA.readFileBytes(MESSAGE_FILE);
            ecdsa.initSign(privateKey);
            ecdsa.update(messageBytes);

            byte[] signature = ecdsa.sign();

            // APARTIR DE AQUI ES COMO SE HIZO EN LA PRACTICA PERO CREO QUE PARA EL PROYECTO
            // NO ES NECESARIO HACERLO
            // oBTIENE R Y S DE LA FIRMA
            rsValues = ECDSA.decodeRSFromDER(signature);
            r = rsValues[0];
            s = rsValues[1];
            ECDSA.saveSignatureToFile(r, s, SIGNATURE_FILE);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Ocurrió un error al generar las llaves.");
        }
    }

    protected static void enviarPintura() {
        try {
            String INPUT_FILE = "descarga.jpg"; // Archivo de imagen de entrada
            SecretKey originalKey;
            originalKey = AESGC.generateAESKey();
            String base64Key = Base64.getEncoder().encodeToString(originalKey.getEncoded());
            SecretKey secretKey = AESGC.decodeAESKeyFromBase64(base64Key);
            AESGC.encryptFileToBase64(secretKey, INPUT_FILE);
            // AQUI VA A OBTENER LAS CLAVES DE UN ARCHIVO PERO MAS ADELANTE SE QUITA
            // ESA FUNCION PORQUE SE OBTIENE DE LA BASE DATOS
            String PUBLIC_KEY_FILE = "publicKeyjuez3.txt";
            System.out.println("llave cifradad con rsa");
            System.out.println(RSA.encrypt(base64Key, PUBLIC_KEY_FILE));

        } catch (Exception e1) {

            e1.printStackTrace();
        }
    }

}