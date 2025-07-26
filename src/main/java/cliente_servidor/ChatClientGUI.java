package cliente_servidor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class ChatClientGUI extends JFrame {

    private static final String ENDERECO_SERVIDOR = "localhost";
    private static final int PORTA = 8080;

    private final JTextArea chatArea;
    private final JTextArea usersArea;
    private final JTextField messageField;
    private final JButton sendButton;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private String clientName;
    private String clientNumber;

    public ChatClientGUI() {
        super("Chat Cliente"); // Já corrigido para título genérico inicial

        // CONFIGURAÇÃO JANELA
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // COMPONENTES
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margens ao redor do painel principal

        // AREA CHAT
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBorder(BorderFactory.createTitledBorder("MENSAGENS"));
        mainPanel.add(chatScrollPane, BorderLayout.CENTER); // Adiciona ao centro do mainPanel

        // AREA USERS (Painel lateral para usuários)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Margem à esquerda
        usersArea = new JTextArea();
        usersArea.setEditable(false);
        usersArea.setLineWrap(true);
        usersArea.setWrapStyleWord(true);
        JScrollPane usersScrollPane = new JScrollPane(usersArea);
        usersScrollPane.setBorder(BorderFactory.createTitledBorder("USUÁRIOS ONLINE"));
        rightPanel.add(usersScrollPane, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST); // Adiciona o painel de usuários à direita

        add(mainPanel, BorderLayout.CENTER); // Adiciona o painel principal ao JFrame

        // AREA MENSAGENS E BOTÃO ENVIAR (southPanel)
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout(5, 5));
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10)); // Ajustado margem superior para espaçamento

        messageField = new JTextField("Escreva sua mensagem aqui");
        messageField.setForeground(Color.GRAY);
        messageField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evt) {
                if (messageField.getText().equals("Escreva sua mensagem aqui")) {
                    messageField.setText("");
                    messageField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent evt) {
                if (messageField.getText().isEmpty()) {
                    messageField.setText("Escreva sua mensagem aqui");
                    messageField.setForeground(Color.GRAY);
                }
            }
        });

        sendButton = new JButton("Enviar");
        sendButton.setBackground(new Color(144, 238, 144));
        sendButton.setForeground(Color.BLACK);
        sendButton.setFocusPainted(false);

        southPanel.add(messageField, BorderLayout.CENTER);
        southPanel.add(sendButton, BorderLayout.EAST);

        add(southPanel, BorderLayout.SOUTH);

        // Adicionar Listeners
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        setVisible(true); // Torna a janela visível primeiro

        connectToServer();
    }

    private void connectToServer() {
        try {
            // Solicita o nome de usuário e agora o número
            clientName = JOptionPane.showInputDialog(this, "Digite seu nome de usuário:", "Nome de Usuário", JOptionPane.QUESTION_MESSAGE);
            if (clientName == null || clientName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome de usuário inválido. Encerrando.", "Erro", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
                return;
            }
            clientNumber = JOptionPane.showInputDialog(this, "Digite seu número (ex: 1):", "Número de Usuário", JOptionPane.QUESTION_MESSAGE);
            if (clientNumber == null || clientNumber.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Número de usuário inválido. Encerrando.", "Erro", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
                return;
            }

            // Atualiza o título da janela com o nome do cliente (CORREÇÃO JÁ FEITA ANTERIORMENTE)
            setTitle("Chat Cliente - " + clientName);


            socket = new Socket(ENDERECO_SERVIDOR, PORTA);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("LOGIN:" + clientName + ":" + clientNumber);

            chatArea.append("[INFO] Conectado ao servidor como: " + clientName + "\n");

            // Inicia uma thread separada para ouvir mensagens do servidor
            new Thread(this::listenForMessages).start();

        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(this, "Servidor não encontrado: " + ENDERECO_SERVIDOR, "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de I/O na conexão com o servidor: " + e.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    // Método para enviar mensagem
    private void sendMessage() {
        String message = messageField.getText();
        if (message.equals("Escreva sua mensagem aqui") || message.trim().isEmpty()) {
            return; // Não envia mensagens vazias ou o texto de placeholder
        }

        // --- INÍCIO DA MODIFICAÇÃO PARA EXIBIR A PRÓPRIA MENSAGEM ---
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("/privado ")) {
                // Para mensagens privadas que você envia, formate a exibição localmente
                String[] parts = message.split(" ", 3);
                if (parts.length >= 3) {
                    String recipient = parts[1];
                    String messageContent = parts[2];
                    chatArea.append("[Você para " + recipient + "] " + messageContent + "\n");
                }
            } else {
                // Para mensagens públicas que você envia
                chatArea.append("[" + clientName + "] " + message + "\n");
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength()); // Rola para o final
        });
        // --- FIM DA MODIFICAÇÃO PARA EXIBIR A PRÓPRIA MENSAGEM ---


        // Lógica de tratamento de comandos (similar ao console)
        if (message.equalsIgnoreCase("sair")) {
            out.println("sair"); // Envia "sair" para o servidor
            try {
                if (socket != null) socket.close();
            } catch (IOException ex) {
                System.err.println("Erro ao fechar socket ao sair: " + ex.getMessage());
            }
            System.exit(0); // Fecha a aplicação do cliente
        } else if (message.equalsIgnoreCase("/help")) { // Adicionado comando help
            showHelp();
        } else if (message.startsWith("/privado ")) {
            String[] parts = message.split(" ", 3);
            if (parts.length >= 3) {
                String recipient = parts[1];
                String messageContent = parts[2];
                out.println("PRIVATE:" + recipient + ":" + messageContent); // Envia no formato privado
            } else {
                JOptionPane.showMessageDialog(this, "Formato incorreto. Use: /privado <nome_destino> <mensagem>", "Erro de Comando", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            // Mensagem pública
            out.println(message);
        }

        // Limpa o campo de mensagem e reseta a cor/placeholder se necessário
        messageField.setText("");
        messageField.setForeground(Color.BLACK); // Garante cor preta após enviar
        messageField.requestFocusInWindow(); // Mantém o foco no campo de mensagem
    }


    // Thread para ouvir mensagens do servidor
    private void listenForMessages() {
        try {
            String serverMessageLine;
            while ((serverMessageLine = in.readLine()) != null) {
                final String msg = serverMessageLine; // Copia para uso na lambda
                SwingUtilities.invokeLater(() -> processServerMessage(msg)); // Processa na EDT
            }
        } catch (IOException e) {
            if (socket != null && !socket.isClosed()) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Conexão com o servidor perdida: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE)
                );
            }
        } finally {
            try {
                if (socket != null) socket.close();
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar recursos do cliente: " + e.getMessage());
            }
            SwingUtilities.invokeLater(() -> {
                chatArea.append("[INFO] Conexão com o servidor encerrada.\n");
                sendButton.setEnabled(false); // Desabilita botão de enviar
                messageField.setEditable(false); // Desabilita campo de texto
            });
        }
    }

    // Processa as mensagens recebidas do servidor e atualiza a GUI
    private void processServerMessage(String serverMessage) {
        if (serverMessage.startsWith("PUBLIC_MSG:")) {
            String[] parts = serverMessage.split(":", 3);
            if (parts.length >= 3) {
                // Filtra o eco do servidor para suas próprias mensagens públicas,
                // porque a exibição já foi feita localmente pelo sendMessage()
                if (!parts[1].trim().equalsIgnoreCase(clientName.trim())) {
                    chatArea.append("[" + parts[1] + "] " + parts[2] + "\n");
                }
            }
        } else if (serverMessage.startsWith("PRIVATE_MSG:")) {
            String[] parts = serverMessage.split(":", 3);
            if (parts.length >= 3) {
                chatArea.append("[PRIVADO de " + parts[1] + "] " + parts[2] + "\n");
            }
        } else if (serverMessage.startsWith("PRIVATE_SENT_CONFIRMATION:")) {
            // Esta parte NÃO é mais necessária se o sendMessage() já exibe a mensagem privada enviada localmente.
            // Para evitar duplicação, é melhor remover ou comentar esta parte aqui.
            // String[] parts = serverMessage.split(":", 3);
            // if (parts.length >= 3) {
            //     chatArea.append("[Você para " + parts[1] + "] " + parts[2] + "\n"); // Confirmação de envio privado
            // }
        } else if (serverMessage.startsWith("SERVER_MSG:")) {
            String infoMessage = serverMessage.substring("SERVER_MSG:".length());
            chatArea.append("[INFO] " + infoMessage + "\n");
            // Se o servidor indicar um erro de login, pode fechar a app
            if (infoMessage.contains("ERRO: Nome de usuário") && infoMessage.contains("já em uso")) {
                JOptionPane.showMessageDialog(this, infoMessage, "Erro de Login", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        } else if (serverMessage.startsWith("USER_LIST:")) {
            String userList = serverMessage.substring("USER_LIST:".length());
            usersArea.setText("Usuários Online:\n" + userList.replace(",", "\n")); // Formata para listar um por linha
        } else {
            // Fallback para mensagens não reconhecidas
            chatArea.append("[MSG DESCONHECIDA] Servidor: " + serverMessage + "\n");
        }
        // Garante que o scrollbar esteja sempre no final
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // Método para mostrar os comandos disponíveis
    private void showHelp() {
        String helpMessage = "--- Comandos Disponiveis ---\n" +
                "Enviar mensagem digitando e clicando 'Enviar'.\n" +
                "Para mensagem privada, use: /privado <nome_destino> <mensagem>\n" +
                "Para ajuda, digite /help e pressione Enter.\n" +
                "Para desconectar, digite 'sair' e pressione Enter.\n" +
                "---------------------------\n";
        JOptionPane.showMessageDialog(this, helpMessage, "Ajuda de Comandos", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        // Garante que a GUI seja criada e atualizada na Thread de Despacho de Eventos da Swing
        SwingUtilities.invokeLater(() -> {
            new ChatClientGUI();
        });
    }
}
