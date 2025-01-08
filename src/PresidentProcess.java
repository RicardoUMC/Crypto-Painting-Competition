package src;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

public class PresidentProcess {
    private static DatabaseManager dbManager;

    public static String obtenerUsuarioPresidente(int idPresidente) {
        try {
            dbManager = new DatabaseManager();
            return dbManager.obtenerUsuario(idPresidente);
        } catch (SQLException e) {
            System.err.println("Error al obtener el usuario del presidente");
            e.printStackTrace();
            return null;
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

    // Firmar el mensaje enmascarado
    public static boolean firmarMensajeEnmascarado(File privateKeyFile) {
        if (privateKeyFile == null) {
            System.out.println("No se seleccionó ningún archivo.");
            return false;
        }
        
        try {
            String publicKeyBase64Encoded = new String(Files.readAllBytes(privateKeyFile.toPath()));
            byte[] privateKeyBytes = Base64.getDecoder().decode(publicKeyBase64Encoded);
            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

            if (privateKey == null) {
                throw new IllegalArgumentException("La clave privada proporcionada no es válida.");
            }
    
            dbManager = new DatabaseManager();
            List<MensajeEnmascarado> mensajes = dbManager.obtenerMensajesEnmascarados();
    
            for (MensajeEnmascarado mensaje : mensajes) {
                String firmaBase64Encoded = BlindSignature.firmarMensaje(mensaje.getMensajeEnmascarado(), privateKey);
                dbManager.guardarFirmaCiega(mensaje.getIdEvaluacion(), firmaBase64Encoded);
            }
            
            System.out.println("Firmas ciegas generadas exitosamente.");
            return true;
        } catch (IOException e) {
            System.err.println("Error en la lectura del archivo");
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            System.err.println("Error en la generación de la llave");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error en el algoritmo para generar instancia");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error con la comunicación de la base de datos");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error en la generación de la firma ciega");
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
        return false;
    }
}
