package src;

public class MensajeEnmascarado {
    private int idEvaluacion;
    private String mensajeEnmascarado;

    public MensajeEnmascarado(int idEvaluacion, String mensajeEnmascarado) {
        this.idEvaluacion = idEvaluacion;
        this.mensajeEnmascarado = mensajeEnmascarado;
    }

    public int getIdEvaluacion() {
        return idEvaluacion;
    }

    public void setIdEvaluacion(int idEvaluacion) {
        this.idEvaluacion = idEvaluacion;
    }

    public String getMensajeEnmascarado() {
        return mensajeEnmascarado;
    }

    public void setMensajeEnmascarado(String mensajeEnmascarado) {
        this.mensajeEnmascarado = mensajeEnmascarado;
    }
}
