package src;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class JudgeWindow {

    public static void ShowJudgeWindow(Stage primaryStage, int idUsuario) {
        // Crear botón "Subir Clave Pública"
        Button btnCrearLlaves = new Button("Generar par de llaves");

        // Crear botón "Calificar"
        Button btnCalificar = new Button("Calificar");

        // Crear botón "Subir Clave Pública"
        Button btnSubirClavePublica = new Button("Subir Clave Pública");

        btnCrearLlaves.setOnAction(_ -> {
            RSA.generateAndSaveKeyPair("privKey.txt", "pubKey.txt");
        });
        
        btnCalificar.setOnAction(_ -> {
            StarRatingApp starRatingApp = new StarRatingApp();
            try {
                Stage stage = new Stage();
                starRatingApp.showRatingApp(stage, idUsuario);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Configurar la acción del botón para subir clave pública
        btnSubirClavePublica.setOnAction(_ -> {
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

        // Contenedor para los botones
        HBox buttonBox = new HBox(10, btnCrearLlaves, btnCalificar, btnSubirClavePublica);
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
}
