package src;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;

public class ECDSA {

    // Función para leer los bytes de un archivo
    public static byte[] readFileBytes(String filename) throws IOException {
        return Files.readAllBytes(new File(filename).toPath());
    }

    // Función para generar el par de claves ECDSA y guardarlas en archivos
    public static void generateAndSaveKeyPair(String privateKeyFile, String publicKeyFile) throws Exception {
        // Generar el par de claves
        KeyPair keyPair = generateKeyPair();

        // Obtener las claves en Base64
        String base64PrivateKey = encodeKeyToBase64(keyPair.getPrivate());
        String base64PublicKey = encodeKeyToBase64(keyPair.getPublic());
        System.out.println("Private key:" + base64PrivateKey);

        System.out.println("Private key:" + base64PublicKey);
        // esto iria a la base de datos y se quitarian los archivos
        saveKeyToFile(base64PrivateKey, privateKeyFile);
        saveKeyToFile(base64PublicKey, publicKeyFile);
    }

    // Función para generar un par de claves ECDSA utilizando la curva P-256
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp384r1"); // Usamos la curva P-256
        keyPairGenerator.initialize(ecSpec, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    // Función para codificar una clave en Base64
    public static String encodeKeyToBase64(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // Función para guardar una clave en un archivo de texto
    public static void saveKeyToFile(String base64Key, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(base64Key);
        }
    }

    public static PublicKey loadPublicKeyFromBase64(String filename) throws Exception {
        // Leer el archivo que contiene la clave pública en formato Base64

        // aqui se obtendria de la base datos
        String base64Key = readFileAsString(filename);

        // Decodificar la clave desde Base64 a su forma binaria
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);

        // Crear el KeySpec a partir de la clave decodificada
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePublic(spec);
    }

    private static String readFileAsString(String filename) throws IOException {
        File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    private static BigInteger[] loadRSFromHexFile(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        BigInteger r = null, s = null;
        String line;

        while ((line = br.readLine()) != null) {
            if (line.startsWith("r: ")) {
                r = new BigInteger(line.substring(3), 16); // Convertir el valor de r a BigInteger
            } else if (line.startsWith("s: ")) {
                s = new BigInteger(line.substring(3), 16); // Convertir el valor de s a BigInteger
            }
        }
        br.close();

        if (r == null || s == null) {
            throw new IOException("No se pudieron leer los valores r y s del archivo.");
        }

        return new BigInteger[] { r, s };
    }

    private static boolean verifySignature(PublicKey publicKey, byte[] message, BigInteger r, BigInteger s)
            throws Exception {
        Signature ecdsa = Signature.getInstance("SHA256withECDSA");

        // Inicializar la verificación con la clave pública
        ecdsa.initVerify(publicKey);

        // Proveer el mensaje a verificar
        ecdsa.update(message);

        // Construir la firma combinando r y s en un formato DER
        byte[] derSignature = encodeRSInDER(r, s);

        // Verificar la firma
        return ecdsa.verify(derSignature);
    }

    private static byte[] encodeRSInDER(BigInteger r, BigInteger s) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        // Tag de secuencia ASN.1
        outStream.write(0x30);

        // Crear un ByteArrayOutputStream temporal para r y s
        ByteArrayOutputStream rsStream = new ByteArrayOutputStream();

        // Codificar r
        byte[] rBytes = r.toByteArray();
        rsStream.write(0x02); // Tag para entero
        rsStream.write(rBytes.length);
        rsStream.write(rBytes);

        // Codificar s
        byte[] sBytes = s.toByteArray();
        rsStream.write(0x02); // Tag para entero
        rsStream.write(sBytes.length);
        rsStream.write(sBytes);

        // Escribir la longitud total de r y s en el outStream
        byte[] rsBytes = rsStream.toByteArray();
        outStream.write(rsBytes.length);
        outStream.write(rsBytes);

        return outStream.toByteArray();
    }

    public static PrivateKey loadPrivateKeyFromBase64(String filename) throws Exception {
        // Leer el archivo que contiene la clave privada en formato Base64

        // aqui se obtendria de base 64 y se quitaria la funcion readFilaAsString
        String base64Key = readFileAsString(filename);

        // Decodificar la clave desde Base64 a su forma binaria
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);

        // Crear el KeySpec a partir de la clave decodificada
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(spec);
    }

    public static void saveSignatureToFile(BigInteger r, BigInteger s, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("r: " + r.toString(16) + "\n");
            writer.write("s: " + s.toString(16) + "\n");
        }
    }

    public static BigInteger[] decodeRSFromDER(byte[] signature) throws IOException {
        ByteArrayInputStream inStream = new ByteArrayInputStream(signature);
        inStream.read();

        inStream.read();

        // Leer 'r'
        inStream.read(); // Leer el tag 0x02 que indica que es un entero
        int rLength = inStream.read(); // Leer la longitud del valor 'r'
        byte[] rBytes = new byte[rLength];
        inStream.read(rBytes); // Leer el valor 'r'
        BigInteger r = new BigInteger(1, rBytes); // BigInteger positivo

        // Leer 's'
        inStream.read(); // Leer el tag 0x02 que indica que es un entero
        int sLength = inStream.read(); // Leer la longitud del valor 's'
        byte[] sBytes = new byte[sLength];
        inStream.read(sBytes); // Leer el valor 's'
        BigInteger s = new BigInteger(1, sBytes); // BigInteger positivo

        return new BigInteger[] { r, s };
    }

}
