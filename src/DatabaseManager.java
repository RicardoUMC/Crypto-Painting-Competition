package src;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

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
    public void crearUsuario(String nombre, String usuario, String contrasena, String tipoUsuario)
            throws SQLException {
        String hashedPassword = BCrypt.hashpw(contrasena, BCrypt.gensalt());
        String query = "INSERT INTO Usuarios (nombre, usuario, contrasena, tipo_usuario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nombre);
            stmt.setString(2, usuario);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, tipoUsuario);
            stmt.executeUpdate();
            System.out.println("Usuario agregado exitosamente.");
        } catch (SQLException ex) {
            System.err.println("Error con la consulta a la base de datos: " + ex.getMessage());
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

    public boolean actualizarLlavePublica(int idUsuario, String llavePublica) {
        String query = "UPDATE Usuarios SET llave_publica = ? WHERE id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, llavePublica);
            stmt.setInt(2, idUsuario);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Registrar una pintura
    public boolean registrarPintura(int idUsuario, String nombrePintura, String archivoCifrado) throws SQLException {
        String query = "INSERT INTO Pinturas (id_usuario, nombre_pintura, archivo_cifrado) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUsuario);
            stmt.setString(2, nombrePintura);
            stmt.setString(3, archivoCifrado);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Obtener pinturas por usuario
    public List<Pintura> obtenerPinturas() throws SQLException {
        List<Pintura> pinturas = new ArrayList<>();
        String query = "SELECT id_usuario, id_pintura, nombre_pintura, archivo_cifrado FROM Pinturas";
        try (PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int idUsuario = rs.getInt("id_usuario");
                int idPintura = rs.getInt("id_pintura");
                String nombrePintura = rs.getString("nombre_pintura");
                String archivoCifrado = rs.getString("archivo_cifrado");

                // Crear un objeto Pintura y agregarlo a la lista
                Pintura pintura = new Pintura(idUsuario, idPintura, nombrePintura, archivoCifrado);
                pinturas.add(pintura);
            }
        }
        return pinturas;
    }

    // Obtener llave envuelta por id_pintura e id_juez
    public String obtenerLlaveEnvuelta(int idPintura, int idJuez) throws SQLException {
        String query = "SELECT llave_envuelta FROM LlavesEnvueltas WHERE id_pintura = ? AND id_juez = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idPintura);
            stmt.setInt(2, idJuez);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("llave_envuelta");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al obtener la llave envuelta: " + e.getMessage());
        }
        return null; // Si no se encuentra
    }

    // Registrar una evaluación
    public void registrarEvaluacion(int idPintura, int idJuez, String calificacion, String comentario) throws SQLException {
        if (evaluacionExistente(idPintura, idJuez)) return;

        String query = "INSERT INTO Evaluaciones (id_pintura, id_juez, calificacion, comentario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idPintura);
            stmt.setInt(2, idJuez);
            stmt.setString(3, calificacion);
            stmt.setString(4, comentario);
            stmt.executeUpdate();
        }
    }

    // Verificar si ya existe una evaluación para una pintura realizada por un juez
    public boolean evaluacionExistente(int idPintura, int idJuez) throws SQLException {
        String query = "SELECT COUNT(*) FROM Evaluaciones WHERE id_pintura = ? AND id_juez = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idPintura);
            stmt.setInt(2, idJuez);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Retorna true si existe al menos una evaluación
                }
            }
        }
        return false; // No existe la evaluación
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

    // Obtener ID del usuario por credenciales
    public int obtenerIdUsuario(String usuario, String contrasena) throws SQLException {
        String query = "SELECT id_usuario, contrasena FROM Usuarios WHERE usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, usuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("contrasena");
                    
                    if (BCrypt.checkpw(contrasena, storedPassword)) {
                        return rs.getInt("id_usuario");
                    }
                }
            }
        }
        return -1;
    }

    // Obtener tipo de usuario por ID
    public String obtenerTipoUsuario(int idUsuario) throws SQLException {
        String query = "SELECT tipo_usuario FROM Usuarios WHERE id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("tipo_usuario");
                }
            }
        }
        return null;
    }

    // Verificar si el usuario ya firmó el acuerdo de confidencialidad
    public boolean verificarAcuerdo(int idUsuario) throws SQLException {
        String query = "SELECT COUNT(*) AS total FROM Acuerdos WHERE id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            } catch (SQLException e) {
                System.err.println("No se pudo ejecutar la consulta");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println("No se pudo preparar la consulta");
            e.printStackTrace();
        }
        return false;
    }

    // Registrar acuerdo de confidencialidad
    public boolean registrarAcuerdo(int idUsuario, String mensaje, String firma) throws SQLException {
        String query = "INSERT INTO Acuerdos (id_usuario, mensaje, firma) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUsuario);
            stmt.setString(2, mensaje);
            stmt.setString(3, firma);
            stmt.executeUpdate();
            return true;
        }
    }

    public List<Integer> obtenerIdsJueces() throws SQLException {
        List<Integer> idsJueces = new ArrayList<>();
        String query = "SELECT id_usuario FROM Usuarios WHERE tipo_usuario = 'JUEZ'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                idsJueces.add(rs.getInt("id_usuario"));
            }
        }
        return idsJueces;
    }

    public boolean registrarLlaveEnvuelta(int idPintura, int idJuez, String llaveEnvuelta) throws SQLException {
        String query = "INSERT INTO LlavesEnvueltas (id_pintura, id_juez, llave_envuelta) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idPintura);
            stmt.setInt(2, idJuez);
            stmt.setString(3, llaveEnvuelta);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            return false;
        }
    }

    public int obtenerIdPinturaPorNombre(String nombrePintura, int idUsuario) throws SQLException {
        String query = "SELECT id_pintura FROM Pinturas WHERE nombre_pintura = ? AND id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nombrePintura);
            stmt.setInt(2, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_pintura");
                }
            }
        }
        return -1; // Si no se encuentra
    }

}
