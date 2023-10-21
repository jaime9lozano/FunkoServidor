package jaime.servicios;

import jaime.excepciones.FunkoNoEncontrado;
import jaime.modelos.Funko;
import jaime.modelos.Notificacion;
import jaime.modelos.Tipos;
import jaime.repositorio.FunkoRepositorio;
import jaime.repositorio.FunkoRepositorioImp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class FunkoServicioImp implements FunkoServicio{
    private static final int CACHE_SIZE = 15;
    private final Logger logger = LoggerFactory.getLogger(FunkoRepositorioImp.class);
    private static FunkoServicioImp instance;
    private final FunkoCache cache;
    private final FunkoNotificacion notificacion;
    private final FunkoRepositorio funkosRepository;
    private FunkoServicioImp(FunkoRepositorio funkosRepository, FunkoNotificacion notification) {
        this.funkosRepository = funkosRepository;
        this.cache = new FunkoCacheImp(CACHE_SIZE);
        this.notificacion = notification;

    }


    public static FunkoServicioImp getInstance(FunkoRepositorio funkosRepository, FunkoNotificacion notification) {
        if (instance == null) {
            instance = new FunkoServicioImp(funkosRepository, notification);
        }
        return instance;
    }
    @Override
    public Flux<Funko> findAll() {
        logger.debug("Buscando todos los funkos");
        return funkosRepository.findAll();
    }

    @Override
    public Flux<Funko> findAllByNombre(String nombre) {
        logger.debug("Buscando todos los funkos por nombre");
        return funkosRepository.findByNombre(nombre);
    }

    @Override
    public Mono<Funko> findById(long id) {
        logger.debug("Buscando funko por id: " + id);
        return cache.get(id)
                .switchIfEmpty(funkosRepository.findById(id)
                        .flatMap(funko -> cache.put(funko.myID(), funko)
                                .then(Mono.just(funko)))
                        .switchIfEmpty(Mono.error(new FunkoNoEncontrado("funko con id " + id + " no encontrado"))));
    }

    private Mono<Funko> saveWithoutNotification(Funko funko) {
        logger.debug("Guardando funko sin notificación: " + funko);
        return funkosRepository.save(funko)
                .flatMap(saved -> findById(saved.myID()));
    }
    @Override
    public Mono<Funko> save(Funko funko) {
        logger.debug("Guardando funko: " + funko);
        return saveWithoutNotification(funko)
                .doOnSuccess(saved -> notificacion.notify(new Notificacion<>(Notificacion.Tipo.NEW, saved)));
    }

    private Mono<Funko> updateWithoutNotification(Funko funko) {
        logger.debug("Actualizando funko sin notificación: " + funko);
        return funkosRepository.findById(funko.myID())
                .switchIfEmpty(Mono.error(new FunkoNoEncontrado("funko con id " + funko.myID() + " no encontrado")))
                .flatMap(existing -> funkosRepository.update(funko)
                        .flatMap(updated -> cache.put(updated.myID(), updated)
                                .thenReturn(updated)));
    }
    @Override
    public Mono<Funko> update(Funko funko) {
        logger.debug("Actualizando funko: " + funko);
        return updateWithoutNotification(funko)
                .doOnSuccess(updated -> notificacion.notify(new Notificacion<>(Notificacion.Tipo.UPDATED, updated)));
    }

    private Mono<Funko> deleteByIdWithoutNotification(long id) {
        logger.debug("Borrando funko sin notificación con id: " + id);
        return funkosRepository.findById(id)
                .switchIfEmpty(Mono.error(new FunkoNoEncontrado("funko con id " + id + " no encontrado")))
                .flatMap(funko -> cache.remove(funko.myID())
                        .then(funkosRepository.deleteById(funko.myID()))
                        .thenReturn(funko));
    }
    @Override
    public Mono<Funko> deleteById(long id) {
        logger.debug("Borrando funko por id: " + id);
        return deleteByIdWithoutNotification(id)
                .doOnSuccess(deleted -> notificacion.notify(new Notificacion<>(Notificacion.Tipo.DELETED, deleted)));
    }

    @Override
    public Mono<Void> deleteAll() {
        cache.clear();
        logger.debug("Borrando todos los funkos");
        return funkosRepository.deleteAll()
                .then(Mono.empty());
    }
    public Flux<Notificacion<Funko>> getNotifications() {
        return notificacion.getNotificationAsFlux();
    }
    public Mono<Funko> funkoCaro(){
        logger.debug("Buscando el funko mas caro");
        return funkosRepository.funkoCaro();
    }
    public Mono<Double> mediaFunko(){
        logger.debug("Buscando la media de los funkos");
        return funkosRepository.mediaFunko();
    }
    public Flux<Map<Tipos, Integer>> agrupModelo(){
        logger.debug("Buscando cuantos funkos hay agrupados por cada modelo");
        return funkosRepository.agrupModelo();
    }
    public Flux<Funko> fecha2023(){
        logger.debug("Buscando todos los funkos lanzados en 2023");
        return funkosRepository.fecha2023();
    }
}
