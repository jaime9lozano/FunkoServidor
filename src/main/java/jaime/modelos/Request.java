package jaime.modelos;

public record Request<T>(Type type, T content,String token ,String createdAt) {
    public enum Type {
        LOGIN, FUNKOCARO, FUNKOS2023, FUNKOSSTITCH, SALIR, OTRO
    }
}
