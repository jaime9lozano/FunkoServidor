package jaime.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jaime.modelos.*;
import jaime.util.LocalDateAdapter;
import jaime.util.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static jaime.modelos.Request.Type.*;

public class Client {
    MyIDStore myid=MyIDStore.getInstance();
    private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private final Logger logger = LoggerFactory.getLogger(Client.class);
    private final Gson gson;
    private SSLSocket socket;
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
            Funko funkonuevo=new Funko(UUID.randomUUID(),"Spiderman", Tipos.MARVEL,59.0, LocalDate.now(), myid.addandgetID());
            Funko funkoactualizar=new Funko( UUID.randomUUID(),"Stitch", Tipos.DISNEY,66.0, LocalDate.now(), myid.addandgetID());
            token=sendRequestLogin();
            sendRequestFunkoCaro(token);
            sendFunko2023(token);
            sendFunkoStitch(token);
            sendFunkoID(token,4L);
            sendFunkoNuevo(token,funkonuevo);
            sendFunkoModificar(token,funkoactualizar);
            sendFunkoBorrar(token,7L);
            sendRequestSalir();

        } catch (IOException e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    private void closeConnection() throws IOException {
        logger.debug("Cerrando la conexión con el servidor: " + HOST + ":" + PORT);
        System.out.println("🔵 Cerrando Cliente");
        if (in != null)
            in.close();
        if (out != null)
            out.close();
        if (socket != null)
            socket.close();
    }

    private void openConnection() throws IOException {
        System.out.println("🔵 Iniciando Cliente");
        Map<String, String> myConfig = readConfigFile();

        logger.debug("Cargando fichero de propiedades");
        // System.setProperty("javax.net.debug", "ssl, keymanager, handshake"); // Debug
        System.setProperty("javax.net.ssl.trustStore", myConfig.get("keyFile")); // llavero cliente
        System.setProperty("javax.net.ssl.trustStorePassword", myConfig.get("keyPassword")); // clave

        SSLSocketFactory clientFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) clientFactory.createSocket(HOST, PORT);

        // Opcionalmente podemos forzar el tipo de protocolo -> Poner el mismo que el cliente
        logger.debug("Protocolos soportados: " + Arrays.toString(socket.getSupportedProtocols()));
        socket.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"});
        socket.setEnabledProtocols(new String[]{"TLSv1.3"});

        logger.debug("Conectando al servidor: " + HOST + ":" + PORT);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("✅ Cliente conectado a " + HOST + ":" + PORT);

        infoSession(socket);

    }
    public Map<String, String> readConfigFile() {
        try {
            logger.debug("Leyendo el fichero de configuracion");
            PropertiesReader properties = new PropertiesReader("client.properties");

            String keyFile = properties.getProperty("keyFile");
            String keyPassword = properties.getProperty("keyPassword");

            // Comprobamos que no estén vacías
            if (keyFile.isEmpty() || keyPassword.isEmpty()) {
                throw new IllegalStateException("Hay errores al procesar el fichero de propiedades o una de ellas está vacía");
            }

            // Comprobamos el fichero de la clave
            if (!Files.exists(Path.of(keyFile))) {
                throw new FileNotFoundException("No se encuentra el fichero de la clave");
            }

            Map<String, String> configMap = new HashMap<>();
            configMap.put("keyFile", keyFile);
            configMap.put("keyPassword", keyPassword);

            return configMap;
        } catch (FileNotFoundException e) {
            logger.error("Error en clave: " + e.getLocalizedMessage());
            System.exit(1);
            return null; // Este retorno nunca se ejecutará debido a System.exit(1)
        } catch (IOException e) {
            logger.error("Error al leer el fichero de configuracion: " + e.getLocalizedMessage());
            return null;
        }
    }
    private void infoSession(SSLSocket socket) {
        logger.debug("Información de la sesión");
        System.out.println("Información de la sesión");
        try {
            SSLSession session = socket.getSession();
            System.out.println("Servidor: " + session.getPeerHost());
            System.out.println("Cifrado: " + session.getCipherSuite());
            System.out.println("Protocolo: " + session.getProtocol());
            System.out.println("Identificador:" + new BigInteger(session.getId()));
            System.out.println("Creación de la sesión: " + session.getCreationTime());
            X509Certificate certificado = (X509Certificate) session.getPeerCertificates()[0];
            System.out.println("Propietario : " + certificado.getSubjectX500Principal());
            System.out.println("Algoritmo: " + certificado.getSigAlgName());
            System.out.println("Tipo: " + certificado.getType());
            System.out.println("Número Serie: " + certificado.getSerialNumber());
            // expiración del certificado
            System.out.println("Válido hasta: " + certificado.getNotAfter());
        } catch (SSLPeerUnverifiedException ex) {
            logger.error("Error en la sesión: " + ex.getLocalizedMessage());
        }
    }
    private String sendRequestLogin() {
        String myToken = null;
        Request<Login> request = new Request<>(LOGIN, new Login("jaime", "jaime1234"), null, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + LOGIN);
        logger.debug("Petición enviada: " + request);
        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta
        try {
            Response<String> response = gson.fromJson(in.readLine(), Response.class);
            logger.debug("Respuesta recibida: " + response.toString());
            // Ahora podríamos implementar un switch para cada tipo de respuesta
            // y hacer lo que queramos con ella...
            System.out.println("Respuesta recibida de tipo: " + response.status());

            switch (response.status()) {
                case TOKEN -> {
                    System.out.println("🟢 Mi token es: " + response.content());
                    myToken = response.content();
                }
                default -> throw new ClientException("Tipo de respuesta no esperado: " + response.content());
            }
        } catch (IOException | ClientException e) {
            logger.error("Error: " + e.getMessage());
        }
        return myToken;
    }
    private void sendRequestFunkoCaro(String token) {
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
                default -> throw new ClientException(response.content());
            }
        } catch (IOException | ClientException e) {
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
                default -> throw new ClientException("Tipo de respuesta no esperado: " + response.content());
            }
        } catch (IOException | ClientException e) {
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
                default -> throw new ClientException("Tipo de respuesta no esperado: " + response.content());
            }
        } catch (IOException | ClientException e) {
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
                default -> throw new ClientException("Tipo de respuesta no esperado: " + response.content());
            }
        } catch (IOException | ClientException e) {
            logger.error("Error: " + e.getMessage());
        }

    }
    private void sendFunkoID(String token,Long id) {
        // Al usar el toString me ahorro el problem ade las fechas con Gson
        Request<String> request = new Request<>(ID, Long.toString(id), token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + ID);
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
                case OK -> System.out.println("🟢 El funko con id: " + id + " es: " + response.content());
                default -> throw new ClientException("Tipo de respuesta no esperado: " + response.content());
            }
        } catch (IOException | ClientException e) {
            logger.error("Error: " + e.getMessage());
        }
    }
    private void sendFunkoNuevo(String token, Funko funko) {
        Gson gson1 = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
        String funkoJson = gson1.toJson(funko);
        Request<String> request = new Request<>(NUEVO, funkoJson, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + NUEVO);
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
                case OK -> System.out.println("🟢 Funko añadido: " + response.content());
                default -> throw new ClientException("Tipo de respuesta no esperado: " + response.content());
            }
        } catch (IOException | ClientException e) {
            logger.error("Error: " + e.getMessage());
        }
    }
    private void sendFunkoModificar(String token, Funko funko) {
        Gson gson1 = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
        String funkoJson = gson1.toJson(funko);
        Request<String> request = new Request<>(ACTUALIZAR, funkoJson, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + ACTUALIZAR);
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
                case OK -> System.out.println("🟢 Funko modificado: " + response.content());
                default -> throw new ClientException("Tipo de respuesta no esperado: " + response.content());
            }
        } catch (IOException | ClientException e) {
            logger.error("Error: " + e.getMessage());
        }
    }
    private void sendFunkoBorrar(String token, Long id) {

        // Al usar el toString me ahorro el problem ade las fechas con Gson
        Request<String> request = new Request<>(BORRAR, Long.toString(id), token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + BORRAR);
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
                case OK -> System.out.println("🟢 Funko borrado: " + response.content());
                default -> throw new ClientException("Tipo de respuesta no esperado: " + response.content());
            }
        } catch (IOException | ClientException e) {
            logger.error("Error: " + e.getMessage());
        }
    }
}
