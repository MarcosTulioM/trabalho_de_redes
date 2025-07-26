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
    //Flag para controlar quando imprimir o prompt
    private static volatile boolean promptNeeded = true; // 'volatile' para acesso seguro entre threads

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

            // Thread que permite que o cliente envie e receba mensagens sem bloquear a entrada do usuário
            new Thread(() -> {
                try {
                    String mensagemServidor;
                    while ((mensagemServidor = in.readLine()) != null) {

                        if (!promptNeeded) { //Limpe o prompt antes de imprimir a nova mensagem
                            System.out.print("\r" + " ".repeat(("Você (" + meuNome + "): ").length()) + "\r");
                        }


                        boolean shouldReprintPrompt = true; // Por padrão, reimprime o prompt após a mensagem

                        if (mensagemServidor.startsWith("PUBLIC_MSG:")) {
                            String[] parts = mensagemServidor.split(":", 3);
                            if (parts.length >= 3) {
                                // Exibe a mensagem pública apenas se NÃO for do próprio remetente (ignora case e trim)
                                if (!parts[1].trim().equalsIgnoreCase(meuNome.trim())) {
                                    System.out.println("[" + parts[1] + "] " + parts[2]);
                                } else {
                                    // Se a mensagem pública é sua, não precisa reimprimir o prompt agora.
                                    // O prompt será tratado pelo loop de envio.
                                    shouldReprintPrompt = false;
                                }
                            }
                        } else if (mensagemServidor.startsWith("PRIVATE_MSG:")) {
                            String[] parts = mensagemServidor.split(":", 3);
                            if (parts.length >= 3) {
                                System.out.println("[PRIVADO de " + parts[1] + "] " + parts[2]);
                            }
                        } else if (mensagemServidor.startsWith("PRIVATE_SENT_CONFIRMATION:")) {
                            // Esta é a confirmação do seu envio privado. Não precisa reimprimir o prompt.
                            String[] parts = mensagemServidor.split(":", 3);
                            if (parts.length >= 3) {
                                System.out.println("[Você para " + parts[1] + "] " + parts[2]);
                            }
                            shouldReprintPrompt = false; // Não reimprime o prompt aqui
                        }
                        else if (mensagemServidor.startsWith("SERVER_MSG:")) {
                            System.out.println("[INFO] " + mensagemServidor.substring("SERVER_MSG:".length()));
                        } else if (mensagemServidor.startsWith("USER_LIST:")) {
                            String userList = mensagemServidor.substring("USER_LIST:".length());
                            System.out.println("[USUÁRIOS ONLINE] " + userList);
                        } else {
                            System.out.println("[MSG DESCONHECIDA] Servidor: " + mensagemServidor);
                        }

                        // Imprime o prompt APENAS se shouldReprintPrompt for true
                        if (shouldReprintPrompt) {
                            System.out.print("Você (" + meuNome + "): ");
                            promptNeeded = false;
                        } else {
                            promptNeeded = true;
                        }
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
                // Imprime o prompt SOMENTE se ele for necessário (não foi impresso pela thread de leitura)
                if (promptNeeded) {
                    System.out.print("Você (" + meuNome + "): ");
                    promptNeeded = false; // Indica que o prompt está impresso
                }

                mensagemUsuario = scanner.nextLine(); // Lê a entrada do usuário

                promptNeeded = true; // Prepare para que o prompt seja reimpresso após a próxima ação

                if (mensagemUsuario.equalsIgnoreCase("sair")) {
                    out.println("sair");
                    break;
                } else if (mensagemUsuario.startsWith("/privado ")) {
                    String[] parts = mensagemUsuario.split(" ", 3);
                    if (parts.length >= 3) {
                        String recipient = parts[1];
                        String messageContent = parts[2];
                        out.println("PRIVATE:" + recipient + ":" + messageContent);
                    } else {
                        System.out.println("[ERRO] Formato incorreto. Use: /privado <nome_destino> <mensagem>");
                        promptNeeded = true; // Re-imprime o prompt no caso de erro de sintaxe
                    }
                } else {
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