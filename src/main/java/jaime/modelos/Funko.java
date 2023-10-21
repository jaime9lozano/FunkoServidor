package jaime.modelos;

import lombok.Builder;


import java.time.LocalDate;
import java.util.UUID;
@Builder
public record Funko(UUID cod, String nombre, Tipos tipo, Double precio, LocalDate fecha_cre, Long myID) {

}
