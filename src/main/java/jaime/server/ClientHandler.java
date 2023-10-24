package jaime.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jaime.modelos.Login;
import jaime.modelos.Request;
import jaime.modelos.Response;
import jaime.modelos.User;
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

import static jaime.modelos.Request.Type.LOGIN;


public class ClientHandler {
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
            Request<String> request;

            // Cuidado con lo de salir!!!!
            while (true) {
                clientInput = in.readLine();
                request = gson.fromJson(clientInput, Request.class);
                handleRequest(request);
            }

        } catch (IOException e) {
            logger.error("Error: " + e.getMessage());
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
    private void handleRequest(Request<?> request) throws IOException {
        // Procesamos la petición y devolvemos la respuesta, esto puede ser un método
        switch (request.type()) {
            case LOGIN -> processLogin((Request<Login>) request);
            case FUNKOCARO -> processFunkoCaro((Request<String>) request);
            case FUNKOS2023 -> processFunkos2023((Request<String>) request);
            case FUNKOSSTITCH -> processFunkosStitch((Request<String>) request);
            case SALIR -> processSalir();
            default ->
                    out.println(gson.toJson(new Response<>(Response.Status.ERROR, "No tengo ni idea", LocalDateTime.now().toString())));
        }
    }
    private void processFunkosStitch(Request<String> request) {
        logger.debug("Petición de funkos de stitch recibida: " + request);
        // Para el UUID solo vamos a comporbar que el token esté activo
        // y que el usuario sea admin
        var token = request.token();
        if (TokenService.getInstance().verifyToken(token, Server.TOKEN_SECRET)) {
            logger.debug("Token válido");
            var claims = TokenService.getInstance().getClaims(token, Server.TOKEN_SECRET);
            var id = claims.get("userid").asInt(); // es verdad que podríamos obtener otro tipo de datos
            var user = UsersRepository.getInstance().findByById(id);
            if (user.isPresent() && user.get().role().equals(User.Role.ADMIN)) {
                logger.debug("Usuario válido y admin procesamos la petición");
                funkoServicio.findAllByNombre("Stitch")
                        .subscribe(funkos -> {
                            logger.debug("Respuesta enviada: " + funkos);
                            var resJson = gson.toJson(funkos); // contenido
                            out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString()))); // Respuesta
                        });
            } else {
                logger.warn("Usuario no válido");
                out.println(gson.toJson(new Response<>(Response.Status.ERROR, "Usuario no válido o no tiene permisos", LocalDateTime.now().toString())));
            }

        } else {
            logger.warn("Token no válido");
            out.println(gson.toJson(new Response<>(Response.Status.ERROR, "Token no válido o caducado", LocalDateTime.now().toString())));
        }
    }
    private void processSalir() throws IOException {
        out.println(gson.toJson(new Response<>(Response.Status.BYE, "Adios", LocalDateTime.now().toString())));
        closeConnection();
    }

    private void processFunkos2023(Request<String> request){
        logger.debug("Petición de funkos en 2023 recibida: " + request);
        // Para el UUID solo vamos a comporbar que el token esté activo
        // y que el usuario sea admin
        var token = request.token();
        if (TokenService.getInstance().verifyToken(token, Server.TOKEN_SECRET)) {
            logger.debug("Token válido");
            var claims = TokenService.getInstance().getClaims(token, Server.TOKEN_SECRET);
            var id = claims.get("userid").asInt(); // es verdad que podríamos obtener otro tipo de datos
            var user = UsersRepository.getInstance().findByById(id);
            if (user.isPresent() && user.get().role().equals(User.Role.ADMIN)) {
                logger.debug("Usuario válido y admin procesamos la petición");
                funkoServicio.fecha2023()
                        .subscribe(funkos -> {
                            logger.debug("Respuesta enviada: " + funkos);
                            var resJson = gson.toJson(funkos); // contenido
                            out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString()))); // Respuesta
                        });
            } else {
                logger.warn("Usuario no válido");
                out.println(gson.toJson(new Response<>(Response.Status.ERROR, "Usuario no válido o no tiene permisos", LocalDateTime.now().toString())));
            }

        } else {
            logger.warn("Token no válido");
            out.println(gson.toJson(new Response<>(Response.Status.ERROR, "Token no válido o caducado", LocalDateTime.now().toString())));
        }
    }
    private void processFunkoCaro(Request<String> request){
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
                        out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString()))); // Respuesta
                    });
        } else {
            logger.warn("Token no válido");
            out.println(gson.toJson(new Response<>(Response.Status.ERROR, "Token no válido o caducado", LocalDateTime.now().toString())));
        }
    }
    private void processLogin(Request<Login> request) {
        logger.debug("Petición de login recibida: " + request);
        // Aquí procesamos el login es un dato anidado!!! Descomponemos la petición
        Login login = gson.fromJson(String.valueOf(request.content()), new TypeToken<Login>() {
        }.getType());
        // existe el usuario??
        var user = UsersRepository.getInstance().findByByUsername(login.username());
        if (user.isPresent()) {
            var canLogin = BCrypt.checkpw(login.password(), user.get().password());
            if (canLogin) {
                // Creamos el token
                var token = TokenService.getInstance().createToken(user.get(), Server.TOKEN_SECRET, Server.TOKEN_EXPIRATION);
                // Enviamos la respuesta
                logger.debug("Respuesta enviada: " + token);
                out.println(gson.toJson(new Response<>(Response.Status.TOKEN, token, LocalDateTime.now().toString())));
            } else {
                logger.warn("Usuario o contraseña incorrectos");
                out.println(gson.toJson(new Response<>(Response.Status.ERROR, "Usuario o contraseña incorrectos", LocalDateTime.now().toString())));
            }
        } else {
            logger.warn("Usuario no encontrado");
            out.println(gson.toJson(new Response<>(Response.Status.ERROR, "Usuario no encontrado", LocalDateTime.now().toString())));
        }

    }
}
