package cliente_servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName;
    private Map<String, PrintWriter> clientesConectados;

    public ClientHandler(Socket socket, Map<String, PrintWriter> clientesConectados) {
        this.clientSocket = socket;
        this.clientesConectados = clientesConectados;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String clientAddress = clientSocket.getInetAddress().getHostAddress();

            String loginMessage = in.readLine();
            if (loginMessage != null && loginMessage.startsWith("LOGIN:")) {
                String[] parts = loginMessage.split(":");
                if (parts.length >= 3) {
                    clientName = parts[1];
                    clientesConectados.put(clientName, out);
                    System.out.println("Cliente " + clientName + " (" + clientAddress + ") conectado e cadastrado.");
                    out.println("SERVER_MSG:Bem-vindo, " + clientName + "! Você está online.");
                } else {
                    out.println("SERVER_MSG:Erro no formato do LOGIN. Conexão será encerrada.");
                    System.err.println("Formato de LOGIN inválido de " + clientAddress);
                    return;
                }
            } else {
                out.println("SERVER_MSG:Primeira mensagem deve ser um LOGIN. Conexão será encerrada.");
                System.err.println("Primeira mensagem não foi LOGIN de " + clientAddress);
                return;
            }

            String mensagemCliente;
            while ((mensagemCliente = in.readLine()) != null) {
                System.out.println("Mensagem de " + clientName + ": " + mensagemCliente);
                out.println("SERVER_MSG:Servidor recebeu sua mensagem: " + mensagemCliente);

                if (mensagemCliente.equalsIgnoreCase("sair")) {
                    System.out.println("Cliente " + clientName + " solicitou desconexão.");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Erro na comunicação com o cliente " + (clientName != null ? clientName : clientSocket.getInetAddress().getHostAddress()) + ": " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                if (clientName != null) {
                    clientesConectados.remove(clientName);
                    System.out.println("Cliente " + clientName + " desconectado e removido.");
                } else {
                    System.out.println("Cliente desconectado (nome não registrado).");
                }
            } catch (IOException e) {
                System.err.println("Erro ao fechar o socket do cliente: " + e.getMessage());
            }
        }
    }
}