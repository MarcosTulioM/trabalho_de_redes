package cliente_servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private static final String ENDERECO_SERVIDOR = "localhost";
    private static final int PORTA = 8080;
    private static String meuNome;

    public static void main(String[] args) {
        System.out.println("Cliente iniciando...");

        try (Socket socket = new Socket(ENDERECO_SERVIDOR, PORTA);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor em " + ENDERECO_SERVIDOR + ":" + PORTA);

            System.out.print("Digite seu nome: ");
            meuNome = scanner.nextLine();
            System.out.print("Digite seu número (ex: 1): ");
            String numeroCliente = scanner.nextLine();

            out.println("LOGIN:" + meuNome + ":" + numeroCliente);

            new Thread(() -> {
                try {
                    String mensagemServidor;
                    while ((mensagemServidor = in.readLine()) != null) {
                        System.out.println("Do servidor: " + mensagemServidor);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao ler do servidor (conexão perdida?): " + e.getMessage());
                }
            }).start();

            System.out.println("Digite 'sair' para desconectar.");
            String mensagemUsuario;

            while (true) {
                System.out.print("Você (" + meuNome + "): "); // Exibe o nome do usuário
                mensagemUsuario = scanner.nextLine();

                out.println(mensagemUsuario);

                if (mensagemUsuario.equalsIgnoreCase("sair")) {
                    break;
                }
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