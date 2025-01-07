package src;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class BlindSignature {

    // Generar el hash del mensaje
    public static byte[] hashMessage(byte[] mensaje) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(mensaje);
    }
    
    public static BigInteger hashMessage(String mensaje) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(mensaje.getBytes());
        return new BigInteger(1, hash); // Convertir a BigInteger positivo
    }

    // Obtener el módulo (n) de una clave pública
    public static BigInteger obtenerModulo(PublicKey clavePublica) throws Exception {
        return ((RSAPublicKey) clavePublica).getModulus();
    }

    // Obtener el exponente público (e) de una clave pública
    public static BigInteger obtenerExponente(PublicKey clavePublica) throws Exception {
        return ((RSAPublicKey) clavePublica).getPublicExponent();
    }

    // Enmascarar el mensaje
    public static BigInteger enmascararMensaje(String mensaje, BigInteger r, BigInteger e, BigInteger n) throws Exception {
        BigInteger m = hashMessage(mensaje); // Hash del mensaje

        // Enmascarar el mensaje: M' = (M * r^e) mod n
        return m.multiply(r.modPow(e, n)).mod(n);
    }

    // Firmar el mensaje enmascarado
    public static String firmarMensaje(String mensajeEnmascarado, PrivateKey clavePrivada) throws Exception {
        byte[] hashBytes = hashMessage(Base64.getDecoder().decode(mensajeEnmascarado));

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, clavePrivada);

        byte[] firmaBytes = cipher.doFinal(hashBytes);
        return Base64.getEncoder().encodeToString(firmaBytes);
    }

    // Desenmascarar la firma
    public static BigInteger desenmascararFirma(BigInteger firmaEnmascarada, BigInteger r, PublicKey clavePublica)
            throws Exception {
        BigInteger n = obtenerModulo(clavePublica); // Módulo

        // Inverso modular de r
        BigInteger rInverso = r.modInverse(n);

        // Desenmascarar la firma: S = (S' * r^-1) mod n
        return firmaEnmascarada.multiply(rInverso).mod(n);
    }

    // Verificar la firma
    public static boolean verificarFirma(BigInteger firma, String mensaje, PublicKey clavePublica) throws Exception {
        BigInteger m = hashMessage(mensaje); // Hash del mensaje
        BigInteger e = obtenerExponente(clavePublica); // Exponente público
        BigInteger n = obtenerModulo(clavePublica); // Módulo

        // Verificar: M == (S^e) mod n
        BigInteger mVerificado = firma.modPow(e, n);
        return m.equals(mVerificado);
    }
}
