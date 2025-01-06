package src;
import java.sql.SQLException;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PresidentWindow {
    private static DatabaseManager dbManager; // Instancia para conexión con la base de datos

    public static void ShowPresidentWindow(Stage primaryStage, int idUsuario) {
        // Crear botón "Firmar"
        Button btnFirmar = new Button("Firmar");
        Button agregarButton = new Button("Agregar usuario");

        // Configurar la acción del botón
        btnFirmar.setOnAction(_ -> {

        });
        
        agregarButton.setOnAction(_ -> agregaUsuario());

        // Contenedor para el botón
        HBox buttonBox = new HBox(10, btnFirmar, agregarButton);
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

    private static void agregaUsuario() {
        // Crear una ventana para ingresar datos del usuario
        Stage stage = new Stage();
        GridPane grid = new GridPane();
        TextField nameField = new TextField();
        TextField userField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Presidente", "Juez", "Concursante");
        Button addButton = new Button("Agregar");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Usuario:"), 0, 1);
        grid.add(userField, 1, 1);
        grid.add(new Label("Contraseña:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Tipo de Usuario:"), 0, 3);
        grid.add(typeComboBox, 1, 3);
        grid.add(addButton, 1, 4);

        addButton.setOnAction(_ -> {
            try {
                dbManager = new DatabaseManager();
                dbManager.crearUsuario(nameField.getText(), userField.getText(), passwordField.getText(), typeComboBox.getValue().toUpperCase());
            } catch (SQLException e1) {
                e1.printStackTrace();
            } finally {
                // Cerrar la conexión a la base de datos
                if (dbManager != null) {
                    try {
                        dbManager.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                stage.close();
            }
        });

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
    }

    /*
     * public static void main(String[] args) {
     * launch(args);
     * }
     */
}
