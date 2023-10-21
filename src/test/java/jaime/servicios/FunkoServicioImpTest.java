package jaime.servicios;

import jaime.excepciones.FunkoNoEncontrado;
import jaime.modelos.Funko;
import jaime.modelos.Tipos;
import jaime.repositorio.FunkoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FunkoServicioImpTest {
    @Mock
    FunkoRepositorio repository;

    @Mock
    FunkoNotificacion notification;

    @InjectMocks
    FunkoServicioImp service;
    @Test
    void findAll() throws SQLException, ExecutionException, InterruptedException{
        var funkos = List.of(
                Funko.builder()
                        .cod(UUID.randomUUID())
                        .nombre("Stitch")
                        .tipo(Tipos.DISNEY)
                        .precio(5.99)
                        .fecha_cre(LocalDate.now())
                        .myID(1L)
                        .build(),
                Funko.builder()
                        .cod(UUID.randomUUID())
                        .nombre("Ojo de halcon")
                        .tipo(Tipos.MARVEL)
                        .precio(7.99)
                        .fecha_cre(LocalDate.now())
                        .myID(2L)
                        .build()
        );

        when(repository.findAll()).thenReturn(Flux.fromIterable(funkos));

        var result = service.findAll().collectList().block();

        assertAll("findAll",
                () -> assertEquals(result.size(), 2, "No se han recuperado dos funkos"),
                () -> assertEquals(result.get(0).cod(), funkos.get(0).cod(), "El primer funko no es el esperado"),
                () -> assertEquals(result.get(1).cod(),funkos.get(1).cod(), "El segundo funko no es el esperado"),
                () -> assertEquals(result.get(0).tipo(), funkos.get(0).tipo(), "El tipo del primer funko no es el esperado"),
                () -> assertEquals(result.get(1).tipo(),funkos.get(1).tipo(), "El tipo del segundo funko no es el esperado"),
                () -> assertEquals(result.get(0).precio(),funkos.get(0).precio(), "El precio del primer funko no es el esperado"),
                () -> assertEquals(result.get(1).precio(),funkos.get(1).precio(), "El precio del primer funko no es el esperado")
        );

        verify(repository, times(1)).findAll();
    }

    @Test
    void findAllByNombre()  throws SQLException, ExecutionException, InterruptedException{
        var funkos = List.of(
                Funko.builder()
                        .cod(UUID.randomUUID())
                        .nombre("Stitch")
                        .tipo(Tipos.DISNEY)
                        .precio(5.99)
                        .fecha_cre(LocalDate.now())
                        .myID(1L)
                        .build()
        );

        when(repository.findByNombre("Stitch")).thenReturn(Flux.fromIterable(funkos));

        var result = service.findAllByNombre("Stitch").collectList().block();

        assertAll("findAllByNombre",
                () -> assertEquals(result.size(), 1, "No se esperaba ningun funko"),
                () -> assertEquals(result.get(0).cod(), funkos.get(0).cod(), "El funko no es el esperado"),
                () -> assertEquals(result.get(0).tipo(), funkos.get(0).tipo(), "El tipo del funko no es el esperado"),
                () -> assertEquals(result.get(0).precio(),funkos.get(0).precio(), "El precio del funko no es el esperado")
        );

        verify(repository, times(1)).findByNombre("Stitch");
    }

    @Test
    void findById() throws SQLException, ExecutionException, InterruptedException{
        var funkos = Funko.builder()
                        .cod(UUID.randomUUID())
                        .nombre("Stitch")
                        .tipo(Tipos.DISNEY)
                        .precio(5.99)
                        .fecha_cre(LocalDate.now())
                        .myID(1L)
                        .build();

        when(repository.findById(1L)).thenReturn(Mono.just(funkos));

        var result = service.findById(1L).blockOptional();

        assertAll("findById",
                () -> assertEquals(result.get().cod(), funkos.cod(), "El funko no es el esperado"),
                () -> assertEquals(result.get().tipo(), funkos.tipo(), "El tipo del funko no es el esperado"),
                () -> assertEquals(result.get().precio(),funkos.precio(), "El precio del funko no es el esperado")
        );

        verify(repository, times(1)).findById(1L);
    }
    @Test
    void save() {
        var funkos = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Stitch")
                .tipo(Tipos.DISNEY)
                .precio(5.99)
                .fecha_cre(LocalDate.now())
                .myID(1L)
                .build();

        when(repository.findById(funkos.myID())).thenReturn(Mono.just(funkos));
        when(repository.save(funkos)).thenReturn(Mono.just(funkos));

        var result = service.save(funkos).block();

        assertAll("save",
                () -> assertEquals(result.cod(), funkos.cod(), "El funko no es el esperado"),
                () -> assertEquals(result.tipo(), funkos.tipo(), "El tipo del funko no es el esperado"),
                () -> assertEquals(result.precio(), funkos.precio(), "El precio del funko no es el esperado")
        );

        verify(repository, times(1)).save(funkos);
    }

    @Test
    void update() throws SQLException, FunkoNoEncontrado, ExecutionException, InterruptedException{
        var funkos = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Stitch")
                .tipo(Tipos.DISNEY)
                .precio(5.99)
                .fecha_cre(LocalDate.now())
                .myID(1L)
                .build();

        when(repository.findById(funkos.myID())).thenReturn(Mono.just(funkos));
        when(repository.update(funkos)).thenReturn(Mono.just(funkos));

        var result = service.update(funkos).block();

        assertAll("update",
                () -> assertEquals(result.cod(), funkos.cod(), "El funko no es el esperado"),
                () -> assertEquals(result.tipo(), funkos.tipo(), "El tipo del funko no es el esperado"),
                () -> assertEquals(result.precio(), funkos.precio(), "El precio del funko no es el esperado")
        );
    }

    @Test
    void deleteById() throws SQLException, ExecutionException, InterruptedException{
        var funkos = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Stitch")
                .tipo(Tipos.DISNEY)
                .precio(5.99)
                .fecha_cre(LocalDate.now())
                .myID(1L)
                .build();

        when(repository.findById(funkos.myID())).thenReturn(Mono.just(funkos));
        when(repository.deleteById(funkos.myID())).thenReturn(Mono.just(true));

        var result = service.deleteById(1L).block();

        assertEquals(result, funkos, "El funko no es el esperado");
    }

    @Test
    void deleteAll() throws SQLException, ExecutionException, InterruptedException{
        var funkos = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Stitch")
                .tipo(Tipos.DISNEY)
                .precio(5.99)
                .fecha_cre(LocalDate.now())
                .myID(1L)
                .build();

        when(repository.deleteAll()).thenReturn(Mono.empty());

        service.deleteAll().block();

        verify(repository, times(1)).deleteAll();
    }

    @Test
    void funkoCaro() {
        var funkos = List.of(
                Funko.builder()
                        .cod(UUID.randomUUID())
                        .nombre("Stitch")
                        .tipo(Tipos.DISNEY)
                        .precio(5.99)
                        .fecha_cre(LocalDate.now())
                        .myID(1L)
                        .build(),
                Funko.builder()
                        .cod(UUID.randomUUID())
                        .nombre("Ojo de halcon")
                        .tipo(Tipos.MARVEL)
                        .precio(7.99)
                        .fecha_cre(LocalDate.now())
                        .myID(2L)
                        .build()
        );

        when(repository.funkoCaro()).thenReturn(Mono.just(funkos.get(1)));

        var result = service.funkoCaro().blockOptional();

        assertAll("FunkoCaro",
                () -> assertEquals(result.get().cod(), funkos.get(1).cod(), "El primer funko no es el esperado"),
                () -> assertEquals(result.get().nombre(), funkos.get(1).nombre(), "El tipo del primer funko no es el esperado"),
                () -> assertEquals(result.get().tipo(),funkos.get(1).tipo(), "El tipo del segundo funko no es el esperado"),
                () -> assertEquals(result.get().precio(),funkos.get(1).precio(), "El precio del primer funko no es el esperado")

        );
    }

    @Test
    void fecha2023() {
        LocalDate fecha = LocalDate.of(2022, 4, 22);
        var funkos = List.of(
                Funko.builder()
                        .cod(UUID.randomUUID())
                        .nombre("Stitch")
                        .tipo(Tipos.DISNEY)
                        .precio(5.99)
                        .fecha_cre(LocalDate.now())
                        .myID(1L)
                        .build(),
                Funko.builder()
                        .cod(UUID.randomUUID())
                        .nombre("Ojo de halcon")
                        .tipo(Tipos.MARVEL)
                        .precio(7.99)
                        .fecha_cre(fecha)
                        .myID(2L)
                        .build()
        );

        when(repository.fecha2023()).thenReturn(Flux.fromIterable(funkos));

        var result = service.fecha2023().collectList().block();

        assertAll("Funkos2023",
                () -> assertEquals(result.get(0).cod(), funkos.get(0).cod(), "El funko no es el esperado"),
                () -> assertEquals(result.get(0).nombre(), funkos.get(0).nombre(), "El nombre del funko no es el esperado"),
                () -> assertEquals(result.get(0).tipo(),funkos.get(0).tipo(), "El tipo del funko no es el esperado"),
                () -> assertEquals(result.get(0).precio(),funkos.get(0).precio(), "El precio del funko no es el esperado")

        );
    }
}