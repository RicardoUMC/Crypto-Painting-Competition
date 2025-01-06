package src;

import java.io.File;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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

    protected static boolean enviarPintura(int idUsuario, Stage stage) {
        try {
            // Crear instancia de DatabaseManager
            dbManager = new DatabaseManager();

            // Verificar si todos los jueces han subido sus claves públicas
            List<Integer> idsJueces = dbManager.obtenerIdsJueces();
            for (int idJuez : idsJueces) {
                String publicKey = dbManager.obtenerLlavePublica(idJuez);
                if (publicKey == null) {
                    mostrarVentanaEmergente("Error",
                            "Todos los jueces deben subir sus claves públicas\n antes de enviar la pintura. Por favor espere a\n que los jueces suban sus claves.");
                    return false;
                }
            }

            // Seleccionar archivo de imagen
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar archivo de imagen");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de imagen", "*.jpg"));
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile == null) {
                System.out.println("No se seleccionó ningún archivo de imagen.");
                return false;
            }

            String INPUT_FILE = selectedFile.getAbsolutePath();

            // Solicitar nombre de la pintura
            String nombrePintura = obtenerNombrePintura(stage);
            if (nombrePintura == null || nombrePintura.isEmpty()) {
                System.out.println("No se ingresó un nombre para la pintura.");
                return false;
            }

            // Generar clave AES y cifrar la imagen
            SecretKey originalKey = AESGC.generateAESKey();
            String base64Key = Base64.getEncoder().encodeToString(originalKey.getEncoded());
            SecretKey secretKey = AESGC.decodeAESKeyFromBase64(base64Key);
            String paintingAESBase64Encoded = AESGC.encodeFileToBase64(secretKey, INPUT_FILE);

            // Registrar la pintura en la tabla Pinturas
            boolean pinturaRegistrada = dbManager.registrarPintura(idUsuario, nombrePintura, paintingAESBase64Encoded);
            if (!pinturaRegistrada) {
                System.out.println("Error al registrar la pintura en la base de datos.");
                return false;
            }

            // Obtener el ID de la pintura registrada
            int idPintura = dbManager.obtenerIdPinturaPorNombre(nombrePintura, idUsuario);

            // Encriptar la clave AES con las claves públicas de los jueces y registrar
            for (int idJuez : idsJueces) {
                String publicKey = dbManager.obtenerLlavePublica(idJuez);
                String wrappedKey = RSA.encrypt(base64Key, publicKey);

                boolean llaveRegistrada = dbManager.registrarLlaveEnvuelta(idPintura, idJuez, wrappedKey);
                if (!llaveRegistrada) {
                    System.out.println("Error al registrar la llave envuelta para el juez con ID: " + idJuez);
                }
            }

            System.out.println("Pintura y llaves envueltas registradas exitosamente.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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

    /**
     * Abre un nuevo Stage para solicitar el nombre de la pintura.
     *
     * @param ownerStage Stage principal que será el dueño de la ventana emergente.
     * @return Nombre ingresado por el usuario o null si no se confirma.
     */
    private static String obtenerNombrePintura(Stage ownerStage) {
        final String[] nombrePintura = { null };

        Stage inputStage = new Stage();
        inputStage.initOwner(ownerStage);
        inputStage.setTitle("Nombre de la Pintura");

        Label label = new Label("Ingresa el nombre de la pintura:");
        TextField textField = new TextField();
        Button confirmButton = new Button("Confirmar");
        Button cancelButton = new Button("Cancelar");

        confirmButton.setOnAction(_ -> {
            nombrePintura[0] = textField.getText();
            inputStage.close();
        });

        cancelButton.setOnAction(_ -> {
            nombrePintura[0] = null;
            inputStage.close();
        });

        VBox layout = new VBox(10, label, textField, confirmButton, cancelButton);
        layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 150);
        inputStage.setScene(scene);
        inputStage.showAndWait();

        return nombrePintura[0];
    }

    /**
     * Muestra una ventana emergente con un mensaje.
     */
    private static void mostrarVentanaEmergente(String titulo, String mensaje) {
        Stage popupStage = new Stage();
        popupStage.setTitle(titulo);

        Label label = new Label(mensaje);
        Button closeButton = new Button("Cerrar");
        closeButton.setOnAction(_ -> popupStage.close());

        VBox layout = new VBox(10, label, closeButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 150);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }
}
