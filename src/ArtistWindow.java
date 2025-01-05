package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ArtistWindow extends Application {

    private int idUsuario; // ID del usuario actual

    // Constructor para recibir el ID del usuario
    public ArtistWindow(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    @Override
    public void start(Stage primaryStage) {
        // Verificar si el acuerdo de confidencialidad está firmado
        boolean isAgreementSigned = ArtistProcess.verificarAcuerdoFirmado(idUsuario);

        // Crear botones
        Button btnFirmar = new Button("Firmar Acuerdo de Confidencialidad");
        Button btnEnviarPintura = new Button("Enviar Pintura");

        // Configurar el estado de los botones
        btnFirmar.setDisable(isAgreementSigned);
        btnEnviarPintura.setDisable(!isAgreementSigned);

        // Acción para firmar el acuerdo
        btnFirmar.setOnAction(_ -> {
            boolean firmado = ArtistProcess.firmar(idUsuario, primaryStage);
            if (firmado) {
                btnFirmar.setDisable(true);
                btnEnviarPintura.setDisable(false);
                System.out.println("Acuerdo firmado con éxito.");
            } else {
                System.out.println("Error al firmar el acuerdo.");
            }
        });

        // Acción para enviar pintura (simulado)
        btnEnviarPintura.setOnAction(_ -> {
            boolean firmado = ArtistProcess.enviarPintura(idUsuario);
            if (firmado) {
                System.out.println("Pintura enviada correctamente.");
            } else {
                System.out.println("Error al enviar pintura.");
            }
        });

        // Crear la interfaz gráfica
        Label label = new Label("Bienvenido al sistema de concursantes.");
        VBox layout = new VBox(10, label, btnFirmar, btnEnviarPintura);
        Scene scene = new Scene(layout, 400, 200);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Ventana de Concursantes");
        primaryStage.show();
    }

    // Método estático para iniciar esta ventana desde otras clases
    public static void ShowArtistWindow(Stage stage, int idUsuario) {
        ArtistWindow window = new ArtistWindow(idUsuario);
        try {
            window.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
