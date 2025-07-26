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

            //Thread que permite que o cliente envie e receba mensagens sem bloquear a entrada do usuário
            new Thread(() -> {
                try {
                    String mensagemServidor;
                    while ((mensagemServidor = in.readLine()) != null) {

                        if (mensagemServidor.startsWith("PUBLIC_MSG:")) {
                            String[] parts = mensagemServidor.split(":", 3);
                            if (parts.length >= 3) {
                                // Exibe a mensagem pública: [Remetente] Conteúdo
                                System.out.println("\n[" + parts[1] + "] " + parts[2]);
                            }
                        } else if (mensagemServidor.startsWith("PRIVATE_MSG:")) {
                            String[] parts = mensagemServidor.split(":", 3);
                            if (parts.length >= 3) {
                                // Exibe a mensagem privada: [PRIVADO de Remetente] Conteúdo
                                System.out.println("\n[PRIVADO de " + parts[1] + "] " + parts[2]);
                            }
                        } else if (mensagemServidor.startsWith("PRIVATE_SENT_CONFIRMATION:")) {
                            String[] parts = mensagemServidor.split(":", 3);
                            if (parts.length >= 3) {
                                System.out.println("\n[Você para " + parts[1] + "] " + parts[2]); // Confirmação de envio privado
                            }
                        }
                        else if (mensagemServidor.startsWith("SERVER_MSG:")) {
                            // Mensagens gerais do servidor: [INFO] Conteúdo
                            System.out.println("\n[INFO] " + mensagemServidor.substring("SERVER_MSG:".length()));
                        } else if (mensagemServidor.startsWith("USER_LIST:")) {
                            // Lista de usuários online
                            String userList = mensagemServidor.substring("USER_LIST:".length());
                            System.out.println("\n[USUÁRIOS ONLINE] " + userList);
                        } else {
                            // Fallback para mensagens não reconhecidas (para depuração)
                            System.out.println("\n[MSG DESCONHECIDA] Servidor: " + mensagemServidor);
                        }
                        // Garante que o prompt "Você:" apareça sempre abaixo da mensagem recebida
                        System.out.print("Você (" + meuNome + "): ");
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao ler do servidor (conexão perdida?): " + e.getMessage());
                }
            }).start();

            System.out.println("--- BEM-VINDO AO CHAT ---");
            System.out.println("Digite sua mensagem para o chat público.");
            System.out.println("Para mensagem privada, use: /privado <nome_destino> <mensagem>");
            System.out.println("Digite 'sair' para desconectar.");
            System.out.println("-------------------------");

            String mensagemUsuario;
            // Loop para ENVIAR mensagens do cliente para o servidor
            while (true) {
                System.out.print("Você (" + meuNome + "): "); // Exibe o nome do usuário
                mensagemUsuario = scanner.nextLine(); // Lê a entrada do usuário

                if (mensagemUsuario.equalsIgnoreCase("sair")) {
                    out.println("sair"); // Envia "sair" para o servidor para que ele possa processar
                    break;
                } else if (mensagemUsuario.startsWith("/privado ")) {
                    // Se a mensagem começa com "/privado ", trata como mensagem privada
                    String[] parts = mensagemUsuario.split(" ", 3); // Divide em "/privado", "nome_destino", "mensagem"
                    if (parts.length >= 3) {
                        String recipient = parts[1]; // O nome do destinatário
                        String messageContent = parts[2]; // O conteúdo da mensagem
                        out.println("PRIVATE:" + recipient + ":" + messageContent); // Envia no formato do protocolo
                    } else {
                        System.out.println("[ERRO] Formato incorreto. Use: /privado <nome_destino> <mensagem>");
                    }
                } else {
                    // Qualquer outra mensagem é tratada como pública (não precisa de prefixo "PUBLIC:" no envio)
                    out.println(mensagemUsuario);
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