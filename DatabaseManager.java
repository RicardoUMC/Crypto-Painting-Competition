import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/tu_base_de_datos";
    private static final String DB_USER = "tu_usuario";
    private static final String DB_PASSWORD = "tu_contraseña";

    private Connection connection;

    // Constructor: Conecta automáticamente a la base de datos
    public DatabaseManager() throws SQLException {
        connect();
    }

    // Conectar a la base de datos
    private void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
    }

    // Cerrar la conexión a la base de datos
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Crear un nuevo usuario
    public void crearUsuario(String nombre, String usuario, String contrasena, String tipoUsuario, String llavePublica)
            throws SQLException {
        String query = "INSERT INTO Usuarios (nombre, usuario, contrasena, tipo_usuario, llave_publica) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nombre);
            stmt.setString(2, usuario);
            stmt.setString(3, contrasena); // Asegúrate de usar un hash para la contraseña
            stmt.setString(4, tipoUsuario);
            stmt.setString(5, llavePublica);
            stmt.executeUpdate();
        }
    }

    // Obtener todos los usuarios
    public List<String> obtenerUsuarios() throws SQLException {
        List<String> usuarios = new ArrayList<>();
        String query = "SELECT * FROM Usuarios";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                usuarios.add(rs.getString("nombre") + " (" + rs.getString("tipo_usuario") + ")");
            }
        }
        return usuarios;
    }

    // Obtener la llave pública de un usuario
    public String obtenerLlavePublica(int idUsuario) throws SQLException {
        String query = "SELECT llave_publica FROM Usuarios WHERE id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("llave_publica");
                }
            }
        }
        return null; // Si no se encuentra el usuario
    }

    // Registrar una pintura
    public void registrarPintura(int idUsuario, String nombrePintura, String archivoCifrado) throws SQLException {
        String query = "INSERT INTO Pinturas (id_usuario, nombre_pintura, archivo_cifrado) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUsuario);
            stmt.setString(2, nombrePintura);
            stmt.setString(3, archivoCifrado);
            stmt.executeUpdate();
        }
    }

    // Obtener pinturas por usuario
    public List<String> obtenerPinturasPorUsuario(int idUsuario) throws SQLException {
        List<String> pinturas = new ArrayList<>();
        String query = "SELECT nombre_pintura FROM Pinturas WHERE id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pinturas.add(rs.getString("nombre_pintura"));
                }
            }
        }
        return pinturas;
    }

    // Registrar una evaluación
    public void registrarEvaluacion(int idPintura, int idJuez, String calificacion, String comentario,
            String firmaCiega) throws SQLException {
        String query = "INSERT INTO Evaluaciones (id_pintura, id_juez, calificacion, comentario, firma_ciega) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idPintura);
            stmt.setInt(2, idJuez);
            stmt.setString(3, calificacion);
            stmt.setString(4, comentario);
            stmt.setString(5, firmaCiega);
            stmt.executeUpdate();
        }
    }

    // Obtener evaluaciones por pintura
    public List<String> obtenerEvaluacionesPorPintura(int idPintura) throws SQLException {
        List<String> evaluaciones = new ArrayList<>();
        String query = "SELECT calificacion, comentario FROM Evaluaciones WHERE id_pintura = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idPintura);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    evaluaciones.add("Calificación: " + rs.getString("calificacion") + ", Comentario: "
                            + rs.getString("comentario"));
                }
            }
        }
        return evaluaciones;
    }

    // Validar usuario para iniciar sesión
    public String validarUsuario(String usuario, String contrasena) throws SQLException {
        String query = "SELECT tipo_usuario FROM Usuarios WHERE usuario = ? AND contrasena = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, usuario);
            stmt.setString(2, contrasena); // Debe comparar el hash de la contraseña
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("tipo_usuario");
                }
            }
        }
        return null; // Si las credenciales no son válidas
    }
}
