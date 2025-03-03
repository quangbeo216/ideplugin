package com.example.chatplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import org.jetbrains.annotations.NotNull
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.swing.JButton
import javax.swing.JPanel
import java.util.Scanner

class ChatToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: com.intellij.openapi.project.Project, toolWindow: ToolWindow) {
        val panel = JPanel(BorderLayout())
        val chatArea = JBTextArea()
        chatArea.isEditable = false
        val scrollPane = JBScrollPane(chatArea)
        panel.add(scrollPane, BorderLayout.CENTER)

        val inputPanel = JPanel(BorderLayout())
        val inputField = JBTextField()
        val sendButton = JButton("Send")

        sendButton.addActionListener {
            val message = inputField.text
            if (message.isNotEmpty()) {
                chatArea.append("You: $message\n")
                val response = getAIResponse(message)
                chatArea.append("AI: $response\n")
                inputField.text = ""
            }
        }

        inputPanel.add(inputField, BorderLayout.CENTER)
        inputPanel.add(sendButton, BorderLayout.EAST)
        panel.add(inputPanel, BorderLayout.SOUTH)

        toolWindow.component.add(panel)
    }

    private fun getAIResponse(message: String): String {
        val apiKey = "your-api-key-here" // Thay thế bằng API Key thật của bạn
        val urlString = "https://api.openai.com/v1/chat/completions"
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            
            val jsonInputString = "{\"model\":\"gpt-4\", \"messages\":[{\"role\":\"user\", \"content\":\"$message\"}]}"
            conn.outputStream.write(jsonInputString.toByteArray())
            
            val scanner = Scanner(conn.inputStream)
            val response = StringBuilder()
            while (scanner.hasNext()) {
                response.append(scanner.nextLine())
            }
            scanner.close()
            response.toString()
        } catch (e: IOException) {
            "Error connecting to AI service."
        }
    }
}

class OpenChatAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showMessageDialog("Chat Plugin is running!", "Chat Plugin", Messages.getInformationIcon())
    }
}
