package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginScreen extends Application {

    private DatabaseManager dbManager;

    @Override
    public void start(Stage primaryStage) {
        try {
            dbManager = new DatabaseManager();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al conectar con la base de datos.");
            return;
        }

        Label userLabel = new Label("Usuario:");
        Label passwordLabel = new Label("Contraseña:");
        TextField userField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Iniciar sesión");

        loginButton.setOnAction(_ -> {
            String usuario = userField.getText();
            String contrasena = passwordField.getText();

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                System.out.println("Por favor, llena todos los campos.");
                return;
            }

            try {
                // Validar usuario con DatabaseManager
                int idUsuario = dbManager.obtenerIdUsuario(usuario, contrasena);
                String userType = dbManager.obtenerTipoUsuario(idUsuario);

                if (idUsuario == -1 || userType == null) {
                    System.out.println("Usuario o contraseña incorrectos.");
                    return;
                }

                if (dbManager != null) {
                    dbManager.close();
                }

                // Redirigir según el tipo de usuario
                switch (userType.toUpperCase()) {
                    case "CONCURSANTE":
                        ArtistWindow.ShowArtistWindow(primaryStage, idUsuario);
                        break;
                    case "JUEZ":
                        // JudgeWindow.ShowJudgeWindow(primaryStage, idUsuario);
                        break;
                    case "PRESIDENTE":
                        // PresidentWindow.ShowPresidentWindow(primaryStage, idUsuario);
                        break;
                    default:
                        System.out.println("Rol desconocido: " + userType);
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Error durante la validación del usuario.");
            }
        });

        GridPane grid = new GridPane();
        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);

        Scene scene = new Scene(grid, 300, 150);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Administrador");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
