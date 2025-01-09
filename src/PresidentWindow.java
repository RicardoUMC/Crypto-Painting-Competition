package src;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PresidentWindow {
    private static DatabaseManager dbManager; // Instancia para conexión con la base de datos

    public static void ShowPresidentWindow(Stage primaryStage, int idUsuario, String usuarioString) {
        // Crear botón "Subir Clave Pública"
        Button btnCrearLlaves = new Button("Generar par de llaves");
        Image llave = new Image("./src/assets/img/llaves.png");

        // Crear el ImageView para mostrar el ícono
        ImageView llaveView = new ImageView(llave);

        llaveView.setFitWidth(80); // Ancho del ícono
        llaveView.setFitHeight(80);
        btnCrearLlaves.setGraphic(llaveView); // Agregar ícono

        // Configurar la posición del ícono
        btnCrearLlaves.setContentDisplay(ContentDisplay.TOP);

        // Crear botón "Firmar"
        Button btnFirmar = new Button("Firmar");
        Image firma = new Image("./src/assets/img/firma.png");

        // Crear el ImageView para mostrar el ícono
        ImageView firmaView = new ImageView(firma);

        firmaView.setFitWidth(80); // Ancho del ícono
        firmaView.setFitHeight(80);
       btnFirmar.setGraphic(firmaView); // Agregar ícono

        // Configurar la posición del ícono
        btnFirmar.setContentDisplay(ContentDisplay.TOP);

         // Crear botón "Agregar usuario"
        Button agregarButton = new Button("Agregar usuario");
        Image usuario = new Image("./src/assets/img/usuario.png");

        // Crear el ImageView para mostrar el ícono
        ImageView usuarioView = new ImageView(usuario);

        usuarioView.setFitWidth(80); // Ancho del ícono
       usuarioView.setFitHeight(80);
        agregarButton.setGraphic(usuarioView); // Agregar ícono

        // Configurar la posición del ícono
        agregarButton.setContentDisplay(ContentDisplay.TOP);

        // Crear botón "Subir Clave Pública"
        Button btnSubirClavePublica = new Button("Subir Clave Pública");
        Image subir = new Image("./src/assets/img/subir.png");

        // Crear el ImageView para mostrar el ícono
        ImageView subirView = new ImageView(subir);

        subirView.setFitWidth(80); // Ancho del ícono
        subirView.setFitHeight(80);
       btnSubirClavePublica.setGraphic(subirView); // Agregar ícono

        // Configurar la posición del ícono
        btnSubirClavePublica.setContentDisplay(ContentDisplay.TOP);

        btnCrearLlaves.setOnAction(event -> {
            String juezUsuario = PresidentProcess.obtenerUsuarioPresidente(idUsuario);
            RSA.generateAndSaveKeyPair(juezUsuario.concat("_privKey.txt"), juezUsuario.concat("_pubKey.txt"));
        });

        // Configurar la acción del botón
        btnFirmar.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Clave Privada");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            PresidentProcess.firmarMensajeEnmascarado(selectedFile);
        });

        // Configurar la acción del botón para subir clave pública
        btnSubirClavePublica.setOnAction(event -> {
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

        agregarButton.setOnAction(event -> agregaUsuario());

        // Contenedor para el botón
        HBox buttonBox = new HBox(10, btnCrearLlaves, btnFirmar, agregarButton, btnSubirClavePublica);
        buttonBox.setStyle("-fx-padding: 15; -fx-alignment: center;");

        // Contenedor principal
        VBox root = new VBox(buttonBox);
        root.setStyle("-fx-padding: 20; -fx-spacing: 15; -fx-alignment: center;");

        // Crear escena
        Scene scene = new Scene(root, 600, 450);

        Button btnGanadores = new Button("Mostrar Ganadores");

        Image podio = new Image("./src/assets/img/ganadores.png");

        // Crear el ImageView para mostrar el ícono
        ImageView ganadoresView = new ImageView(podio);

        ganadoresView.setFitWidth(80); // Ancho del ícono
       ganadoresView.setFitHeight(80);
        btnGanadores.setGraphic(ganadoresView); // Agregar ícono

        // Configurar la posición del ícono
        btnGanadores.setContentDisplay(ContentDisplay.TOP);

        btnGanadores.setOnAction(event -> {
            List<Map<String, Object>> ganadores = PresidentProcess.calcularGanadores();
            if (!ganadores.isEmpty()) {
                WinnersWindow.mostrarGanadores(primaryStage, ganadores);
            } else {
                System.out.println("No hay evaluaciones verificadas.");
            }
        });
        root.getChildren().add(btnGanadores);

        // Botón para cerrar sesión
        Button logoutButton = new Button("Cerrar sesión");
        Image cerrarsesion = new Image("./src/assets/img/cerrar_sesion.png");

        // Crear el ImageView para mostrar el ícono
        ImageView cerrarsesionView = new ImageView(cerrarsesion);

        cerrarsesionView.setFitWidth(80); // Ancho del ícono
        cerrarsesionView.setFitHeight(80);
       logoutButton.setGraphic(cerrarsesionView); // Agregar ícono

        // Configurar la posición del ícono
        logoutButton.setContentDisplay(ContentDisplay.TOP);

        logoutButton.setOnAction(event -> {
            LoginScreen loginScreen = new LoginScreen();
            try {
                loginScreen.start(primaryStage); // Regresa al login
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        root.getChildren().add(logoutButton);

        // Configurar ventana
        primaryStage.setTitle("Ventana del Presidente ("+ usuarioString + ")");
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

        addButton.setOnAction(event -> {
            try {
                dbManager = new DatabaseManager();
                dbManager.crearUsuario(nameField.getText(), userField.getText(), passwordField.getText(),
                        typeComboBox.getValue().toUpperCase());
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
