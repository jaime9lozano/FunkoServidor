package jaime.servicios;

import jaime.modelos.Funko;
import jaime.modelos.Notificacion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

public class FunkoNotificacionImp implements FunkoNotificacion{
    private static FunkoNotificacionImp INSTANCE = new FunkoNotificacionImp();

    private final Flux<Notificacion<Funko>> funkosNotificationFlux;
    // Para las notificaciones
    private FluxSink<Notificacion<Funko>> funkosNotification;
    private FunkoNotificacionImp() {
        this.funkosNotificationFlux = Flux.<Notificacion<Funko>>create(emitter -> this.funkosNotification = emitter).share();
    }
    public static FunkoNotificacionImp getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FunkoNotificacionImp();
        }
        return INSTANCE;
    }
    @Override
    public Flux<Notificacion<Funko>> getNotificationAsFlux() {
        return funkosNotificationFlux;
    }

    @Override
    public void notify(Notificacion<Funko> notificacion) {
        funkosNotification.next(notificacion);
    }
}
