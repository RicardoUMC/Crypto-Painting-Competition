package src;

import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.engines.RSABlindedEngine;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class BlindSignature {

    // Generar el hash del mensaje
    public static BigInteger hashMessage(String mensaje) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(mensaje.getBytes());
        System.out.println("Hash de mensaje: " + Base64.getEncoder().encodeToString(hash));
        return new BigInteger(1, hash); // Convertir a BigInteger positivo
    }

    // Convertir PublicKey de java.security a RSAKeyParameters de Bouncy Castle
    private static RSAKeyParameters convertToBouncyCastlePublicKey(PublicKey publicKey) {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        return new RSAKeyParameters(false, rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());
    }

    // Convertir PrivateKey de java.security a RSAPrivateCrtKeyParameters de Bouncy
    // Castle
    private static RSAPrivateCrtKeyParameters convertToBouncyCastlePrivateKey(PrivateKey privateKey) {
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
        return new RSAPrivateCrtKeyParameters(
                rsaPrivateKey.getModulus(),
                null, // Exponente público no es necesario para operaciones privadas
                rsaPrivateKey.getPrivateExponent(),
                null, null, null, null, null);
    }

    // Obtener el módulo (n) de una clave pública
    public static BigInteger obtenerModulo(PublicKey clavePublica) throws Exception {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) clavePublica;
        return rsaPublicKey.getModulus();
    }
    
    // Obtener el exponente público (e) de una clave pública
    public static BigInteger obtenerExponente(PublicKey clavePublica) throws Exception {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) clavePublica;
        return rsaPublicKey.getPublicExponent();
    }

    // Enmascarar el mensaje
    public static BigInteger enmascararMensaje(String mensaje, BigInteger r, PublicKey publicKey) throws Exception {
        RSAKeyParameters bcPublicKey = convertToBouncyCastlePublicKey(publicKey);

        BigInteger m = hashMessage(mensaje);
        BigInteger n = bcPublicKey.getModulus();
        BigInteger e = bcPublicKey.getExponent();

        m = m.mod(n);

        // Enmascarar el mensaje: M' = (M * r^e) mod n
        BigInteger result = m.multiply(r.modPow(e, n)).mod(n);

        // Validar que el tamaño de M' sea menor a n
        if (result.compareTo(n) >= 0) {
            throw new IllegalArgumentException("El mensaje enmascarado excede el tamaño permitido.");
        }

        byte[] unsignedBytes = toUnsignedByteArray(result);
        System.out.println("Resultado M': " + Base64.getEncoder().encodeToString(unsignedBytes) + " (" + unsignedBytes.length + " bytes)");
        return result;
    }

    // Firmar el mensaje enmascarado
    public static String firmarMensaje(String mensajeEnmascarado, PrivateKey privateKey) throws Exception {
        RSAPrivateCrtKeyParameters bcPrivateKey = convertToBouncyCastlePrivateKey(privateKey);

        // Crear el motor de cifrado RSA
        RSABlindedEngine rsaEngine = new RSABlindedEngine();
        rsaEngine.init(true, bcPrivateKey);
        
        // Convertir el mensaje a bytes y firmar
        byte[] mensajeBytes = Base64.getDecoder().decode(mensajeEnmascarado);
        byte[] firmaBytes = rsaEngine.processBlock(mensajeBytes, 0, mensajeBytes.length);

        String firma = Base64.getEncoder().encodeToString(firmaBytes);

        return firma;
    }

    // Desenmascarar la firma
    public static BigInteger desenmascararFirma(BigInteger firmaEnmascarada, BigInteger r, PublicKey publicKey)
            throws Exception {
        RSAKeyParameters bcPublicKey = convertToBouncyCastlePublicKey(publicKey);
        BigInteger n = bcPublicKey.getModulus();

        // Inverso modular de r
        BigInteger rInverso = r.modInverse(n);

        // Desenmascarar la firma: S = (S' * r^-1) mod n
        return firmaEnmascarada.multiply(rInverso).mod(n);
    }

    // Verificar la firma
    public static boolean verificarFirma(BigInteger firma, String mensaje, PublicKey publicKey) throws Exception {
        RSAKeyParameters bcPublicKey = convertToBouncyCastlePublicKey(publicKey);

        // Crear el motor de cifrado RSA para verificación
        RSABlindedEngine rsaEngine = new RSABlindedEngine();
        rsaEngine.init(false, bcPublicKey);

        BigInteger mensajeHash = hashMessage(mensaje);

        byte[] firmaBytes = toUnsignedByteArray(firma);
        byte[] mensajeVerificadoBytes = rsaEngine.processBlock(firmaBytes, 0, firmaBytes.length);
        
        // Convertir los bytes del mensaje verificado a BigInteger
        BigInteger mensajeVerificado = new BigInteger(1, mensajeVerificadoBytes);
        
        byte[] mensajeBytes = toUnsignedByteArray(mensajeHash);
        byte[] mensajeVBytes = toUnsignedByteArray(mensajeVerificado);
        System.out.println("Resultado M: " + Base64.getEncoder().encodeToString(mensajeBytes));
        System.out.println("Resultado M': " + Base64.getEncoder().encodeToString(mensajeVBytes));
        return mensajeHash.equals(mensajeVerificado);
    }

    // Convertir un BigInteger a un array de bytes sin el byte de signo extra
    public static byte[] toUnsignedByteArray(BigInteger bigInteger) {
        byte[] byteArray = bigInteger.toByteArray();

        // Si el primer byte es 0 y el tamaño es mayor al tamaño esperado, elimina el
        // byte de signo
        if (byteArray.length > 1 && byteArray[0] == 0) {
            byte[] trimmedArray = new byte[byteArray.length - 1];
            System.arraycopy(byteArray, 1, trimmedArray, 0, trimmedArray.length);
            return trimmedArray;
        }

        return byteArray;
    }

}
