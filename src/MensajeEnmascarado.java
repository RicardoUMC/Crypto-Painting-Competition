package src;

public class MensajeEnmascarado {
    private int idFirmaCiega;
    private String mensajeEnmascarado;

    public MensajeEnmascarado(int idFirmaCiega, String mensajeEnmascarado) {
        this.idFirmaCiega = idFirmaCiega;
        this.mensajeEnmascarado = mensajeEnmascarado;
    }

    public int getIdFirmaCiega() {
        return idFirmaCiega;
    }

    public void setIdFirmaCiega(int idFirmaCiega) {
        this.idFirmaCiega = idFirmaCiega;
    }

    public String getMensajeEnmascarado() {
        return mensajeEnmascarado;
    }

    public void setMensajeEnmascarado(String mensajeEnmascarado) {
        this.mensajeEnmascarado = mensajeEnmascarado;
    }
}
