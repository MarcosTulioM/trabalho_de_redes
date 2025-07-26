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

    /**
     * Envia uma mensagem para TODOS os clientes conectados.
     * @param sender O nome do remetente da mensagem.
     * @param message O conteúdo da mensagem a ser enviada.
     */
    public static void broadcastMessage(String sender, String message) {
        // Itera sobre todos os PrintWriters dos clientes conectados
        for (Map.Entry<String, PrintWriter> entry : clientesConectados.entrySet()) {
            // String recipientName = entry.getKey(); // Não é usado diretamente no broadcast, mas pode ser para logs
            PrintWriter recipientOut = entry.getValue();
            // Envia a mensagem no formato "PUBLIC_MSG:Remetente:Conteúdo"
            recipientOut.println("PUBLIC_MSG:" + sender + ":" + message);
        }
        System.out.println("BROADCAST de " + sender + ": " + message);
    }

    /**
     * Envia uma mensagem para um cliente específico.
     * @param sender O nome do remetente da mensagem.
     * @param recipient O nome do destinatário da mensagem.
     * @param message O conteúdo da mensagem a ser enviada.
     */
    public static void sendPrivateMessage(String sender, String recipient, String message) {
        PrintWriter recipientOut = clientesConectados.get(recipient); // Tenta obter o PrintWriter do destinatário

        if (recipientOut != null) {
            // Se o destinatário está online, envia a mensagem no formato "PRIVATE_MSG:Remetente:Conteúdo"
            recipientOut.println("PRIVATE_MSG:" + sender + ":" + message);
            PrintWriter senderOut = clientesConectados.get(sender);
            if (senderOut != null) { // Garante que o remetente ainda esteja online
                senderOut.println("PRIVATE_SENT_CONFIRMATION:" + recipient + ":" + message); // Um tipo diferente para o remetente
            }
            System.out.println("PRIVADO de " + sender + " para " + recipient + ": " + message);
        } else {
            // Se o destinatário não for encontrado (offline ou nome incorreto), informa o remetente.
            PrintWriter senderOut = clientesConectados.get(sender);
            if (senderOut != null) { // Garante que o remetente ainda esteja online
                senderOut.println("SERVER_MSG:O usuário '" + recipient + "' não está online ou não foi encontrado.");
                System.out.println("ERRO: Tentativa de enviar privado para offline: " + recipient + " de " + sender);
            }
        }
    }

    //Envia a lista atualizada de usuários online para todos os clientes conectados.
    public static void sendUserListToAll() {
        StringBuilder userList = new StringBuilder("USER_LIST:");
        // Constrói a string da lista de usuários
        for (String name : clientesConectados.keySet()) {
            userList.append(name).append(",");
        }
        // Remove a última vírgula se houver usuários, ou adiciona uma mensagem se não houver.
        if (userList.length() > "USER_LIST:".length()) {
            userList.setLength(userList.length() - 1); // Remove a vírgula final
        } else {
            userList.append("Nenhum usuário online."); // Caso não haja usuários
        }

        // Envia a lista para cada cliente conectado
        for (PrintWriter out : clientesConectados.values()) {
            out.println(userList.toString());
        }
        System.out.println("Lista de usuários atualizada enviada: " + userList.toString());
    }


}
