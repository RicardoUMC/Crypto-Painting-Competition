package src;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class WinnersWindow {
    public static void mostrarGanadores(Stage primaryStage, List<Map<String, Object>> ganadores) {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");

        for (Map<String, Object> pintura : ganadores) {
            String nombrePintura = (String) pintura.get("nombrePintura");
            int calificacionTotal = (int) pintura.get("calificacionTotal");

            Label label = new Label("Pintura: " + nombrePintura +
                    "\nCalificación Total: " + calificacionTotal);
            root.getChildren().add(label);
        }

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Ganadores del Concurso");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
