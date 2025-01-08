package src;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import src.JudgeProcess.Evaluacion;

import java.io.File;
import java.util.List;

public class JudgeWindow {

    public static void ShowJudgeWindow(Stage primaryStage, int idJuez) {
        // Crear botón "Subir Clave Pública"
        Button btnCrearLlaves = new Button("Generar par de llaves");

        // Crear botón "Calificar"
        Button btnCalificar = new Button("Calificar");

        // Crear botón "Verificar"
        Button btnVerificarFirma = new Button("Verificar");

        // Crear botón "Subir Clave Pública"
        Button btnSubirClavePublica = new Button("Subir Clave Pública");

        btnCrearLlaves.setOnAction(_ -> {
            String juezUsuario = JudgeProcess.obtenerUsuarioJuez(idJuez);
            RSA.generateAndSaveKeyPair(juezUsuario.concat("_privKey.txt"), juezUsuario.concat("_pubKey.txt"));
        });

        btnCalificar.setOnAction(_ -> {
            StarRatingApp starRatingApp = new StarRatingApp();
            try {
                Stage stage = new Stage();
                starRatingApp.showRatingApp(stage, idJuez);
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
                boolean resultado = JudgeProcess.subirClavePublica(idJuez, selectedFile);
                if (!resultado) {
                    System.out.println("No se pudo completar la operación.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnVerificarFirma.setOnAction(_ -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Seleccionar Archivo de R");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
                File selectedFile = fileChooser.showOpenDialog(primaryStage);

                List<Evaluacion> resultados = JudgeProcess.obtenerEvaluaciones(idJuez);

                String mensajeOriginal = new String();
                for (Evaluacion evaluacion : resultados) {
                    mensajeOriginal = mensajeOriginal.concat(evaluacion.getComentarioTexto()).concat(Integer.toString(evaluacion.getRating()[0]))
                            .concat(Integer.toString(evaluacion.getIdPintura()))
                            .concat(Integer.toString(idJuez));
                }
                
                boolean resultado = JudgeProcess.verificarFirmaCiega(idJuez, mensajeOriginal, selectedFile);
                if (resultado) {
                    System.out.println("Firma verificada correctamente.");
                } else {
                    System.out.println("La firma de la pintura no es válida.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Contenedor para los botones
        HBox buttonBox = new HBox(10, btnCrearLlaves, btnCalificar, btnVerificarFirma, btnSubirClavePublica);
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
