package jaime.modelos;

public record Response<T>(Status status, T content, String createdAt) {
    public enum Status {
        OK, ERROR, BYE, TOKEN
    }
}
