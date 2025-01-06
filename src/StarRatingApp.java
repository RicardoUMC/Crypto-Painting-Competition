package src;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.List;

public class StarRatingApp {

    public void showRatingApp(Stage primaryStage, int idJuez) {
        DatabaseManager dbManager = null;
        try {
            // Crear conexión a la base de datos
            dbManager = new DatabaseManager();

            // Obtener la clave privada para decifrado de pinturas
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Clave Privada");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            String privateKeyString = new String(Files.readAllBytes(selectedFile.toPath()));

            // Obtener pinturas registradas
            List<Pintura> pinturas = dbManager.obtenerPinturas();

            // Contenedor principal con scroll
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            VBox mainContainer = new VBox(20);
            mainContainer.setStyle("-fx-padding: 20; -fx-spacing: 15;");
            scrollPane.setContent(mainContainer);

            // Crear secciones dinámicas para cada pintura
            for (Pintura pintura : pinturas) {
                HBox pinturaBox = new HBox(20);
                pinturaBox.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1;");

                // Decodificar y mostrar la imagen de la pintura
                byte[] imagenBytes = JudgeProcess.descifrarPintura(dbManager, pintura, idJuez, privateKeyString);

                ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(imagenBytes)));
                imageView.setFitWidth(200);
                imageView.setPreserveRatio(true);

                // Contenedor para comentarios y calificación
                VBox detailsBox = new VBox(10);

                // Campo para comentarios
                TextArea comentarioArea = new TextArea();
                comentarioArea.setPromptText("Escribe tu comentario...");
                comentarioArea.setPrefRowCount(3);

                // Calificación con estrellas
                HBox starBox = new HBox(5);
                ImageView[] stars = new ImageView[5];
                int[] rating = { 0 }; // Almacena la calificación para esta pintura
                Image starEmpty = new Image("./src/assets/img/estrella_sin_color.png");
                Image starFilled = new Image("./src/assets/img/estrella_dorada.png");

                for (int i = 0; i < 5; i++) {
                    ImageView star = new ImageView(starEmpty);
                    star.setFitWidth(30);
                    star.setPreserveRatio(true);

                    final int starIndex = i;
                    star.setOnMouseClicked(_ -> {
                        updateRating(stars, starFilled, starEmpty, starIndex + 1);
                        rating[0] = starIndex + 1;
                    });

                    stars[i] = star;
                    starBox.getChildren().add(star);
                }

                // Agregar componentes al contenedor
                detailsBox.getChildren().addAll(
                        new Label("Nombre de la pintura: " + pintura.getNombrePintura()),
                        comentarioArea,
                        new Label("Calificación:"),
                        starBox);

                // Agregar la imagen y los detalles a la sección
                pinturaBox.getChildren().addAll(imageView, detailsBox);
                mainContainer.getChildren().add(pinturaBox);
            }

            // Agregar botón para guardar todas las evaluaciones
            Button saveButton = new Button("Guardar Evaluaciones");
            saveButton.setOnAction(_ -> {
                // Lógica para guardar calificaciones y comentarios
                System.out.println("Evaluaciones guardadas exitosamente.");
            });
            mainContainer.getChildren().add(saveButton);

            // Configurar escena
            Scene scene = new Scene(scrollPane, 800, 600);
            primaryStage.setTitle("Evaluación de Pinturas");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
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

    // Método para actualizar las estrellas seleccionadas
    private void updateRating(ImageView[] stars, Image starFilled, Image starEmpty, int newRating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < newRating) {
                stars[i].setImage(starFilled);
            } else {
                stars[i].setImage(starEmpty);
            }
        }
    }

}
