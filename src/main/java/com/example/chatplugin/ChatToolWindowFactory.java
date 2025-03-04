package com.example.chatplugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.intellij.openapi.editor.ex.util.EditorUtil;

public class ChatToolWindowFactory implements ToolWindowFactory {
    private final Map<String, String> snippets = new HashMap<>();
    private EditorTextField editorTextField;
    boolean isDark;
    JButton sendButton;
    JTextArea chatArea;
    JBTextField inputField;


    public ChatToolWindowFactory() {

        String apiUrl = "https://a7c2-118-70-168-239.ngrok-free.app"; // Thay URL API của bạn
        String jsonResponse = getJsonResponse(apiUrl);
        // jsonResponse = jsonResponse.replace("\n", "<br>");
        String abc = """
                %s
                """.formatted(jsonResponse).stripIndent();
        jsonResponse = jsonResponse.replace("\\n", "\n");
        jsonResponse = jsonResponse.replace("\\r", "\n");
        System.out.println(jsonResponse);
        // Giả lập danh sách snippet
        snippets.put("Hello World", jsonResponse);
        snippets.put("For Loop", "for (int i = 0; i < 10; i++) {\n    System.out.println(i);\n}");
        snippets.put("If Condition", "if (x > 0) {\n    System.out.println(\"Positive\");\n} else {\n    System.out.println(\"Negative\");\n}");


        // Lấy theme hiện tại
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();

        // Xác định nền light/dark
        isDark = scheme.getDefaultBackground().getRed() < 128;


    }

    public static String getJsonResponse(String urlString) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET"); // Hoặc "POST" nếu cần
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }


                reader.close();
            } else {
                System.out.println("Lỗi: " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    @Override
    public void createToolWindowContent(@NotNull com.intellij.openapi.project.Project project, @NotNull ToolWindow toolWindow) {
        JPanel panel = new JPanel(new BorderLayout());

        chatArea = new JBTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(Color.DARK_GRAY);
        chatArea.setForeground(Color.WHITE);

        JBScrollPane scrollPane = new JBScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JBTextField();

        sendButton = new JButton("Send");

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        toolWindow.getComponent().add(panel);
        /*JPanel panel = new JPanel(new BorderLayout());

        // EditorTextField để hiển thị snippet
        editorTextField = new EditorTextField("", project, null);
        editorTextField.setOneLineMode(false);
        editorTextField.setPreferredSize(new Dimension(400, 200));
        JBScrollPane scrollPane = new JBScrollPane(editorTextField);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Dropdown để chọn snippet
        JComboBox<String> snippetSelector = new JComboBox<>(snippets.keySet().toArray(new String[0]));
        snippetSelector.addActionListener(e -> {
            String selectedSnippet = (String) snippetSelector.getSelectedItem();
            if (selectedSnippet != null) {

                editorTextField.setText(snippets.get(selectedSnippet));
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Select Snippet:"), BorderLayout.WEST);
        topPanel.add(snippetSelector, BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.NORTH);

        toolWindow.getComponent().add(panel);*/
        addSendAction();

    }

    private String getNoteCode(String message){
        String apiKey = "your-api-key-here"; // Thay thế bằng API Key thật của bạn
        String urlString = "https://api.openai.com/v1/chat/completions";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = "{\"model\":\"gpt-4\", \"messages\":[{\"role\":\"user\", \"content\":\"" + message + "\"}]}";
            conn.getOutputStream().write(jsonInputString.getBytes());

            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();
            return response.toString();
        } catch (IOException e) {
            return "Error connecting to AI service.";
        }
    }

    private String getAIResponse(String message) {
        String apiKey = "your-api-key-here"; // Thay thế bằng API Key thật của bạn
        String urlString = "https://api.openai.com/v1/chat/completions";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = "{\"model\":\"gpt-4\", \"messages\":[{\"role\":\"user\", \"content\":\"" + message + "\"}]}";
            conn.getOutputStream().write(jsonInputString.getBytes());

            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();
            return response.toString();
        } catch (IOException e) {
            return "Error connecting to AI service.";
        }
    }

    private void addSendAction() {

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText();
                if (!message.isEmpty()) {
                    chatArea.append("You: " + message + "\n");
                    String response = getAIResponse(message);
                    chatArea.append("if (x > 0) {\n    System.out.println(\"Positive\");\n} else {\n    System.out.println(\"Negative\");\n}");
                    inputField.setText("");
                }
            }
        });
    }
}

class OpenChatAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Messages.showMessageDialog("Chat Plugin is running!", "Chat Plugin", Messages.getInformationIcon());
    }
}
