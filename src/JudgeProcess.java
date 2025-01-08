package src;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;

import javafx.scene.control.TextArea;

public class JudgeProcess {
    
    private static DatabaseManager dbManager;

    public static String obtenerUsuarioJuez(int idJuez) {
        try {
            dbManager = new DatabaseManager();
            return dbManager.obtenerUsuario(idJuez);
        } catch (SQLException e) {
            System.err.println("Error al obtener el usuario del juez");
            e.printStackTrace();
            return null;
        } finally {
            if (dbManager != null) {
                try {
                    dbManager.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Subir la clave pública del juez a la base de datos.
     *
     * @param idUsuario     ID del juez.
     * @param publicKeyFile Archivo de clave pública seleccionada.
     * @return true si la clave pública se guarda correctamente, false en caso
     *         contrario.
     */
    public static boolean subirClavePublica(int idUsuario, File publicKeyFile) {
        try {
            dbManager = new DatabaseManager();

            if (publicKeyFile == null) {
                System.out.println("No se seleccionó ningún archivo.");
                return false;
            }

            // Leer contenido del archivo como texto
            String publicKeyContent = new String(Files.readAllBytes(publicKeyFile.toPath()));

            // Guardar la clave pública en la base de datos
            boolean claveGuardada = dbManager.actualizarLlavePublica(idUsuario, publicKeyContent);
            if (claveGuardada) {
                System.out.println("Clave pública guardada exitosamente.");
            } else {
                System.out.println("Error al guardar la clave pública.");
            }

            return claveGuardada;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al subir la clave pública.");
            return false;
        } finally {
            if (dbManager != null) {
                try {
                    dbManager.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static byte[] descifrarPintura(Pintura pintura, int idJuez, String privateKeyString) {
        try {
            dbManager = new DatabaseManager();

            String wrappedKey = dbManager.obtenerLlaveEnvuelta(pintura.getIdPintura(), idJuez);
            String base64AESKey = RSA.decrypt(wrappedKey, privateKeyString);
            if (base64AESKey == null) {
                System.out.println("No se pudo decifrar la clave de AES");
                return null;
            }
            System.out.println("AES Key: " + base64AESKey);
            
            SecretKey secretKey = AESGC.decodeAESKeyFromBase64(base64AESKey);
            return AESGC.decryptBase64ToByte(secretKey, pintura.getArchivoCifrado());
        } catch (SQLException e) {
            System.err.println("Error al obtener llave envuelta");
            e.printStackTrace();
            return null; 
        } catch (Exception e) {
            System.err.println("Error al obtener archivo cifrado");
            e.printStackTrace();
            return null; 
        }
    }

    public static boolean subirMensaje(int idJuez, String mensaje) {
        try {
            dbManager = new DatabaseManager();

            String llavePresidenteBase64 = dbManager.obtenerLlavePresidente();
            String juezUsuario = dbManager.obtenerUsuario(idJuez);
            PublicKey presidentePublicKey = RSA.getPublicKeyFromBase64(llavePresidenteBase64);
            BigInteger mensajeEnmascarado = generarMensajeEnmascarado(mensaje, presidentePublicKey,
                juezUsuario.concat("_factor_r.txt"));
            
            byte[] unsignedBytes = BlindSignature.toUnsignedByteArray(mensajeEnmascarado);
            String mensajeEnmascaradoBase64Encoded = Base64.getEncoder().encodeToString(unsignedBytes);
            dbManager.registrarMensajeEnmascarado(idJuez, mensajeEnmascaradoBase64Encoded);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar mensaje cegado");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Error al enmascarar el mensaje para firmado a ciegas");
            e.printStackTrace();
            return false;
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
    }
    public static void subirEvaluacion(int idPintura, int idJuez, String calificacion, String comentario) {
        try {
            dbManager = new DatabaseManager();
            dbManager.registrarEvaluacion(idPintura, idJuez, calificacion, comentario);
        } catch (SQLException e) {
            System.err.println("Error al registrar evaluaciones");
            e.printStackTrace();
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
    }

    public static Evaluacion cargarEvaluacion(int idPintura, int idJuez) {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            return dbManager.obtenerEvaluacion(idPintura, idJuez);
        } catch (SQLException e) {
            System.err.println("Error al cargar evaluación");
            e.printStackTrace();
            return null;
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
    }

    public static List<Evaluacion> obtenerEvaluaciones(int idJuez) {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            return dbManager.obtenerEvaluaciones(idJuez);
        } catch (SQLException e) {
            System.err.println("Error al obtener evaluaciones");
            e.printStackTrace();
            return null;
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
    }

    // Generar mensaje enmascarado
    public static BigInteger generarMensajeEnmascarado(String mensaje, PublicKey clavePublica, String archivoR) throws Exception {
        // Generar factor aleatorio r
        SecureRandom random = new SecureRandom();
        BigInteger n = BlindSignature.obtenerModulo(clavePublica);
        BigInteger r = new BigInteger(n.bitLength(), random);
        r = r.mod(n); // Asegurar que r < n

        // Guardar r en archivo codificado en Base64
        try (FileOutputStream fos = new FileOutputStream(archivoR)) {
            byte[] unsignedBytes = BlindSignature.toUnsignedByteArray(r);
            fos.write(Base64.getEncoder().encode(unsignedBytes));
        }

        // Enmascarar el mensaje
        return BlindSignature.enmascararMensaje(mensaje, r, clavePublica);
    }

    public static boolean verificarFirmaCiega(int idPintura, int idJuez, String mensajeOriginal, File archivoR) {
        try {
            if (archivoR == null) {
                System.out.println("No se seleccionó ningún archivo.");
                return false;
            }

            dbManager = new DatabaseManager();
            String llavePresidenteBase64Encoded = dbManager.obtenerLlavePresidente();
            PublicKey llavePresidente = RSA.getPublicKeyFromBase64(llavePresidenteBase64Encoded);
            // BigInteger llavePresidente = new BigInteger(1, Base64.getDecoder().decode(llavePresidenteBase64Encoded));

            // Leer contenido del archivo como texto
            String rBase64Encoded = new String(Files.readAllBytes(archivoR.toPath()));
            BigInteger r = new BigInteger(1, Base64.getDecoder().decode(rBase64Encoded));

            // Obtener la firma ciega en Base64 desde la base de datos
            String firmaCiegaBase64Encoded = dbManager.obtenerFirmaCiega(idPintura, idJuez);
            if (firmaCiegaBase64Encoded == null) {
                System.err.println("No se encontró firma ciega para la evaluación.");
                return false;
            }

            // Convertir la firma en Base64 a BigInteger
            BigInteger firmaCiega = new BigInteger(1, Base64.getDecoder().decode(firmaCiegaBase64Encoded));

            // Desenmascarar la firma utilizando el factor r
            BigInteger firmaDesenmascarada = BlindSignature.desenmascararFirma(firmaCiega, r, llavePresidente);

            // Verificar la firma desenmascarada con el mensaje original
            boolean esValida = BlindSignature.verificarFirma(firmaDesenmascarada, mensajeOriginal, llavePresidente);

            if (esValida) {
                System.out.println("Firma ciega verificada correctamente.");
            } else {
                System.err.println("La firma ciega no es válida.");
            }

            return esValida;
        } catch (Exception e) {
            System.err.println("Error al verificar la firma ciega.");
            e.printStackTrace();
            return false;
        } finally {
            if (dbManager != null) {
                try {
                    dbManager.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Clase auxiliar para almacenar evaluaciones
    protected static class Evaluacion {
        private final int idPintura;
        private final TextArea comentario;
        private final String comentarioString;
        private final int[] rating;

        public Evaluacion(int idPintura, TextArea comentario, int[] rating) {
            this.idPintura = idPintura;
            this.comentario = comentario;
            this.comentarioString = null;
            this.rating = rating;
        }

        public Evaluacion(int idPintura, String comentarioString, int calificacion) {
            this.idPintura = idPintura;
            this.comentario = null;
            this.comentarioString = comentarioString;
            this.rating = new int[] { calificacion };
        }

        public int getIdPintura() {
            return idPintura;
        }

        public TextArea getComentario() {
            return comentario;
        }

        public String getComentarioTexto() {
            return comentarioString;
        }

        public int[] getRating() {
            return rating;
        }
    }
}
