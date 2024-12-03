import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginScreen extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Configurar los elementos de la interfaz de usuario
        Label userLabel = new Label("Usuario:");
        Label passwordLabel = new Label("Contraseña:");
        TextField userField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Iniciar sesión");

        // Configurar el evento del botón de inicio de sesión
        loginButton.setOnAction(e -> {
            // Validar usuario y contraseña (simulado)
            String usuario = userField.getText();
            String contraseña = passwordField.getText();

            if (usuario.isEmpty() || contraseña.isEmpty()) {
                // Mostrar el inicio nuevamente si los campos están vacíos
                start(primaryStage);
                return;
            }
            try {
                String userType = Servidor.login(usuario, contraseña);
                Servidor.displayUserInterface(userType, primaryStage);
                // String ROL = obtener rol del usuario
                switch (/*rol*/) {
                    case "artista":
                        ArtistWindow.ShowArtistWindow(primaryStage);
                        break;
                    case "juez":
                        JudgeWindow.ShowJudgeWindow(primaryStage);
                        break;
                    case "presidente":
                        PresidentWindow.ShowPresidentWindow(primaryStage);
                        break;
                    default:
                        System.out.println("Rol desconocido: " + userType);
                        break;

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
});

        // Configurar el diseño del grid para organizar los elementos
        GridPane grid = new GridPane();
        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);

        // Configurar la escena principal y mostrar la ventana
        Scene scene = new Scene(grid, 300, 150);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Administrador");
        primaryStage.show();
    }

    // Métodos para mostrar las interfaces de usuario específicas
    private void showArtistaWindow(Stage primaryStage) {
        Label label = new Label("Bienvenido, Artista!");
        Scene scene = new Scene(label, 300, 150);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ventana de Artista");
        primaryStage.show();
    }

    private void showJuezWindow(Stage primaryStage) {
        Label label = new Label("Bienvenido, Juez!");
        Scene scene = new Scene(label, 300, 150);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ventana de Juez");
        primaryStage.show();
    }

    private void showPresidenteWindow(Stage primaryStage) {
        Label label = new Label("Bienvenido, Presidente!");
        Scene scene = new Scene(label, 300, 150);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ventana de Presidente");
        primaryStage.show();
    }

    public static void initialize(String[] args) {
        launch(args);
    }
}
