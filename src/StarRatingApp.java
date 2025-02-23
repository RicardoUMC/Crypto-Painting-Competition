package src;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import src.JudgeProcess.Evaluacion;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class StarRatingApp {

    private int starsQuantity = 3;

    public void showRatingApp(Stage primaryStage, int idJuez, ArrayList<Integer> participantes) {
        DatabaseManager dbManager = null;
        List<Pintura> pinturas = new ArrayList<Pintura>();
        String privateKeyString = null;
        try {
            // Crear conexión a la base de datos
            dbManager = new DatabaseManager();

            // Obtener la clave privada para decifrado de pinturas
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Clave Privada");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            privateKeyString = new String(Files.readAllBytes(selectedFile.toPath()));

            // Obtener pinturas registradas
            for (Integer idUsuario : participantes) {
                Pintura pintura = dbManager.obtenerPintura(idUsuario);
                if (pinturas != null) {
                    pinturas.add(pintura);
                }
            }

        } catch (Exception e) {
            System.err.println("Error al obtener pinturas registradas");
            e.printStackTrace();
            return;
        } finally {
            if (dbManager != null) {
                try {
                    dbManager.close();
                } catch (Exception e) {
                    System.err.println("Error al cerrar conexión con base de datos");
                    e.printStackTrace();
                }
            }
        }

        // Contenedor principal con scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox mainContainer = new VBox(20);
        mainContainer.setStyle("-fx-padding: 20; -fx-spacing: 15;");
        scrollPane.setContent(mainContainer);

        // Lista para almacenar evaluaciones
        List<Evaluacion> evaluaciones = new ArrayList<>();

        // Crear secciones dinámicas para cada pintura
        for (Pintura pintura : pinturas) {
            HBox pinturaBox = new HBox(20);
            pinturaBox.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1;");

            // Decodificar y mostrar la imagen de la pintura
            byte[] imagenBytes = JudgeProcess.descifrarPintura(pintura, idJuez, privateKeyString);

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
            HBox starBox = new HBox(this.starsQuantity);
            ImageView[] stars = new ImageView[this.starsQuantity];
            int[] rating = { 0 }; // Almacena la calificación para esta pintura
            Image starEmpty = new Image("./src/assets/img/estrella_sin_color.png");
            Image starFilled = new Image("./src/assets/img/estrella_dorada.png");

            for (int i = 0; i < this.starsQuantity; i++) {
                ImageView star = new ImageView(starEmpty);
                star.setFitWidth(30);
                star.setPreserveRatio(true);

                final int starIndex = i;
                star.setOnMouseClicked(event -> {
                    updateRating(stars, starFilled, starEmpty, starIndex + 1);
                    rating[0] = starIndex + 1;
                });

                stars[i] = star;
                starBox.getChildren().add(star);
            }

            // Cargar evaluación existente, si la hay
            Evaluacion evaluacionExistente = JudgeProcess.cargarEvaluacion(pintura.getIdPintura(), idJuez);
            if (evaluacionExistente != null) {
                comentarioArea.setText(evaluacionExistente.getComentarioTexto());
                rating[0] = evaluacionExistente.getRating()[0];
                updateRating(stars, starFilled, starEmpty, rating[0]);
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

            // Guardar evaluación en la lista
            evaluaciones.add(new Evaluacion(pintura.getIdPintura(), comentarioArea, rating));
        }

        // Agregar botón para guardar todas las evaluaciones
        Button saveButton = new Button("Guardar Evaluaciones");
        saveButton.setOnAction(event -> {
            boolean allValid = true;

            String mensaje = new String();
            for (Evaluacion eval : evaluaciones) {
                String comentario = eval.getComentario().getText();
                int calificacion = eval.getRating()[0];

                if (comentario.isEmpty() || calificacion == 0) {
                    allValid = false;
                    System.err.println("Error: Faltan datos en una evaluación.");
                    continue;
                }

                try {
                    mensaje = mensaje.concat(comentario).concat(Integer.toString(calificacion))
                        .concat(Integer.toString(eval.getIdPintura()))
                        .concat(Integer.toString(idJuez));

                    JudgeProcess.subirEvaluacion(eval.getIdPintura(), idJuez, String.valueOf(calificacion), comentario);
                } catch (Exception e) {
                    e.printStackTrace();
                    allValid = false;
                }
            }

            if (allValid && JudgeProcess.subirMensaje(idJuez, mensaje)) {
                System.out.println("Todas las evaluaciones fueron guardadas exitosamente.");
                primaryStage.close();
            } else {
                System.err.println("Algunas evaluaciones no pudieron ser guardadas.");
            }
        });
        mainContainer.getChildren().add(saveButton);

        // Configurar escena
        Scene scene = new Scene(scrollPane, 800, 600);
        primaryStage.setTitle("Evaluación de Pinturas");
        primaryStage.setScene(scene);
        primaryStage.show();
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
