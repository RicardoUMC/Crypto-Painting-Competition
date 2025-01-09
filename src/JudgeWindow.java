package src;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import src.JudgeProcess.Evaluacion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JudgeWindow {

    public static void ShowJudgeWindow(Stage primaryStage, int idJuez, String usuarioString) {
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
        // Crear botón "Calificar"
        Button btnCalificar = new Button("Calificar");
        Image calificar = new Image("./src/assets/img/calificar.png");

        // Crear el ImageView para mostrar el ícono
        ImageView calificarView = new ImageView(calificar);

        calificarView.setFitWidth(80); // Ancho del ícono
        calificarView.setFitHeight(80);
        btnCalificar.setGraphic(calificarView); // Agregar ícono

        // Configurar la posición del ícono
        btnCalificar.setContentDisplay(ContentDisplay.TOP);

        // Crear botón "Verificar"
        Button btnVerificarFirma = new Button("Verificar");
        Image verificar = new Image("./src/assets/img/verificar.png");

        // Crear el ImageView para mostrar el ícono
        ImageView verificarView = new ImageView(verificar);

        verificarView.setFitWidth(80); // Ancho del ícono
        verificarView.setFitHeight(80);
        btnVerificarFirma.setGraphic(verificarView); // Agregar ícono

        // Configurar la posición del ícono
        btnVerificarFirma.setContentDisplay(ContentDisplay.TOP);

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
            String juezUsuario = JudgeProcess.obtenerUsuarioJuez(idJuez);
            RSA.generateAndSaveKeyPair(juezUsuario.concat("_privKey.txt"), juezUsuario.concat("_pubKey.txt"));
        });

        btnCalificar.setOnAction(event -> {
            ArrayList<Integer> participantes = JudgeProcess.validaFirmasECDSA();
            StarRatingApp starRatingApp = new StarRatingApp();
            try {
                Stage stage = new Stage();
                starRatingApp.showRatingApp(stage, idJuez, participantes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Configurar la acción del botón para subir clave pública
        btnSubirClavePublica.setOnAction(event -> {
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

        btnVerificarFirma.setOnAction(event -> {
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

        // Crear escena
        Scene scene = new Scene(root, 600, 300);

        // Configurar ventana
        primaryStage.setTitle("Ventana del Juez ("+ usuarioString + ")");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
