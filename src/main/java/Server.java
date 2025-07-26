import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;

    public class Server {

        private static final int PORTA = 8080; // Porta que o servidor vai escutar

        public static void main(String[] args) {
            System.out.println("Servidor iniciando...");

            try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
                // Fecha o serverSocket automaticamente
                System.out.println("Servidor escutando na porta " + PORTA);
                System.out.println("Aguardando conexões de clientes...");

                // serverSocket.accept() é um método BLOQUEANTE
                // Ele espera até que um cliente se conecte.
                // Quando um cliente se conecta, ele retorna um objeto Socket que representa
                // a conexão com aquele cliente específico.

                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

                // Configura streams de entrada e saída para comunicação com o cliente
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                //Escreve caracteres para o socket
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String mensagemCliente;

                // Lê mensagens do cliente
                while ((mensagemCliente = in.readLine()) != null) {
                    System.out.println("Mensagem recebida do cliente: " + mensagemCliente);

                    // Envia uma resposta para o cliente
                    out.println("Servidor recebeu: " + mensagemCliente);

                    // Se o cliente enviar "sair", encerra a conexão
                    if (mensagemCliente.equalsIgnoreCase("sair")) {
                        System.out.println("Cliente " + clientSocket.getInetAddress().getHostAddress() + " solicitou desconexão.");
                        break;
                    }
                }

                // Fechar os recursos
                clientSocket.close();
                System.out.println("Conexão com o cliente encerrada.");

            } catch (IOException e) {
                System.err.println("Erro no servidor: " + e.getMessage());
                e.printStackTrace(); // Para ver o stack trace completo do erro
            } finally {
                System.out.println("Servidor encerrado.");
            }
        }
    }
