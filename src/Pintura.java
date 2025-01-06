package src;

public class Pintura {
    private int idUsuario;
    private int idPintura;

    private String nombrePintura;
    private String archivoCifrado;

    // Constructor
    public Pintura(int idUsuario, int idPintura, String nombrePintura, String archivoCifrado) {
        this.idUsuario = idUsuario;
        this.idPintura = idPintura;
        this.nombrePintura = nombrePintura;
        this.archivoCifrado = archivoCifrado;
    }

    // Getters y setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdPintura() {
        return idPintura;
    }

    public void setIdPintura(int idPintura) {
        this.idPintura = idPintura;
    }

    public String getNombrePintura() {
        return nombrePintura;
    }

    public void setNombrePintura(String nombrePintura) {
        this.nombrePintura = nombrePintura;
    }

    public String getArchivoCifrado() {
        return archivoCifrado;
    }

    public void setArchivoCifrado(String archivoCifrado) {
        this.archivoCifrado = archivoCifrado;
    }
}
