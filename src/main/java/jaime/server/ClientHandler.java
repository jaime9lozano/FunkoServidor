package jaime.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jaime.modelos.*;
import jaime.repositorio.FunkoRepositorioImp;
import jaime.repositorio.UsersRepository;
import jaime.servicios.DatabaseManager;
import jaime.servicios.FunkoNotificacionImp;
import jaime.servicios.FunkoServicioImp;
import jaime.servicios.TokenService;
import jaime.util.LocalDateAdapter;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public class ClientHandler extends Thread{
    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    private final long clientNumber;
    BufferedReader in;
    PrintWriter out;
    FunkoServicioImp funkoServicio = FunkoServicioImp.getInstance(
            FunkoRepositorioImp.getInstance(DatabaseManager.getInstance()),
            FunkoNotificacionImp.getInstance()
    );
    public ClientHandler(Socket socket, long clientNumber) {
        this.clientSocket = socket;
        this.clientNumber = clientNumber;
    }
    public void run() {
        try {

            openConnection();

            String clientInput;
            Request request;

            // Cuidado con lo de salir!!!!
            while (true) {
                clientInput = in.readLine();
                logger.debug("Petición recibida en bruto: " + clientInput);
                request = gson.fromJson(clientInput, Request.class);
                handleRequest(request);
            }

        } catch (IOException e) {
            logger.error("Error: " + e.getMessage());
        }catch (ServerException ex) {
            // Concentramos todos los errores aquí
            out.println(gson.toJson(new Response<>(Response.Status.ERROR, ex.getMessage(), LocalDateTime.now().toString())));
        }
    }

    private void closeConnection() throws IOException {
        logger.debug("Cerrando la conexión con el cliente: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
        out.close();
        in.close();
        clientSocket.close();
    }

    private void openConnection() throws IOException {
        logger.debug("Conectando con el cliente nº: " + clientNumber + " : " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }
    @SuppressWarnings("unchecked")
    private void handleRequest(Request<?> request) throws IOException, ServerException {
        // Procesamos la petición y devolvemos la respuesta, esto puede ser un método
        switch (request.type()) {
            case LOGIN -> processLogin((Request<Login>) request);
            case FUNKOCARO -> processFunkoCaro((Request<String>) request);
            case FUNKOS2023 -> processFunkos2023((Request<String>) request);
            case FUNKOSSTITCH -> processFunkosStitch((Request<String>) request);
            case ID -> processFunkoID((Request<String>) request);
            case NUEVO -> processFunkoNuevo((Request<String>) request);
            case ACTUALIZAR -> processFunkoActualizar((Request<String>) request);
            case BORRAR -> processFunkoBorrar((Request<String>) request);
            case SALIR -> processSalir();
            default ->
                    out.println(gson.toJson(new Response<>(Response.Status.ERROR, "No tengo ni idea", LocalDateTime.now().toString())));
        }
    }
    private void processFunkosStitch(Request<String> request) throws ServerException {
        logger.debug("Petición de funkos de stitch recibida: " + request);
        // Para el UUID solo vamos a comporbar que el token esté activo
        // y que el usuario sea admin
        var token = request.token();
        if (TokenService.getInstance().verifyToken(token, Server.TOKEN_SECRET)) {
            logger.debug("Token válido");
            List<Funko> funkosList = funkoServicio.findAllByNombre("Stitch")
                    .collectList()
                    .block();

            logger.debug("Respuesta enviada: " + funkosList);
            var resJson = gson.toJson(funkosList); // contenido
            out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString())));
        } else {
            logger.warn("Token no válido");
            throw new ServerException("Token no válido");
        }
    }
    private void processSalir() throws IOException {
        out.println(gson.toJson(new Response<>(Response.Status.BYE, "Adios", LocalDateTime.now().toString())));
        closeConnection();
    }

    private void processFunkos2023(Request<String> request) throws ServerException {
        logger.debug("Petición de funkos en 2023 recibida: " + request);
        // Para el UUID solo vamos a comporbar que el token esté activo
        // y que el usuario sea admin
        var token = request.token();
        if (TokenService.getInstance().verifyToken(token, Server.TOKEN_SECRET)) {
            logger.debug("Token válido");
            List<Funko> funkosList = funkoServicio.fecha2023()
                    .collectList() // Convierte el Flux en una Lista de Funkos
                    .block();   // Espera a que termine la operación y obtiene el resultado

            logger.debug("Respuesta enviada: " + funkosList);
            var resJson = gson.toJson(funkosList); // contenido
            out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString())));
        } else {
            logger.warn("Token no válido");
            throw new ServerException("Token no válido");
        }
    }
    private void processFunkoCaro(Request<String> request) throws ServerException {
        logger.debug("Petición de funko mas caro recibida: " + request);
        // Para la fecha solo vamos a comporbar que el token esté activo
        // Si no lo está, no se procesa la petición
        var token = request.token();
        if (TokenService.getInstance().verifyToken(token, Server.TOKEN_SECRET)) {
            logger.debug("Token válido");
            funkoServicio.funkoCaro()
                    .subscribe(funko -> {
                        logger.debug("Respuesta enviada: " + funko);
                        var resJson = gson.toJson(funko); // contenido
                        out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString())));
                    });
        } else {
            logger.warn("Token no válido");
            throw new ServerException("Token no valido o caducado");
        }
    }
    private void processFunkoID(Request<String> request) throws ServerException {
        Long id= Long.valueOf(request.content());
        logger.debug("Petición de funko con id: " + request);
        // Para la fecha solo vamos a comporbar que el token esté activo
        // Si no lo está, no se procesa la petición
        var token = request.token();
        if (TokenService.getInstance().verifyToken(token, Server.TOKEN_SECRET)) {
            logger.debug("Token válido");
            funkoServicio.findById(id)
                    .subscribe(funko -> {
                        logger.debug("Respuesta enviada: " + funko);
                        var resJson = gson.toJson(funko);
                        out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString()))); // Respuesta
                    });
        } else {
            logger.warn("Token no válido");
            throw new ServerException("Token no valido o caducado");
        }
    }
    private void processFunkoNuevo(Request<String> request) throws ServerException {
        logger.debug("Petición de funko nuevo: " + request);

        // Obtener el token y el objeto Funko del request
        var token = request.token();
        Funko funko = gson.fromJson(request.content(), Funko.class);

        if (TokenService.getInstance().verifyToken(token, Server.TOKEN_SECRET)) {
            logger.debug("Token válido");
            funkoServicio.save(funko)
                    .subscribe(funko1 -> {
                        logger.debug("Respuesta enviada: " + funko1);
                        var resJson = gson.toJson(funko1);
                        out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString()))); // Respuesta
                    });

        } else {
            logger.warn("Token no válido");
            throw new ServerException("Token no válido o caducado");
        }
    }

    private void processFunkoActualizar(Request<String> request) throws ServerException {
        logger.debug("Petición de funko actualizar: " + request);
        // Para la fecha solo vamos a comporbar que el token esté activo
        // Si no lo está, no se procesa la petición
        var token = request.token();
        Funko funko = gson.fromJson(request.content(), Funko.class);
        if (TokenService.getInstance().verifyToken(token, Server.TOKEN_SECRET)) {
            logger.debug("Token válido");
            funkoServicio.update(funko)
                    .subscribe(funko1 -> {
                        logger.debug("Respuesta enviada: " + funko1);
                        var resJson = gson.toJson(funko1);
                        out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString()))); // Respuesta
                    });
        } else {
            logger.warn("Token no válido");
            throw new ServerException("Token no valido o caducado");
        }
    }

    private void processFunkoBorrar(Request<String> request) throws ServerException {
        logger.debug("Petición de funkos borrar: " + request);
        // Para el UUID solo vamos a comporbar que el token esté activo
        // y que el usuario sea admin
        var token = request.token();
        Long id1= Long.valueOf(request.content());
        if (TokenService.getInstance().verifyToken(token, Server.TOKEN_SECRET)) {
            logger.debug("Token válido");
            var claims = TokenService.getInstance().getClaims(token, Server.TOKEN_SECRET);
            var id = claims.get("userid").asInt();
            var user = UsersRepository.getInstance().findByById(id);
            if (user.isPresent() && user.get().role().equals(User.Role.ADMIN)) {
                logger.debug("Usuario válido y admin procesamos la petición");
                funkoServicio.deleteById(id1)
                        .subscribe(funkos -> {
                            logger.debug("Respuesta enviada: " + funkos);
                            var resJson = gson.toJson(funkos); // contenido
                            out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString()))); // Respuesta
                        });
            } else {
                logger.warn("Usuario no válido");
                throw new ServerException("Usuario no autorizado para esta acción");
            }

        } else {
            logger.warn("Token no válido");
            throw new ServerException("Token no válido");
        }
    }
    private void processLogin(Request<Login> request) throws ServerException {
        logger.debug("Petición de login recibida: " + request);
        // Aquí procesamos el login es un dato anidado!!! Descomponemos la petición
        Login login = gson.fromJson(String.valueOf(request.content()), new TypeToken<Login>() {
        }.getType());
        // existe el usuario??
        // System.out.println(login);
        var user = UsersRepository.getInstance().findByByUsername(login.username());
        if (user.isEmpty() || !BCrypt.checkpw(login.password(), user.get().password())) {
            logger.warn("Usuario no encontrado o falla la contraseña");
            throw new ServerException("Usuario o contraseña incorrectos");
        }
        // Creamos el token
        var token = TokenService.getInstance().createToken(user.get(), Server.TOKEN_SECRET, Server.TOKEN_EXPIRATION);
        // Enviamos la respuesta
        logger.debug("Respuesta enviada: " + token);
        out.println(gson.toJson(new Response<>(Response.Status.TOKEN, token, LocalDateTime.now().toString())));

    }
}
