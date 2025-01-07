package src;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;

import javax.crypto.SecretKey;

import src.StarRatingApp.Evaluacion;

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

    public static byte[] descifrarPintura(Pintura pintura, int idJuez, String privateKeyString) {
        try {
            dbManager = new DatabaseManager();

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

    public static void subirEvaluacion(int idPintura, int idJuez, String calificacion, String comentario, 
            String mensaje) {
        try {
            dbManager = new DatabaseManager();
            String llavePresidenteBase64 = dbManager.obtenerLlavePresidente();
            String juezUsuario = dbManager.obtenerUsuario(idJuez);
            PublicKey presidentePublicKey = RSA.getPublicKeyFromBase64(llavePresidenteBase64);

            BigInteger mensajeEnmascarado = generarMensajeEnmascarado(mensaje, presidentePublicKey, juezUsuario.concat("_factor_r.txt"));

            // BigInteger mensajeEnmascarado = BlindSignature.enmascararMensaje(mensaje, juezUsuario, presidentePublicKey);

            String mensajeEnmascaradoBase64 = Base64.getEncoder().encodeToString(mensajeEnmascarado.toByteArray());

            // Llamar al método de DatabaseManager para registrar la evaluación
            dbManager.registrarEvaluacion(idPintura, idJuez, calificacion, comentario, mensajeEnmascaradoBase64);
        } catch (SQLException e) {
            System.err.println("Error al registrar evaluaciones");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error al enmascarar el mensaje para firmado a ciegas");
            e.printStackTrace();
        } finally {
            if (dbManager != null) {
                try {
                    dbManager.close();
                } catch (Exception e) {
                    System.err.println("Error al cerrar conexión con base de datos");
                    e.printStackTrace();
                }
            }
        }
    }

    public static Evaluacion cargarEvaluacion(int idPintura, int idJuez) {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            return dbManager.obtenerEvaluacion(idPintura, idJuez);
        } catch (SQLException e) {
            System.err.println("Error al cargar evaluación");
            e.printStackTrace();
            return null;
        } finally {
            if (dbManager != null) {
                try {
                    dbManager.close();
                } catch (Exception e) {
                    System.err.println("Error al cerrar conexión con base de datos");
                    e.printStackTrace();
                }
            }
        }
    }

    // Generar mensaje enmascarado
    public static BigInteger generarMensajeEnmascarado(String mensaje, PublicKey clavePublica, String archivoR) throws Exception {
        // Generar factor aleatorio r
        SecureRandom random = new SecureRandom();
        BigInteger r = new BigInteger(2048, random);
        BigInteger e = BlindSignature.obtenerExponente(clavePublica);
        BigInteger n = BlindSignature.obtenerModulo(clavePublica);
        r = r.mod(n); // Asegurar que r < n

        // Guardar r en archivo codificado en Base64
        try (FileOutputStream fos = new FileOutputStream(archivoR)) {
            fos.write(Base64.getEncoder().encode(r.toByteArray()));
        }

        // Enmascarar el mensaje
        return BlindSignature.enmascararMensaje(mensaje, r, e, n);
    }

    // Desenmascarar firma recibida del presidente
    public static BigInteger desenmascararFirma(BigInteger firmaEnmascarada, PublicKey clavePublica, String archivoR) throws Exception {
        // Leer r desde el archivo
        byte[] rBytes = Base64.getDecoder().decode(Files.readAllBytes(new File(archivoR).toPath()));
        BigInteger r = new BigInteger(1, rBytes);

        // Desenmascarar la firma
        return BlindSignature.desenmascararFirma(firmaEnmascarada, r, clavePublica);
    }
}
