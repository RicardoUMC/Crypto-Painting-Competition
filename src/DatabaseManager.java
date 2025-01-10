package src;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import src.ECDSA.FirmaECDSA;
import src.JudgeProcess.Evaluacion;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/database";
    private static final String DB_USER = "USER";
    private static final String DB_PASSWORD = "PASSWORD";

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

    // Obtener todos los usuarios
    public String obtenerUsuario(int idUsuario) throws SQLException {
        String usuario = new String();
        String query = "SELECT usuario FROM Usuarios WHERE id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario = rs.getString("usuario");
                    return usuario;
                }
            }
        }
        return usuario;
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
    public Pintura obtenerPintura(int idUsuario) throws SQLException {
        String query = "SELECT id_pintura, nombre_pintura, archivo_cifrado FROM Pinturas WHERE id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idPintura = rs.getInt("id_pintura");
                    String nombrePintura = rs.getString("nombre_pintura");
                    String archivoCifrado = rs.getString("archivo_cifrado");

                    // Crear un objeto Pintura y agregarlo a la lista
                    return new Pintura(idUsuario, idPintura, nombrePintura, archivoCifrado);
                }
            }
        }
        return null;
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

    // Obtener llave envuelta por id_pintura e id_juez
    public String obtenerLlavePresidente() throws SQLException {
        String query = "SELECT llave_publica FROM Usuarios WHERE tipo_usuario = 'PRESIDENTE'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("llave_publica");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al obtener la llave pública del presidente: " + e.getMessage());
        }
        return null;
    }

    // Registrar una evaluación
    public void registrarEvaluacion(int idPintura, int idJuez, String calificacion, String comentario)
            throws SQLException {
        if (evaluacionExistente(idPintura, idJuez))
            return;

        String query = "INSERT INTO Evaluaciones (id_pintura, id_juez, calificacion, comentario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idPintura);
            stmt.setInt(2, idJuez);
            stmt.setString(3, calificacion);
            stmt.setString(4, comentario);
            stmt.executeUpdate();
        }
    }

    public void registrarMensajeEnmascarado(int idJuez, String mensajeEnmascarado)
            throws SQLException {
        String query = "INSERT INTO FirmasCiegas (id_juez, mensaje_enmascarado) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idJuez);
            stmt.setString(2, mensajeEnmascarado);
            stmt.executeUpdate();
        }
    }

    public List<MensajeEnmascarado> obtenerMensajesEnmascarados()
            throws SQLException {
        List<MensajeEnmascarado> mensajesEnmascarados = new ArrayList<>();
        String query = "SELECT id_firma_ciega, mensaje_enmascarado from FirmasCiegas";
        try (PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int idFirmaCiega = rs.getInt("id_firma_ciega");
                String mensaje = rs.getString("mensaje_enmascarado");

                // Crear un objeto Pintura y agregarlo a la lista
                MensajeEnmascarado menajeEnmascarado = new MensajeEnmascarado(idFirmaCiega, mensaje);
                mensajesEnmascarados.add(menajeEnmascarado);
            }
        }
        return mensajesEnmascarados;
    }

    public String obtenerMensajeEnmascarado(int idJuez)
            throws SQLException {
        String query = "SELECT mensaje_enmascarado from FirmasCiegas WHERE id_juez = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idJuez);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("mensaje_enmascarado");
                }
            }
        }
        return null;
    }

    public void guardarFirmaCiega(int idFirmaCiega, String firmaCiegas)
            throws SQLException {
        String query = "UPDATE FirmasCiegas SET firma_ciega = ? WHERE id_firma_ciega = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, firmaCiegas);
            stmt.setInt(2, idFirmaCiega);
            stmt.executeUpdate();
        }
    }

    public String obtenerFirmaCiega(int idJuez) throws SQLException {
        String query = "SELECT firma_ciega FROM FirmasCiegas WHERE id_juez = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idJuez);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("firma_ciega");
                }
            }
        }
        return null;
    }

    // Obtener evaluación existente para una pintura y un juez
    public List<Evaluacion> obtenerEvaluaciones(int idJuez) throws SQLException {
        List<Evaluacion> evaluaciones = new ArrayList<>();
        String query = "SELECT id_pintura, calificacion, comentario FROM Evaluaciones WHERE id_juez = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idJuez);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idPintura = rs.getInt("id_pintura");
                    int calificacion = rs.getInt("calificacion");
                    String comentario = rs.getString("comentario");
                    evaluaciones.add(new Evaluacion(idPintura, comentario, calificacion));
                }
            }
        }
        return evaluaciones;
    }

    // Obtener evaluación existente para una pintura y un juez
    public Evaluacion obtenerEvaluacion(int idPintura, int idJuez) throws SQLException {
        String query = "SELECT calificacion, comentario FROM Evaluaciones WHERE id_pintura = ? AND id_juez = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idPintura);
            stmt.setInt(2, idJuez);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int calificacion = rs.getInt("calificacion");
                    String comentario = rs.getString("comentario");
                    return new Evaluacion(idPintura, comentario, calificacion);
                }
            }
        }
        return null; // No existe evaluación
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

    public List<FirmaECDSA> obtenerAcuerdosECDSA() {
        List<FirmaECDSA> acuerdos = new ArrayList<>();
        String query = "SELECT id_usuario, mensaje, firma FROM Acuerdos";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int idUsuario = rs.getInt("id_usuario");
                String mensaje = rs.getString("mensaje");
                String firma = rs.getString("firma");
                acuerdos.add(new FirmaECDSA(idUsuario, mensaje, firma));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener acuerdos");
            e.printStackTrace();
        }
        return acuerdos;
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

    public List<Map<String, Object>> obtenerPinturasConEvaluaciones() throws SQLException {
        List<Map<String, Object>> pinturasConCalificacion = new ArrayList<>();
        String query = """
                    SELECT p.nombre_pintura, SUM(e.calificacion) AS calificacion_total
                    FROM Pinturas p
                    INNER JOIN Evaluaciones e ON p.id_pintura = e.id_pintura
                    WHERE e.validado = TRUE
                    GROUP BY p.id_pintura, p.nombre_pintura
                    ORDER BY calificacion_total DESC;
                """;

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, Object> pinturaData = new HashMap<>();
                pinturaData.put("nombrePintura", rs.getString("nombre_pintura"));
                pinturaData.put("calificacionTotal", rs.getInt("calificacion_total"));
                pinturasConCalificacion.add(pinturaData);
            }
        }
        return pinturasConCalificacion;
    }

    public void actualizarEstadoValidado(int idJuez) throws SQLException {
        String query = "UPDATE Evaluaciones SET validado = TRUE WHERE id_juez = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idJuez);
            int filasActualizadas = stmt.executeUpdate();
            System.out.println("Estado de validado actualizado para " + filasActualizadas
                    + " evaluaciones del juez con ID: " + idJuez);
        } catch (SQLException e) {
            System.err.println("Error al actualizar el estado de validado: " + e.getMessage());
            throw e;
        }
    }
}
