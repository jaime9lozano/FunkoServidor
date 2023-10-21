package jaime.servicios;

import jaime.modelos.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FunkoServicio {
    Flux<Funko> findAll();

    Flux<Funko> findAllByNombre(String nombre);

    Mono<Funko> findById(long id);

    Mono<Funko> save(Funko funko);

    Mono<Funko> update(Funko funko);

    Mono<Funko> deleteById(long id);

    Mono<Void> deleteAll();
}
