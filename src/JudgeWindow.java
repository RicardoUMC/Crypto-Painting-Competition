package src;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class JudgeWindow {

    public static void ShowJudgeWindow(Stage primaryStage, int idUsuario) {
        // Crear botón "Calificar"
        Button btnCalificar = new Button("Calificar");

        // Configurar la acción del botón
        btnCalificar.setOnAction(_ -> {

        });

        // Contenedor para el botón
        HBox buttonBox = new HBox(10, btnCalificar);
        buttonBox.setStyle("-fx-padding: 15; -fx-alignment: center;");

        // Contenedor principal
        VBox root = new VBox(buttonBox);
        root.setStyle("-fx-padding: 20; -fx-spacing: 15; -fx-alignment: center;");

        // Crear escena
        Scene scene = new Scene(root, 300, 200);

        // Configurar ventana
        primaryStage.setTitle("Ventana del Juez");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /*
     * public static void main(String[] args) {
     * launch(args);
     * }
     */
}
