package jaime.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jaime.modelos.Login;
import jaime.modelos.Request;
import jaime.modelos.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

import static jaime.modelos.Request.Type.*;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private final Logger logger = LoggerFactory.getLogger(Client.class);
    private final Gson gson;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    String token;

    public Client() {
        gson = new Gson();
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    public void start() {
        try {
            openConnection();

            token=sendRequestLogin();
            sendRequestFunkoCaro(token);
            sendFunko2023(token);
            sendFunkoStitch(token);
            sendRequestSalir();

        } catch (IOException e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    private void closeConnection() throws IOException {
        logger.debug("Cerrando la conexión con el servidor: " + HOST + ":" + PORT);
        in.close();
        out.close();
        socket.close();
    }

    private void openConnection() throws IOException {
        logger.debug("Conectando al servidor: " + HOST + ":" + PORT);
        socket = new Socket(HOST, PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private String sendRequestLogin() {
        String myToken = null;
        Request<Login> request = new Request<>(LOGIN, new Login("ana", "ana1234"), null, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + LOGIN);
        logger.debug("Petición enviada: " + request);
        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta
        try {
            Response response = gson.fromJson(in.readLine(), Response.class);
            logger.debug("Respuesta recibida: " + response.toString());
            // Ahora podríamos implementar un switch para cada tipo de respuesta
            // y hacer lo que queramos con ella...
            System.out.println("Respuesta recibida de tipo: " + response.status());

            switch (response.status()) {
                case TOKEN -> {
                    System.out.println("🟢 Mi token es: " + response.content());
                    myToken = response.content().toString();
                }
                default -> {
                    System.err.println("🔴 Error: Tipo de respuesta no esperado: " + response.content());
                    closeConnection();
                    System.exit(1);
                }
            }

            // Esperamos un poco para que se vea bien
            Thread.sleep(2000);
        } catch (IOException | InterruptedException e) {
            logger.error("Error: " + e.getMessage());
        }
        return myToken;
    }
    private void sendRequestFunkoCaro(String token) {
        // Al usar el toString me ahorro el problem ade las fechas con Gson
        // El request es del tipo del content!!
        Request<String> request = new Request<>(FUNKOCARO, null, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + FUNKOCARO);
        logger.debug("Petición enviada: " + request);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta
        try {
            Response<String> response = gson.fromJson(in.readLine(), new TypeToken<Response<String>>() {
            }.getType());
            logger.debug("Respuesta recibida: " + response.toString());
            // Ahora podríamos implementar un switch para cada tipo de respuesta
            // y hacer lo que queramos con ella...
            System.out.println("Respuesta recibida de tipo: " + response.status());

            switch (response.status()) {
                case OK -> System.out.println("🟢 El funko mas caro es: " + response.content());
                case ERROR -> System.err.println("🔴 Error: " + response.content());
                default -> {
                    System.err.println("🔴 Error: Tipo de respuesta no esperado: " + response.content());
                    closeConnection();
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            logger.error("Error: " + e.getMessage());
        }
    }
    private void sendFunko2023(String token) {

        // Al usar el toString me ahorro el problem ade las fechas con Gson
        Request<String> request = new Request<>(FUNKOS2023, null, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + FUNKOS2023);
        logger.debug("Petición enviada: " + request);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta
        try {
            Response<String> response = gson.fromJson(in.readLine(), new TypeToken<Response<String>>() {
            }.getType());
            logger.debug("Respuesta recibida: " + response.toString());
            // Ahora podríamos implementar un switch para cada tipo de respuesta
            // y hacer lo que queramos con ella...
            System.out.println("Respuesta recibida de tipo: " + response.status());

            switch (response.status()) {
                case OK -> System.out.println("🟢 Los funkos del 2023 son: " + response.content());
                case ERROR -> System.err.println("🔴 Error: " + response.content());
                default -> {
                    System.err.println("🔴 Error: Tipo de respuesta no esperado: " + response.content());
                    closeConnection();
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            logger.error("Error: " + e.getMessage());
        }
    }
    private void sendFunkoStitch(String token) {

        // Al usar el toString me ahorro el problem ade las fechas con Gson
        Request<String> request = new Request<>(FUNKOSSTITCH, null, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + FUNKOSSTITCH);
        logger.debug("Petición enviada: " + request);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta
        try {
            Response<String> response = gson.fromJson(in.readLine(), new TypeToken<Response<String>>() {
            }.getType());
            logger.debug("Respuesta recibida: " + response.toString());
            // Ahora podríamos implementar un switch para cada tipo de respuesta
            // y hacer lo que queramos con ella...
            System.out.println("Respuesta recibida de tipo: " + response.status());

            switch (response.status()) {
                case OK -> System.out.println("🟢 Los funkos de stitch son: " + response.content());
                case ERROR -> System.err.println("🔴 Error: " + response.content());
                default -> {
                    System.err.println("🔴 Error: Tipo de respuesta no esperado: " + response.content());
                    closeConnection();
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            logger.error("Error: " + e.getMessage());
        }
    }
    private void sendRequestSalir() {
        // Al usar el toString me ahorro el problem ade las fechas con Gson
        Request<String> request = new Request<>(SALIR, null, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + SALIR);
        logger.debug("Petición enviada: " + request);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta
        try {
            Response<String> response = gson.fromJson(in.readLine(), new TypeToken<Response<String>>() {
            }.getType());
            logger.debug("Respuesta recibida: " + response.toString());
            // Ahora podríamos implementar un switch para cada tipo de respuesta
            // y hacer lo que queramos con ella...
            System.out.println("Respuesta recibida de tipo: " + response.status());

            switch (response.status()) {
                case ERROR -> System.err.println("🔴 Error: " + response.content());
                case BYE -> {
                    System.out.println("Vamos a cerrar la conexión " + response.content());
                    closeConnection();
                }
                default -> {
                    System.err.println("🔴 Error: Tipo de respuesta no esperado: " + response.content());
                    closeConnection();
                    System.exit(1);
                }
            }

            // Esperamos un poco para que se vea bien
            Thread.sleep(2000);
        } catch (IOException | InterruptedException e) {
            logger.error("Error: " + e.getMessage());
        }

    }
}
