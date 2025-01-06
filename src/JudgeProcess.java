package src;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

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
}
