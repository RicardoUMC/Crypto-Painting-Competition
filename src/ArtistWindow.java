package src;

import java.io.File;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
        Button btnCrearLlaves = new Button("Generar par de llaves");
         Image llave = new Image("./src/assets/img/llaves.png");

        // Crear el ImageView para mostrar el ícono
        ImageView llaveView = new ImageView(llave);

        llaveView.setFitWidth(80); // Ancho del ícono
        llaveView.setFitHeight(80);
        btnCrearLlaves.setGraphic(llaveView); // Agregar ícono

        // Configurar la posición del ícono
        btnCrearLlaves.setContentDisplay(ContentDisplay.TOP);

        Button btnFirmar = new Button("Firmar Acuerdo de Confidencialidad");
Image firma = new Image("./src/assets/img/firma.png");

        // Crear el ImageView para mostrar el ícono
        ImageView firmaView = new ImageView(firma);

        firmaView.setFitWidth(80); // Ancho del ícono
        firmaView.setFitHeight(80);
       btnFirmar.setGraphic(firmaView); // Agregar ícono

        // Configurar la posición del ícono
        btnFirmar.setContentDisplay(ContentDisplay.TOP);

        Button btnEnviarPintura = new Button("Enviar Pintura");
        Image enviar = new Image("./src/assets/img/enviar.png");

        // Crear el ImageView para mostrar el ícono
        ImageView enviarView = new ImageView(enviar);

        enviarView.setFitWidth(80); // Ancho del ícono
        enviarView.setFitHeight(80);
       btnEnviarPintura.setGraphic(enviarView); // Agregar ícono

        // Configurar la posición del ícono
        btnEnviarPintura.setContentDisplay(ContentDisplay.TOP);
        // Crear botón "Subir Clave Pública"
        Button btnSubirClavePublica = new Button("Subir Clave Pública");
        Image subir = new Image("./src/assets/img/subir.png");

        // Crear el ImageView para mostrar el ícono
        ImageView subirView = new ImageView(subir);

        subirView.setFitWidth(80); // Ancho del ícono
        subirView.setFitHeight(80);
       btnSubirClavePublica.setGraphic(subirView); // Agregar ícono

        // Configurar la posición del ícono
        btnSubirClavePublica.setContentDisplay(ContentDisplay.TOP);
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

        // Configurar la acción del botón para enviar clave pública
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
        layout.setPadding(new Insets(10, 20, 10, 40));
        Scene scene = new Scene(layout, 280, 640);

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
            Image cerrarsesion = new Image("./src/assets/img/cerrar_sesion.png");

            // Crear el ImageView para mostrar el ícono
            ImageView cerrarsesionView = new ImageView(cerrarsesion);
    
            cerrarsesionView.setFitWidth(80); // Ancho del ícono
            cerrarsesionView.setFitHeight(80);
           logoutButton.setGraphic(cerrarsesionView); // Agregar ícono
    
            // Configurar la posición del ícono
            logoutButton.setContentDisplay(ContentDisplay.TOP);

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
