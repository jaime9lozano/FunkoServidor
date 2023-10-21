package jaime.repositorio;

import jaime.modelos.Funko;
import jaime.modelos.Tipos;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface FunkoRepositorio extends CrudRepositorio<Funko, Long>{
    Flux<Funko> findByNombre(String nombre);
    Mono<Funko> funkoCaro();
    Mono<Double> mediaFunko();
    Flux<Map<Tipos, Integer>> agrupModelo();
   Flux<Funko> fecha2023();
}
