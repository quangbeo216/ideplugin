package com.example.chatplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import com.intellij.util.ui.JBUI;
import com.intellij.openapi.editor.ex.util.EditorUtil;

public class ChatToolWindowFactory implements ToolWindowFactory {
	private final Map<String, Integer> snippets = new LinkedHashMap<>();
	private final Map<String, Integer> snippetTitleBlock = new LinkedHashMap<>();
	JPanel contentPanel;
	private EditorTextField editorTextField;
	boolean isDark;
	JButton sendButton;
	JButton reloadButton;
	JTextArea chatArea;
	JBTextField inputField;
	JPanel mainPanel;
	ComboBox<String> comboBoxCat;
	ComboBox<String> comboBoxTitle;
	JTextField textField;
	String apiUrl;
	JBScrollPane scrollPane;
	ToolWindow toolWindow;

	Project project;

	public ChatToolWindowFactory() {

		apiUrl = "http://plugin.quangbeo216.com/api/"; // Thay URL API của bạn
		String jsonResponse = getJsonResponse(apiUrl);
		// jsonResponse = jsonResponse.replace("\n", "<br>");
		String abc = """
				%s
				""".formatted(jsonResponse).stripIndent();
		jsonResponse = jsonResponse.replace("\\n", "\n");
		jsonResponse = jsonResponse.replace("\\r", "\n");
		System.out.println(jsonResponse);
		// Giả lập danh sách snippet
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

		System.out.println(response.toString());

		return response.toString();
	}


	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(null); // Loại bỏ border

		contentPanel = new MyPanel();
		contentPanel.setBorder(null);
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); // Thêm BoxLayout để quản lý các thành phần theo chiều dọc

		// Tạo scrollPane chứa contentPanel
		//EditorTextField editorTextField = new MyEditorTextField("abc");

		//contentPanel.add(editorTextField,BorderLayout.CENTER);
		footerView();
		scrollPane = new JBScrollPane(contentPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // Thanh cuộn chỉ hiện khi cần
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Tắt thanh cuộn ngang nếu không cần

		scrollPane.setBorder(new EmptyBorder(0, 0, 20, 0));
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		getCategory();
		addSelectCategory();

		addSendAction();
		reloadButon();
		toolWindow.getComponent().add(mainPanel, BorderLayout.CENTER);
		this.toolWindow = toolWindow;
		this.project = project;
		toolWindow.getComponent().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int width = toolWindow.getComponent().getWidth();
				System.out.println("Updated ToolWindow Width: " + width);
			}
		});


	}

	private void getCategory() {
		String urlString = apiUrl + "getListCategory";
		JSONObject jsonResponse = new JSONObject(getJsonResponse(urlString));
		JSONArray data = jsonResponse.getJSONArray("data");

		// Duyệt qua các phần tử trong data
		snippets.clear();
		snippets.put("Category ---", 0);
		for (int i = 0; i < data.length(); i++) {
			JSONObject category = data.getJSONObject(i);
			snippets.put(category.getString("name"), category.getInt("id"));
		}

		comboBoxCat.removeAllItems();
		for (Map.Entry<String, Integer> entry : snippets.entrySet()) {
			comboBoxCat.addItem(entry.getKey());
		}
	}

	public void getListTitleNote(int id) {
		String urlString = apiUrl + "getListNoteTitle/" + id;

		JSONObject jsonResponse = new JSONObject(getJsonResponse(urlString));
		JSONArray data = jsonResponse.getJSONArray("data");
		snippetTitleBlock.clear();
		snippetTitleBlock.put("---", 0);
		for (int i = 0; i < data.length(); i++) {
			JSONObject category = data.getJSONObject(i);
			snippetTitleBlock.put(category.getString("title") + " " + category.getInt("id"), category.getInt("id"));
		}

		comboBoxTitle.removeAllItems();

		for (Map.Entry<String, Integer> entry : snippetTitleBlock.entrySet()) {
			System.out.println(entry.getKey());
			comboBoxTitle.addItem(entry.getKey());
		}

	}


	public void getContentNote(int id,String selectedSnippet) {
		String urlString = apiUrl + "getNoteContent/" + id;
		JSONObject jsonResponse = new JSONObject(getJsonResponse(urlString));
		JSONArray data = jsonResponse.getJSONArray("data");

		for (int i = 0; i < data.length(); i++) {
			JSONObject category = data.getJSONObject(i);
			System.out.println(category.getString("content"));

			int width = this.toolWindow.getComponent().getWidth();
			EditorTextField editorTextField = new MyEditorTextField(category.getString("content").trim(),this.project,width);
			JLabel newLabel = new JLabel(selectedSnippet.trim());
			newLabel.setHorizontalAlignment(SwingConstants.LEFT); // Căn từ trên xuống dưới

			System.out.println(editorTextField.getHeight());

			contentPanel.add(newLabel);
			contentPanel.add(editorTextField);

			contentPanel.revalidate();
			contentPanel.repaint();
			mainPanel.revalidate();
			mainPanel.repaint();

			scrollPane.revalidate();
			scrollPane.repaint();

			SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));

		}
		//System.out.println("Response: " + response.toString());

	}


	private void footerView() {
		JPanel bottomPanelTop = new JPanel(new BorderLayout());
		JPanel bottomPanel = new JPanel(new BorderLayout());
		comboBoxCat = new ComboBox<>(snippets.keySet().toArray(new String[0]));

		comboBoxCat.addActionListener(e -> {
			String selectedSnippet = (String) comboBoxCat.getSelectedItem();
			if (selectedSnippet != null) {
				getListTitleNote(Integer.parseInt(String.valueOf(snippets.get(selectedSnippet))));
			}
		});

		bottomPanelTop.add(comboBoxCat, BorderLayout.BEFORE_LINE_BEGINS);

		comboBoxTitle = new ComboBox<>();
		comboBoxTitle.addActionListener(e -> {
			String selectetTitle = (String) comboBoxTitle.getSelectedItem();
			System.out.println("selectetTitle"+selectetTitle);
			if (selectetTitle != null) {
				getContentNote(Integer.parseInt(String.valueOf(snippetTitleBlock.get(selectetTitle))),selectetTitle);
			}
		});
		bottomPanelTop.add(comboBoxTitle, BorderLayout.CENTER);

		reloadButton = new JButton("reload");
		bottomPanelTop.add(reloadButton, BorderLayout.AFTER_LINE_ENDS);

		JPanel bottomPanelBottom = new JPanel(new BorderLayout());

		textField = new JTextField(15);
		bottomPanelBottom.add(textField, BorderLayout.CENTER);

		sendButton = new JButton("Submit");
		bottomPanelBottom.add(sendButton, BorderLayout.EAST);

		bottomPanel.add(bottomPanelTop, BorderLayout.AFTER_LINE_ENDS);
		bottomPanel.add(bottomPanelBottom, BorderLayout.SOUTH);

		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
	}

	private void addSendAction() {

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String message = inputField.getText();
				if (!message.isEmpty()) {
					chatArea.append("You: " + message + "\n");
				//	String response = getNoteCode(message);
					chatArea.append("if (x > 0) {\n    System.out.println(\"Positive\");\n} else {\n    System.out.println(\"Negative\");\n}");
					inputField.setText("");
				}
			}
		});
	}

	private void addSelectCategory() {
		// Add ItemListener
		comboBoxCat.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					System.out.println("Selected: " + e.getItem());
				}
			}
		});
	}

	private void reloadButon(){
		reloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getCategory();
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

