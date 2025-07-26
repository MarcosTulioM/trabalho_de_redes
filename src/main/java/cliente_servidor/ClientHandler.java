package cliente_servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName; // Armazena o nome do cliente logado
    private Map<String, PrintWriter> clientesConectados; // Referência ao mapa compartilhado do Server

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
                String[] parts = loginMessage.split(":", 3); // Limite 3 para pegar o nome e número
                if (parts.length >= 3) {
                    clientName = parts[1];
                    if (clientesConectados.containsKey(clientName)) {
                        out.println("SERVER_MSG:ERRO: Nome de usuário '" + clientName + "' já em uso. Por favor, escolha outro.");
                        System.out.println("Login falhou para " + clientAddress + ": Nome '" + clientName + "' já em uso.");
                        return; // Encerra a thread, pois o login falhou
                    }

                    clientesConectados.put(clientName, out); // Adiciona o cliente ao mapa
                    System.out.println("Cliente " + clientName + " (" + clientAddress + ") conectado e cadastrado.");
                    out.println("SERVER_MSG:Bem-vindo, " + clientName + "! Você está online.");

                    Server.sendUserListToAll(); // Envia a lista atualizada para todos
                } else {
                    out.println("SERVER_MSG:ERRO: Formato incorreto do LOGIN. Use: LOGIN:<nome>:<numero>. Conexão será encerrada.");
                    System.err.println("Formato de LOGIN inválido de " + clientAddress);
                    return; // Encerra a thread
                }
            } else {
                out.println("SERVER_MSG:ERRO: A primeira mensagem deve ser um LOGIN. Conexão será encerrada.");
                System.err.println("Primeira mensagem não foi LOGIN de " + clientAddress);
                return; // Encerra a thread
            }

            String mensagemCliente;
            while ((mensagemCliente = in.readLine()) != null) {
                if (mensagemCliente.equalsIgnoreCase("sair")) {
                    System.out.println("Cliente " + clientName + " solicitou desconexão.");
                    break; // Sai do loop e encerra a thread
                } else if (mensagemCliente.startsWith("PRIVATE:")) {
                    // Mensagem privada: PRIVATE:<destinatario>:<conteudo>
                    String[] parts = mensagemCliente.split(":", 3); // Limite 3 para pegar o conteúdo completo
                    if (parts.length >= 3) {
                        String recipient = parts[1];
                        String messageContent = parts[2];
                        Server.sendPrivateMessage(clientName, recipient, messageContent); // Chama o método estático do Server
                    } else {
                        out.println("SERVER_MSG:ERRO: Formato incorreto para mensagem privada. Use: PRIVATE:<destinatario>:<mensagem>");
                    }
                } else {
                    // Qualquer outra mensagem é tratada como pública
                    Server.broadcastMessage(clientName, mensagemCliente); // Chama o método estático do Server
                }
            }

        } catch (IOException e) {
            // Este catch será acionado se a conexão cair inesperadamente (ex: cliente fecha a janela)
            System.err.println("Erro na comunicação com o cliente " + (clientName != null ? clientName : clientSocket.getInetAddress().getHostAddress()) + ": " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }

                if (clientName != null) {
                    clientesConectados.remove(clientName); // Remove do mapa compartilhado
                    System.out.println("Cliente " + clientName + " desconectado e removido.");
                    Server.sendUserListToAll(); // Envia a lista atualizada para todos após a saída
                } else {
                    System.out.println("Cliente desconectado (nome não registrado antes da queda/erro).");
                }
            } catch (IOException e) {
                System.err.println("Erro ao fechar o socket do cliente: " + e.getMessage());
            }
        }
    }
}