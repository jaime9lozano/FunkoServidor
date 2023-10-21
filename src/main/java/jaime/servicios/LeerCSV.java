package jaime.servicios;

import jaime.modelos.Funko;
import jaime.modelos.MyIDStore;
import jaime.modelos.Tipos;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;


public class LeerCSV {
    private static LeerCSV instance;
    MyIDStore m = MyIDStore.getInstance();
    private LeerCSV() {
    }
    public static LeerCSV getInstance() {
        if(instance==null){
            instance=new LeerCSV();
        }
        return instance;
    }
    private String csv() {
        Path currentRelativePath = Paths.get("");
        String ruta = currentRelativePath.toAbsolutePath().toString();
        String dir = ruta + File.separator + "data";
        return dir + File.separator + "funkos.csv";
    }
    private String json() {
        Path currentRelativePath = Paths.get("");
        String ruta = currentRelativePath.toAbsolutePath().toString();
        String dir = ruta + File.separator + "data";
        return dir + File.separator + "funkos.json";
    }
    public Flux<Funko> leerCsv() {
        return Flux.defer(() -> {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(csv()));
                return Flux.fromStream(reader.lines()
                                .skip(1)
                                .map(linea -> linea.split(","))
                                .map(valores -> new Funko(
                                        UUID.fromString(valores[0].substring(0,35)),
                                        valores[1],
                                        Tipos.valueOf(valores[2]),
                                        Double.parseDouble(valores[3]),
                                        LocalDate.parse(valores[4]),
                                        m.addandgetID()
                                )))
                        .doFinally(signalType -> {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            } catch (IOException e) {
                return Flux.error(e);
            }
        });
    }
    private Mono<Void> crearJson() {
        Flux<Funko> funkoFlux = leerCsv();
        String jsonFilePath = json();
        return funkoFlux
                .flatMap(this::convertirFunkoToJson)
                .collectList()
                .flatMapMany(Flux::fromIterable)  // Convierte la lista en un Flux
                .collectList()
                .flatMap(jsonList -> writeJsonToFile(Flux.fromIterable(jsonList), jsonFilePath));
    }
    private Mono<String> convertirFunkoToJson(Funko funko) {
        return Mono.fromCallable(() -> {
            // Suponiendo una estructura específica del JSON, ajústala según tus necesidades
            String json = "{" +"\n"+
                    "\"uuid\":\"" + funko.cod() + "\"," +"\n"+
                    "\"nombre\":\"" + funko.nombre() + "\"," +"\n"+
                    "\"tipo\":\"" + funko.tipo() + "\"," +"\n"+
                    "\"precio\":" + funko.precio() + "," +"\n"+
                    "\"fecha_cre\":\"" + funko.fecha_cre() + "\"," +"\n"+
                    "\"myID\":" + funko.myID() +"\n"+
                    "},"+"\n";
            return json;
        });
    }
    private Mono<Void> writeJsonToFile(Flux<String> jsonFlux, String jsonFilePath) {
        return Mono.fromRunnable(() -> {
            try (FileWriter fileWriter = new FileWriter(jsonFilePath)) {
                fileWriter.write("[");
                jsonFlux.subscribe(
                        json -> {
                            try {
                                fileWriter.write(json);
                            } catch (IOException e) {
                                throw new RuntimeException("Error al escribir el JSON en el archivo: " + jsonFilePath, e);
                            }
                        },
                        error -> {
                            // Manejar errores aquí si es necesario
                        },
                        () -> {
                            // Cerrar el archivo y eliminar la coma extra al final
                            try {
                                fileWriter.write("]");
                                fileWriter.flush();
                            } catch (IOException e) {
                                throw new RuntimeException("Error al escribir el JSON en el archivo: " + jsonFilePath, e);
                            }
                        }
                );
            } catch (IOException e) {
                throw new RuntimeException("Error al escribir el JSON en el archivo: " + jsonFilePath, e);
            }
        });
    }
}
