package jaime.servicios;

import jaime.modelos.Funko;
import jaime.modelos.Notificacion;
import reactor.core.publisher.Flux;

public interface FunkoNotificacion {
    Flux<Notificacion<Funko>> getNotificationAsFlux();

    void notify(Notificacion<Funko> notificacion);
}
