import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class HttpServer {
    public static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_PATH = "DOCUMENT_ROOT";
    public String getDefaultPath() {
        return DEFAULT_PATH;
    }
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port: " + serverSocket.getLocalPort() + "\n");
        } catch (IOException e) {
            System.out.println("Port " + port + " is blocked.");
            System.exit(-1);
        }
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                MyClientSession session = new MyClientSession(clientSocket);
                new Thread(session).start();
            } catch (IOException e){
                System.out.println("Failed to establish connection. ");
                System.out.println(e.getMessage());
                System.exit(-1);
            }
        }

//        while (true) { try { Socket clientSocket = serverSocket.accept(); /* Для обработки запроса от каждого клиента создается * отдельный объект и отдельный поток */ MyClientSession session = new MyClientSession(clientSocket); new Thread(session).start(); } catch (IOException e) { System.out.println("Failed to establish connection."); System.out.println(e.getMessage()); System.exit(-1); } }
    }
}