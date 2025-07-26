package cliente_servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.PrintWriter;

public class Server {

    private static final int PORTA = 8080;
    private static Map<String, PrintWriter> clientesConectados = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Servidor iniciando...");

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor escutando na porta " + PORTA);
            System.out.println("Aguardando conexões de clientes...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, clientesConectados);

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }

        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Servidor encerrado.");
        }
    }
}
