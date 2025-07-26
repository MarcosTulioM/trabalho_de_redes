import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private static final String ENDERECO_SERVIDOR = "localhost"; // O IP ou hostname do servidor
    private static final int PORTA = 8080; // A mesma porta que o servidor está escutando

    public static void main(String[] args) {
        System.out.println("Cliente iniciando...");

        //Garante que o socket, in, out e scanner sejam fechados automaticamente
        try (Socket socket = new Socket(ENDERECO_SERVIDOR, PORTA);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor em " + ENDERECO_SERVIDOR + ":" + PORTA);
            System.out.println("Digite 'sair' para desconectar.");

            String mensagemUsuario;
            String mensagemServidor;

            // Enviar mensagens do cliente para o servidor
            while (true) {
                System.out.print("Você: ");
                mensagemUsuario = scanner.nextLine(); // Lê a entrada do usuário

                out.println(mensagemUsuario); // Envia a mensagem para o servidor

                if (mensagemUsuario.equalsIgnoreCase("sair")) {
                    break;
                }

                // Espera e lê a resposta do servidor
                mensagemServidor = in.readLine();
                System.out.println("Servidor: " + mensagemServidor);
            }

        } catch (UnknownHostException e) {
            System.err.println("Erro: Servidor não encontrado. Verifique o endereço: " + ENDERECO_SERVIDOR);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Erro de I/O na comunicação com o servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Cliente desconectado.");
        }
    }
}
