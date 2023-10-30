package jaime.modelos;

public record Request<T>(Type type, T content,String token ,String createdAt) {
    public enum Type {
        LOGIN, ID, NUEVO, ACTUALIZAR, BORRAR,FUNKOCARO, FUNKOS2023, FUNKOSSTITCH, SALIR, OTRO
    }
}
