import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PresidentWindow {

    public static void ShowPresidentWindow(Stage primaryStage) {
        // Crear botón "Firmar"
        Button btnFirmar = new Button("Firmar");

        // Configurar la acción del botón
        btnFirmar.setOnAction(e -> {

        });

        // Contenedor para el botón
        HBox buttonBox = new HBox(10, btnFirmar);
        buttonBox.setStyle("-fx-padding: 15; -fx-alignment: center;");

        // Contenedor principal
        VBox root = new VBox(buttonBox);
        root.setStyle("-fx-padding: 20; -fx-spacing: 15; -fx-alignment: center;");

        // Crear escena
        Scene scene = new Scene(root, 300, 200);

        // Configurar ventana
        primaryStage.setTitle("Ventana del Presidente");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /*
     * public static void main(String[] args) {
     * launch(args);
     * }
     */
}
