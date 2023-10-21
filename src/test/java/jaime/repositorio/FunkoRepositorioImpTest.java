package jaime.repositorio;

import jaime.modelos.Funko;
import jaime.modelos.Tipos;
import jaime.servicios.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class FunkoRepositorioImpTest {
private FunkoRepositorio funkorepositorio;
    @BeforeEach
    void setUp() throws SQLException {
        funkorepositorio = FunkoRepositorioImp.getInstance(DatabaseManager.getInstance());
        DatabaseManager.getInstance().initTables();
    }
    @Test
    void findAll() {
        Funko funko1 = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Stitch")
                .tipo(Tipos.DISNEY)
                .precio(5.99)
                .fecha_cre(LocalDate.now())
                .myID(1L)
                .build();
        Funko funko2 = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Ojo de halcon")
                .tipo(Tipos.MARVEL)
                .precio(7.99)
                .fecha_cre(LocalDate.now())
                .myID(2L)
                .build();
        funkorepositorio.save(funko1).block();
        funkorepositorio.save(funko2).block();
        List<Funko> foundFunko=funkorepositorio.findAll().collectList().block();
        assertEquals(2, foundFunko.size());
    }

    @Test
    void findById() {
        Funko funko = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Stitch")
                .tipo(Tipos.DISNEY)
                .precio(5.99)
                .fecha_cre(LocalDate.now())
                .myID(1L)
                .build();
        Funko savedFunko=funkorepositorio.save(funko).block();
        Optional<Funko> foundFunko = funkorepositorio.findById(savedFunko.myID()).blockOptional();
        assertAll(
                () -> assertEquals(funko.cod(), foundFunko.get().cod()),
                () -> assertEquals(funko.nombre(), foundFunko.get().nombre()),
                () -> assertEquals(funko.tipo(), foundFunko.get().tipo()),
                () -> assertEquals(funko.precio(),foundFunko.get().precio()),
                () -> assertEquals(funko.fecha_cre(), foundFunko.get().fecha_cre()),
                () -> assertEquals(funko.myID(), foundFunko.get().myID())
        );
    }
    @Test
    void findAlumnoByIdNoExiste() {
        Optional<Funko> foundFunko = funkorepositorio.findById(1L).blockOptional();
        assertAll(() -> assertFalse(foundFunko.isPresent())
        );
    }

    @Test
    void save() {
        Funko funko = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Stitch")
                .tipo(Tipos.DISNEY)
                .precio(5.99)
                .fecha_cre(LocalDate.now())
                .myID(1L)
                .build();
        Funko savedFunko=funkorepositorio.save(funko).block();
        assertAll(() -> assertNotNull(savedFunko),
                () -> assertEquals(funko.cod(), savedFunko.cod()),
                () -> assertEquals(funko.nombre(), savedFunko.nombre()),
                () -> assertEquals(funko.tipo(), savedFunko.tipo()),
                () -> assertEquals(funko.precio(), savedFunko.precio()),
                () -> assertEquals(funko.fecha_cre(), savedFunko.fecha_cre()),
                () -> assertEquals(funko.myID(), savedFunko.myID())
        );
    }
    @Test
    void deleteById() {
        Funko funko = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Stitch")
                .tipo(Tipos.DISNEY)
                .precio(5.99)
                .fecha_cre(LocalDate.now())
                .myID(1L)
                .build();
        Funko savedFunko = funkorepositorio.save(funko).block();
        funkorepositorio.deleteById(savedFunko.myID()).block();
        Optional<Funko> foundAlumno = funkorepositorio.findById(savedFunko.myID()).blockOptional();
        assertAll(() -> assertFalse(foundAlumno.isPresent())
        );
    }

    @Test
    void deleteAll() {
        Funko funko = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Stitch")
                .tipo(Tipos.DISNEY)
                .precio(5.99)
                .fecha_cre(LocalDate.now())
                .myID(1L)
                .build();
        Funko funko2 = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Ojo de halcon")
                .tipo(Tipos.MARVEL)
                .precio(7.99)
                .fecha_cre(LocalDate.now())
                .myID(2L)
                .build();
        funkorepositorio.save(funko).block();
        funkorepositorio.save(funko2).block();
        funkorepositorio.deleteAll().block();
        List<Funko> foundFunko=funkorepositorio.findAll().collectList().block();
        assertEquals(0, foundFunko.size());
    }

    @Test
    void findByNombre() {
        Funko funko1 = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Stitch")
                .tipo(Tipos.DISNEY)
                .precio(5.99)
                .fecha_cre(LocalDate.now())
                .myID(1L)
                .build();
        Funko funko2 = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Ojo de halcon")
                .tipo(Tipos.MARVEL)
                .precio(7.99)
                .fecha_cre(LocalDate.now())
                .myID(2L)
                .build();
        funkorepositorio.save(funko1).block();
        funkorepositorio.save(funko2).block();
        List<Funko> foundFunko=funkorepositorio.findByNombre("Stitch").collectList().block();
        assertAll(() -> assertNotNull(foundFunko),
                () -> assertEquals(funko1.cod(), foundFunko.get(0).cod()),
                () -> assertEquals(funko1.nombre(), foundFunko.get(0).nombre()),
                () -> assertEquals(funko1.tipo(), foundFunko.get(0).tipo()),
                () -> assertEquals(funko1.precio(),foundFunko.get(0).precio()),
                () -> assertEquals(funko1.fecha_cre(), foundFunko.get(0).fecha_cre()),
                () -> assertEquals(funko1.myID(), foundFunko.get(0).myID())
        );
    }

    @Test
    void funkoCaro() {
        Funko funko1 = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Stitch")
                .tipo(Tipos.DISNEY)
                .precio(5.99)
                .fecha_cre(LocalDate.now())
                .myID(1L)
                .build();
        Funko funko2 = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Ojo de halcon")
                .tipo(Tipos.MARVEL)
                .precio(7.99)
                .fecha_cre(LocalDate.now())
                .myID(2L)
                .build();
        funkorepositorio.save(funko1).block();
        funkorepositorio.save(funko2).block();
        Optional<Funko> foundFunko = funkorepositorio.funkoCaro().blockOptional();
        assertAll(
                () -> assertEquals(funko2.cod(), foundFunko.get().cod()),
                () -> assertEquals(funko2.nombre(), foundFunko.get().nombre()),
                () -> assertEquals(funko2.tipo(), foundFunko.get().tipo()),
                () -> assertEquals(funko2.precio(),foundFunko.get().precio()),
                () -> assertEquals(funko2.fecha_cre(), foundFunko.get().fecha_cre()),
                () -> assertEquals(funko2.myID(), foundFunko.get().myID())
        );
    }
    @Test
    void fecha2023() {
        LocalDate fecha = LocalDate.of(2022, 4, 22);
        Funko funko1 = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Stitch")
                .tipo(Tipos.DISNEY)
                .precio(5.99)
                .fecha_cre(fecha)
                .myID(1L)
                .build();
        Funko funko2 = Funko.builder()
                .cod(UUID.randomUUID())
                .nombre("Ojo de halcon")
                .tipo(Tipos.MARVEL)
                .precio(7.99)
                .fecha_cre(LocalDate.now())
                .myID(2L)
                .build();
        funkorepositorio.save(funko1).block();
        funkorepositorio.save(funko2).block();
        List<Funko> foundFunko = funkorepositorio.fecha2023().collectList().block();
        assertAll(() -> assertNotNull(foundFunko),
                () -> assertEquals(funko2.cod(), foundFunko.get(0).cod()),
                () -> assertEquals(funko2.nombre(), foundFunko.get(0).nombre()),
                () -> assertEquals(funko2.tipo(), foundFunko.get(0).tipo()),
                () -> assertEquals(funko2.precio(),foundFunko.get(0).precio()),
                () -> assertEquals(funko2.fecha_cre(), foundFunko.get(0).fecha_cre()),
                () -> assertEquals(funko2.myID(), foundFunko.get(0).myID())
        );
    }
}