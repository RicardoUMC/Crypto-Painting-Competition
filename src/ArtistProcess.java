package src;

import java.io.File;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.SecretKey;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ArtistProcess {

    private static DatabaseManager dbManager; // Instancia para conexión con la base de datos

    /**
     * Generar y guardar las llaves ECDSA en archivos.
     *
     * @param privateKeyFile Archivo para guardar la clave privada.
     * @param publicKeyFile  Archivo para guardar la clave pública.
     * @return true si se generan y guardan correctamente, false en caso contrario.
     */
    protected static boolean generarYGuardarLlaves(String privateKeyFile, String publicKeyFile) {
        try {
            ECDSA.generateAndSaveKeyPair(privateKeyFile, publicKeyFile);
            System.out.println("Llaves generadas y guardadas correctamente.");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Ocurrió un error al generar las llaves.");
            return false;
        }
    }

    /**
     * Firmar un mensaje utilizando la clave privada ECDSA seleccionada por el
     * usuario.
     *
     * @param idUsuario ID del usuario para registrar el acuerdo.
     * @param stage     Ventana actual para mostrar el selector de archivos.
     * @return true si se firma y registra el acuerdo correctamente, false en caso
     *         contrario.
     */
    protected static boolean firmar(int idUsuario, Stage stage) {
        try {
            dbManager = new DatabaseManager(); // Conectar a la base de datos

            String agreementFile = "Acuerdo de Confidencialidad.pdf";
            String signatureFile = "signature.txt";

            // Seleccionar archivo de clave privada
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar archivo de clave privada");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile == null) {
                System.out.println("No se seleccionó ningún archivo de clave privada.");
                return false;
            }

            String privateKeyFile = selectedFile.getAbsolutePath();

            // Cargar la clave privada
            PrivateKey privateKey = ECDSA.loadPrivateKeyFromBase64(privateKeyFile);

            // Inicializar ECDSA para firmar
            Signature ecdsa = Signature.getInstance("SHA256withECDSA");
            byte[] messageBytes = ECDSA.readFileBytes(agreementFile);
            ecdsa.initSign(privateKey);
            ecdsa.update(messageBytes);

            // Generar la firma
            byte[] signature = ecdsa.sign();

            // Obtener r y s de la firma
            BigInteger[] rsValues = ECDSA.decodeRSFromDER(signature);
            BigInteger r = rsValues[0];
            BigInteger s = rsValues[1];

            // Guardar la firma en archivo
            ECDSA.saveSignatureToFile(r, s, signatureFile);

            // Codificar firma en Base64 para almacenarla en la base de datos
            String firmaBase64 = Base64.getEncoder().encodeToString(signature);

            // Registrar el acuerdo en la base de datos
            String agreementBase64 = Base64.getEncoder().encodeToString(messageBytes);
            boolean result = dbManager.registrarAcuerdo(idUsuario, agreementBase64, firmaBase64);

            if (dbManager != null) {
                dbManager.close();
            }

            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Ocurrió un error al firmar el mensaje.");
            return false;
        }
    }

    protected static boolean enviarPintura(int idUsuario) {
        try {
            String INPUT_FILE = "descarga.jpg"; // Archivo de imagen de entrada
            SecretKey originalKey = AESGC.generateAESKey();
            String base64Key = Base64.getEncoder().encodeToString(originalKey.getEncoded());
            SecretKey secretKey = AESGC.decodeAESKeyFromBase64(base64Key);
            AESGC.encryptFileToBase64(secretKey, INPUT_FILE);

            String PUBLIC_KEY_FILE = "publicKeyjuez3.txt";
            System.out.println("Llave cifrada con RSA:");
            System.out.println(RSA.encrypt(base64Key, PUBLIC_KEY_FILE));

            return true;
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        }
    }

    /**
     * Verificar si el acuerdo de confidencialidad ya fue firmado.
     */
    protected static boolean verificarAcuerdoFirmado(int idUsuario) {
        boolean result = false;
        try {
            dbManager = new DatabaseManager();

            if (dbManager != null) {
                result = dbManager.verificarAcuerdo(idUsuario);
            
                dbManager.close();
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }
}
