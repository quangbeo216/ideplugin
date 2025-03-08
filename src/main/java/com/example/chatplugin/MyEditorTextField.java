package com.example.chatplugin;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;

import javax.swing.*;
import java.awt.*;

public class MyEditorTextField extends EditorTextField {
	public MyEditorTextField(String content, Project project, int width){
		super(content, project,null);
		//this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		//this.setBorder(BorderFactory.createEmptyBorder());
		this.setOpaque(false); // Xóa nền
		this.setBorder(null);
		this.setEnabled(false);
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
		this.putClientProperty("JComponent.outline", null); // Tránh viền mặc định

		this.setBackground(scheme.getDefaultBackground()); // Đặt màu nền trong suốt
		this.setForeground(scheme.getDefaultForeground()); // Màu chữ theo theme

		// Thiết lập chiều rộng theo yêu cầu
		this.setPreferredSize(new Dimension(width - 20,this.calculateHeight()+15));

		// Lắng nghe thay đổi nội dung để cập nhật chiều cao tự động
	}

	// Tính toán chiều cao tự động dựa trên số dòng
	private int calculateHeight() {
		int lineCount = getLineCount(); // Lấy số dòng hiện tại trong văn bản
		int lineHeight = this.getFontMetrics(this.getFont()).getHeight(); // Chiều cao của mỗi dòng
		System.out.println("lineHeight"+lineHeight);
		System.out.println("lineCount"+lineCount);
		return lineCount * lineHeight + 10; // Cộng thêm một chút để có không gian giữa các dòng
	}

	// Cập nhật chiều cao khi nội dung thay đổi
	private void updateHeight() {
		this.setPreferredSize(new Dimension(this.getWidth(), calculateHeight()));
		this.revalidate(); // Cập nhật layout của component
	}

	// Lấy số dòng trong văn bản
	private int getLineCount() {
		return this.getDocument().getLineCount();
		//System.out.println("zzzzz"+this.getDocument().getLineCount());
		//return this.getDocument().getTextLength() == 0 ? 1 : this.getDocument().getTextLength() / this.getFontMetrics(this.getFont()).charWidth('m');
	}
}
