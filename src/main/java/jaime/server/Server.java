package jaime.server;

import jaime.repositorio.FunkoRepositorioImp;
import jaime.servicios.DatabaseManager;
import jaime.servicios.FunkoNotificacionImp;
import jaime.servicios.FunkoServicioImp;
import jaime.servicios.LeerCSV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    public static final String TOKEN_SECRET = "DAW2023";
    public static final long TOKEN_EXPIRATION = 10000;
    private static final AtomicLong clientNumber = new AtomicLong(0);
    private final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        LeerCSV l = LeerCSV.getInstance();
        FunkoServicioImp funkoServicio = FunkoServicioImp.getInstance(
                FunkoRepositorioImp.getInstance(DatabaseManager.getInstance()),
                FunkoNotificacionImp.getInstance()
        );
        funkoServicio.getNotifications().subscribe(
                notificacion -> {
                    switch (notificacion.getTipo()) {
                        case NEW:
                            System.out.println("🟢 Funko insertado: " + notificacion.getContenido());
                            break;
                        case UPDATED:
                            System.out.println("🟠 Funko actualizado: " + notificacion.getContenido());
                            break;
                        case DELETED:
                            System.out.println("🔴 Funko eliminado: " + notificacion.getContenido());
                            break;
                    }
                },
                error -> System.err.println("Se ha producido un error: " + error),
                () -> System.out.println("Completado")
        );
        l.leerCsv().subscribe(funko -> funkoServicio.save(funko).subscribe());
        System.out.println("Datos cargados en la base de datos");
        try {
            // Nos anunciamos como socket
            ServerSocket serverSocket = new ServerSocket(3000);
            System.out.println("Servidor escuchando en el puerto 3000");

            // Nos ponemos a escuchar, cada petición la atendemos en un hilo para no bloquear el main
            while (true) {
                new ClientHandler(serverSocket.accept(), clientNumber.incrementAndGet()).run();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
