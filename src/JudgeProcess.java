package src;

import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;

import javax.crypto.SecretKey;

public class JudgeProcess {

    private static DatabaseManager dbManager;

    /**
     * Subir la clave pública del juez a la base de datos.
     *
     * @param idUsuario     ID del juez.
     * @param publicKeyFile Archivo de clave pública seleccionada.
     * @return true si la clave pública se guarda correctamente, false en caso
     *         contrario.
     */
    public static boolean subirClavePublica(int idUsuario, File publicKeyFile) {
        try {
            dbManager = new DatabaseManager();

            if (publicKeyFile == null) {
                System.out.println("No se seleccionó ningún archivo.");
                return false;
            }

            // Leer contenido del archivo como texto
            String publicKeyContent = new String(Files.readAllBytes(publicKeyFile.toPath()));

            // Guardar la clave pública en la base de datos
            boolean claveGuardada = dbManager.actualizarLlavePublica(idUsuario, publicKeyContent);
            if (claveGuardada) {
                System.out.println("Clave pública guardada exitosamente.");
            } else {
                System.out.println("Error al guardar la clave pública.");
            }

            return claveGuardada;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al subir la clave pública.");
            return false;
        } finally {
            if (dbManager != null) {
                try {
                    dbManager.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static byte[] descifrarPintura(DatabaseManager dbManager, Pintura pintura, int idJuez, String privateKeyString) {
        try {
            String wrappedKey = dbManager.obtenerLlaveEnvuelta(pintura.getIdPintura(), idJuez);
            String base64AESKey = RSA.decrypt(wrappedKey, privateKeyString);
            if (base64AESKey == null) {
                System.out.println("No se pudo decifrar la clave de AES");
                return null;
            }
            System.out.println("AES Key: " + base64AESKey);
            
            SecretKey secretKey = AESGC.decodeAESKeyFromBase64(base64AESKey);
            return AESGC.decryptBase64ToByte(secretKey, pintura.getArchivoCifrado());
        } catch (SQLException e) {
            System.err.println("Error al obtener llave envuelta");
            e.printStackTrace();
            return null; 
        } catch (Exception e) {
            System.err.println("Error al obtener archivo cifrado");
            e.printStackTrace();
            return null; 
        }
    }
}
