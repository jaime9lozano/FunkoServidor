package jaime.repositorio;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Result;
import jaime.modelos.Funko;
import jaime.modelos.Tipos;
import jaime.servicios.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FunkoRepositorioImp implements FunkoRepositorio{
    private final Logger logger = LoggerFactory.getLogger(FunkoRepositorioImp.class);
    private static FunkoRepositorioImp instance;
    private final ConnectionPool connectionFactory;
    private FunkoRepositorioImp(DatabaseManager databaseManager) {
        this.connectionFactory = databaseManager.getConnectionPool();
    }

    public static FunkoRepositorioImp getInstance(DatabaseManager db) {
        if (instance == null) {
            instance = new FunkoRepositorioImp(db);
        }
        return instance;
    }
    @Override
    public Flux<Funko> findAll() {
        logger.debug("Buscando todos los funkos");
        String sql = "SELECT * FROM FUNKOS";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql).execute())
                        .flatMap(result -> result.map((row, rowMetadata) ->
                                Funko.builder()
                                        .cod(row.get("cod", UUID.class))
                                        .nombre(row.get("nombre", String.class))
                                        .tipo(Tipos.valueOf(row.get("modelo", String.class)))
                                        .precio(row.get("precio", Double.class))
                                        .fecha_cre(row.get("fecha_lanzamiento", LocalDate.class))
                                        .myID(row.get("MyID", Long.class))
                                        .build()
                        )),
                Connection::close
        );
    }

    @Override
    public Mono<Funko> findById(Long id) {
        logger.debug("Buscando funko por id: " + id);
        String sql = "SELECT * FROM FUNKOS WHERE id = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, id)
                        .execute()
                ).flatMap(result -> Mono.from(result.map((row, rowMetadata) ->
                        Funko.builder()
                                .cod(row.get("cod", UUID.class))
                                .nombre(row.get("nombre", String.class))
                                .tipo(Tipos.valueOf(row.get("modelo", String.class)))
                                .precio(row.get("precio", Double.class))
                                .fecha_cre(row.get("fecha_lanzamiento", LocalDate.class))
                                .myID(row.get("MyID", Long.class))
                                .build()
                ))),
                Connection::close
        );
    }

    @Override
    public Mono<Funko> save(Funko funko) {
        logger.debug("Guardando funko: " + funko);
        String sql = "INSERT INTO FUNKOS (cod,nombre,modelo,precio,fecha_lanzamiento,MyID,created_at,updated_at) VALUES (?, ?, ?, ?, ?,?,?,?)";
        LocalDate hoy = LocalDate.now();
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, funko.cod())
                        .bind(1, funko.nombre())
                        .bind(2, funko.tipo().toString())
                        .bind(3, funko.precio())
                        .bind(4, funko.fecha_cre())
                        .bind(5,funko.myID())
                        .bind(6,funko.fecha_cre())
                        .bind(7,hoy)
                        .execute()
                ).then(Mono.just(funko)), // Aquí devolvemos el objeto 'alumno' después de la inserción
                Connection::close
        );
    }

    @Override
    public Mono<Funko> update(Funko funko) {
        logger.debug("Actualizando funko: " + funko);
        String query = "UPDATE FUNKOS SET nombre = ?, modelo = ?, precio = ?,updated_at = ? WHERE MyID = ?";
        LocalDate hoy = LocalDate.now();
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(query)
                        .bind(0, funko.nombre())
                        .bind(1, funko.tipo().toString())
                        .bind(2, funko.precio())
                        .bind(3,hoy)
                        .bind(4,funko.myID())
                        .execute()
                ).then(Mono.just(funko)), // Aquí devolvemos el objeto 'alumno' después de la actualización
                Connection::close
        );
    }

    @Override
    public Mono<Boolean> deleteById(Long id) {
        logger.debug("Borrando funko por id: " + id);
        String sql = "DELETE FROM FUNKOS WHERE id = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                                .bind(0, id)
                                .execute()
                        ).flatMapMany(Result::getRowsUpdated)
                        .hasElements(),
                Connection::close
        );
    }

    @Override
    public Mono<Void> deleteAll() {
        logger.debug("Borrando todos los funkos");
        String sql = "DELETE FROM FUNKOS";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .execute()
                ).then(),
                Connection::close
        );
    }

    @Override
    public Flux<Funko> findByNombre(String nombre) {
        logger.debug("Buscando todos los funkos por nombre");
        String sql = "SELECT * FROM FUNKOS WHERE nombre LIKE ?";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql)
                        .bind(0, "%" + nombre + "%")
                        .execute()
                ).flatMap(result -> result.map((row, rowMetadata) ->
                        Funko.builder()
                                .cod(row.get("cod", UUID.class))
                                .nombre(row.get("nombre", String.class))
                                .tipo(Tipos.valueOf(row.get("modelo", String.class)))
                                .precio(row.get("precio", Double.class))
                                .fecha_cre(row.get("fecha_lanzamiento", LocalDate.class))
                                .myID(row.get("MyID", Long.class))
                                .build()
                )),
                Connection::close
        );
    }
    @Override
    public Mono<Funko> funkoCaro(){
        logger.debug("Buscando el funko mas caro");
        String sql = "SELECT * FROM FUNKOS ORDER BY precio DESC LIMIT 1";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .execute()
                ).flatMap(result -> Mono.from(result.map((row, rowMetadata) ->
                        Funko.builder()
                                .cod(row.get("cod", UUID.class))
                                .nombre(row.get("nombre", String.class))
                                .tipo(Tipos.valueOf(row.get("modelo", String.class)))
                                .precio(row.get("precio", Double.class))
                                .fecha_cre(row.get("fecha_lanzamiento", LocalDate.class))
                                .myID(row.get("MyID", Long.class))
                                .build()
                ))),
                Connection::close
        );
    }

    @Override
    public Mono<Double> mediaFunko() {
        logger.debug("Buscando la media del precio");
        String sql = "SELECT AVG(precio) AS media_precios FROM FUNKOS";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .execute()
                ).flatMap(result -> Mono.from(result.map((row, rowMetadata) ->
                        row.get("media_precios", Double.class)
                ))),
                Connection::close
        );
    }

    @Override
    public Flux<Map<Tipos, Integer>> agrupModelo() {
        logger.debug("Buscando los funkos agrupados por modelos");
        String sql = "SELECT modelo,count(*) as cuenta FROM FUNKOS GROUP BY modelo ORDER BY modelo";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql)
                        .execute()
                ).flatMap(result -> result.map((row, rowMetadata) -> {
                    String modelo = row.get("modelo", String.class);
                    int cuenta = Integer.parseInt(row.get("cuenta", String.class));
                    Tipos tipoEnum = Tipos.valueOf(modelo);
                    Map<Tipos, Integer> resultMap = new HashMap<>();
                    resultMap.put(tipoEnum, cuenta);
                    return resultMap;
                })),
                Connection::close
        );
    }

    @Override
    public Flux<Funko> fecha2023() {
        logger.debug("Buscando todos los funkos lanzados en 2023");
        String sql = "SELECT * FROM FUNKOS WHERE YEAR(fecha_lanzamiento) = 2023";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql)
                        .execute()
                ).flatMap(result -> result.map((row, rowMetadata) ->
                        Funko.builder()
                                .cod(row.get("cod", UUID.class))
                                .nombre(row.get("nombre", String.class))
                                .tipo(Tipos.valueOf(row.get("modelo", String.class)))
                                .precio(row.get("precio", Double.class))
                                .fecha_cre(row.get("fecha_lanzamiento", LocalDate.class))
                                .myID(row.get("MyID", Long.class))
                                .build()
                )),
                Connection::close
        );
    }

}
