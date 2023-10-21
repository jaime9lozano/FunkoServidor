package jaime.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jaime.modelos.Request;
import jaime.modelos.Response;
import jaime.repositorio.FunkoRepositorioImp;
import jaime.servicios.DatabaseManager;
import jaime.servicios.FunkoNotificacionImp;
import jaime.servicios.FunkoServicioImp;
import jaime.util.LocalDateAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;


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
    private void handleRequest(Request<String> request) throws IOException {
        // Procesamos la petición y devolvemos la respuesta, esto puede ser un método
        switch (request.type()) {
            case LOGIN ->
                    out.println(gson.toJson(new Response<>(Response.Status.OK, "Bienvenido", LocalDateTime.now().toString())));
            case FUNKOCARO -> {
                funkoServicio.funkoCaro()
                        .subscribe(funko -> {
                            logger.debug("Respuesta enviada: " + funko);
                            var resJson = gson.toJson(funko); // contenido
                            out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString()))); // Respuesta
                        });
            }
            case FUNKOS2023 -> {
                funkoServicio.fecha2023()
                        .subscribe(funkos -> {
                            logger.debug("Respuesta enviada: " + funkos);
                            var resJson = gson.toJson(funkos); // contenido
                            out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString()))); // Respuesta
                        });
            }
            case FUNKOSSTITCH -> {
                funkoServicio.findAllByNombre("Stitch")
                        .subscribe(funkos -> {
                            logger.debug("Respuesta enviada: " + funkos);
                            var resJson = gson.toJson(funkos); // contenido
                            out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString()))); // Respuesta
                        });
            }
            case SALIR -> {
                out.println(gson.toJson(new Response<>(Response.Status.BYE, "Adios", LocalDateTime.now().toString())));
                closeConnection();
            }
            default ->
                    out.println(gson.toJson(new Response<>(Response.Status.ERROR, "No tengo ni idea", LocalDateTime.now().toString())));
        }
    }
}
