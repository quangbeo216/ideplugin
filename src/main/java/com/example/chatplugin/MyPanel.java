package com.example.chatplugin;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MyPanel extends JPanel {

    public MyPanel() {
        super(new BorderLayout());
        // Lấy scheme màu hiện tại của IDE
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();

        // Lấy màu nền hiện tại của theme
        Color backgroundColor = scheme.getDefaultBackground();

        // Áp dụng màu nền vào JPanel
        this.setBackground(new Color(247, 248, 250));
        this.setBorder(null); //
        this.setLayout(new FlowLayout());

        // Thêm margin vào JPanel (khoảng cách 20px cho mỗi cạnh)
        this.setBorder(new EmptyBorder(0, 0, 20, 0)); // top, left, bottom, right
    }
    
}
