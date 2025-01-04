package src;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StarRatingApp extends Application {
    private int rating = 0; // Variable para guardar la calificación

    @Override
    public void start(Stage primaryStage) {
        // Ruta de la imagen local
        String imagePath = "descarga.jpg"; // Cambia "path_to_your_image.jpg" por la ruta de tu imagen
        Image image = new Image(imagePath);
        ImageView imageView = new ImageView(image);

        // Ajustar tamaño de la imagen
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);

        // Imágenes de estrellas
        Image starEmpty = new Image("./assets/img/estrella_sin_color.png"); // Cambia "path_to_star_empty.png"
        Image starFilled = new Image("./assets/img/estrella_dorada.png"); // Cambia "path_to_star_filled.png"

        // Crear una fila de estrellas
        HBox starBox = new HBox(5);
        starBox.setAlignment(Pos.CENTER);
        ImageView[] stars = new ImageView[5];

        for (int i = 0; i < 5; i++) {
            ImageView star = new ImageView(starEmpty);
            star.setFitWidth(50);
            star.setPreserveRatio(true);

            final int starIndex = i;
            star.setOnMouseClicked(_ -> updateRating(stars, starFilled, starEmpty, starIndex + 1));

            stars[i] = star;
            starBox.getChildren().add(star);
        }

        // Campo de texto para comentarios
        TextArea commentField = new TextArea();
        commentField.setPromptText("Escribe tu comentario aquí...");
        commentField.setWrapText(true);

        // Botón para guardar
        Button saveButton = new Button("Guardar");
        saveButton.setOnAction(_ -> {
            String comment = commentField.getText();
            System.out.println("Calificación guardada: " + rating);
            System.out.println("Comentario guardado: " + comment);
        });

        // Layout principal
        VBox root = new VBox(20, imageView, starBox, commentField, saveButton);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // Configuración de la escena
        Scene scene = new Scene(root, 400, 500);
        primaryStage.setTitle("Calificación de Pinturas");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Método para actualizar las estrellas seleccionadas
    private void updateRating(ImageView[] stars, Image starFilled, Image starEmpty, int newRating) {
        rating = newRating;
        for (int i = 0; i < stars.length; i++) {
            if (i < newRating) {
                stars[i].setImage(starFilled);
            } else {
                stars[i].setImage(starEmpty);
            }
        }
    }

    public static void initialize(String[] args) {
        launch(args);
    }
}
