import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ArtistWindow {

    public static void ShowArtistWindow(Stage primaryStage) {
        // Crear botones
        Button btnSign = new Button("Firmar");
        Button btnSendPaint = new Button("Enviar Pintura");

        // Configurar acciones de los botones
        btnSign.setOnAction(e -> {
            Artistprocess.firmar();

        });
        btnSendPaint.setOnAction(e -> {
            Artistprocess.enviarPintura();
        });

        // Contenedor para los botones
        HBox buttonBox = new HBox(10, btnSign, btnSendPaint);
        buttonBox.setStyle("-fx-padding: 15; -fx-alignment: center;");

        // Contenedor principal
        VBox root = new VBox(buttonBox);
        root.setStyle("-fx-padding: 20; -fx-spacing: 15; -fx-alignment: center;");

        // Crear escena
        Scene scene = new Scene(root, 300, 200);

        // Configurar ventana
        primaryStage.setTitle("Ventana con botones");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /*
     * public static void main(String[] args) {
     * launch(args);
     * }
     */
}
