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

        BigInteger m = hashMessage(mensaje); // Hash del mensaje
        BigInteger n = bcPublicKey.getModulus(); // Módulo
        BigInteger e = bcPublicKey.getExponent(); // Exponente público

        // Enmascarar el mensaje: M' = (M * r^e) mod n
        BigInteger result = m.multiply(r.modPow(e, n)).mod(n);
        System.out.println("Resultado M': " + Base64.getEncoder().encodeToString(result.toByteArray()));
        return result;
    }

    // Firmar el mensaje enmascarado
    public static String firmarMensaje(String mensajeEnmascarado, PrivateKey privateKey) throws Exception {
        RSAPrivateCrtKeyParameters bcPrivateKey = convertToBouncyCastlePrivateKey(privateKey);

        // Crear el motor de cifrado RSA
        RSABlindedEngine rsaEngine = new RSABlindedEngine();
        rsaEngine.init(true, bcPrivateKey);
        
        // Convertir el mensaje a bytes y firmar
        // byte[] mensajeBytes = mensajeEnmascarado.toByteArray();
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
        BigInteger m = hashMessage(mensaje); // Hash del mensaje
        BigInteger e = bcPublicKey.getExponent(); // Exponente público
        BigInteger n = bcPublicKey.getModulus(); // Módulo

        // Verificar: M == (S^e) mod n
        BigInteger mVerificado = firma.modPow(e, n);
        return m.equals(mVerificado);
    }
}
