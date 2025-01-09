package src;

import java.io.File;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ArtistWindow extends Application {

    private int idUsuario; // ID del usuario actual
    private String usuarioString; // ID del usuario actual

    // Constructor para recibir el ID del usuario
    public ArtistWindow(int idUsuario, String usuarioString) {
        this.idUsuario = idUsuario;
        this.usuarioString = usuarioString;
    }

    @Override
    public void start(Stage primaryStage) {
        // Verificar si el acuerdo de confidencialidad está firmado
        boolean isAgreementSigned = ArtistProcess.verificarAcuerdoFirmado(idUsuario);

        // Crear botones
        Button btnCrearLlaves = new Button("Generar par de llaves ECDSA");
        Button btnFirmar = new Button("Firmar Acuerdo de Confidencialidad");
        Button btnEnviarPintura = new Button("Enviar Pintura");

        // Crear botón "Subir Clave Pública"
        Button btnSubirClavePublica = new Button("Subir Clave Pública");

        // Configurar el estado de los botones
        btnFirmar.setDisable(isAgreementSigned);
        btnEnviarPintura.setDisable(!isAgreementSigned);

        btnCrearLlaves.setOnAction(event -> {
            String idConcursante = ArtistProcess.obtenerUsuarioConcursante(idUsuario);
            try {
                ECDSA.generateAndSaveKeyPair(idConcursante.concat("_privKey_ECDSA.txt"), idConcursante.concat("_pubKey_ECDSA.txt"));
            } catch (Exception e) {
                System.err.println("Error al generar par de claves ECDSA");
                e.printStackTrace();
            }
        });

        // Acción para firmar el acuerdo
        btnFirmar.setOnAction(event -> {
            boolean firmado = ArtistProcess.firmar(idUsuario, primaryStage);
            if (firmado) {
                btnFirmar.setDisable(true);
                btnEnviarPintura.setDisable(false);
                System.out.println("Acuerdo firmado con éxito.");
            } else {
                System.out.println("Error al firmar el acuerdo.");
            }
        });

        // Configurar la acción del botón para subir clave pública
        btnSubirClavePublica.setOnAction(event -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Seleccionar Clave Pública");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
                File selectedFile = fileChooser.showOpenDialog(primaryStage);

                // Delegar la funcionalidad a JudgeProcess
                boolean resultado = JudgeProcess.subirClavePublica(idUsuario, selectedFile);
                if (!resultado) {
                    System.out.println("No se pudo completar la operación.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Acción para enviar pintura (simulado)
        btnEnviarPintura.setOnAction(event -> {
            boolean enviado = ArtistProcess.enviarPintura(idUsuario, primaryStage);
            if (enviado) {
                System.out.println("Pintura enviada correctamente.");
            } else {
                System.out.println("Error al enviar pintura.");
            }
        });

        // Crear la interfaz gráfica
        Label label = new Label("Bienvenido al sistema de concursantes.");
        VBox layout = new VBox(10, label, btnCrearLlaves, btnFirmar, btnEnviarPintura, btnSubirClavePublica);
        Scene scene = new Scene(layout, 400, 200);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Ventana de Concursantes (" + usuarioString + ")");
        primaryStage.show();
    }

    // Método estático para iniciar esta ventana desde otras clases
    public static void ShowArtistWindow(Stage stage, int idUsuario, String usuarioString) {
        ArtistWindow window = new ArtistWindow(idUsuario, usuarioString);
        try {
            window.start(stage);

            // Botón para cerrar sesión
            Button logoutButton = new Button("Cerrar sesión");
            logoutButton.setOnAction(event -> {
                LoginScreen loginScreen = new LoginScreen();
                try {
                    loginScreen.start(stage); // Regresa al login
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Añadir el botón al layout
            VBox layout = (VBox) stage.getScene().getRoot();
            layout.getChildren().add(logoutButton);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
