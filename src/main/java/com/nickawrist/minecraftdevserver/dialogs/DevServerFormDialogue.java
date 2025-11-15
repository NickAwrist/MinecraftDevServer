package com.nickawrist.minecraftdevserver.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DevServerFormDialogue extends DialogWrapper {

    private JPanel mainPanel;
    private JTextField serverNameField;
    private JTextField serverVersionField;

    public DevServerFormDialogue(@Nullable Project project) {
        super(project);
        setTitle("Create Dev Server");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        mainPanel = new JPanel(new BorderLayout());

        JLabel serverNameLabel = new JLabel("Server Name");
        serverNameField = new JTextField();
        JLabel serverVersionLabel = new JLabel("Server Version");
        serverVersionField = new JTextField();

        mainPanel.add(serverNameLabel, BorderLayout.NORTH);
        mainPanel.add(serverNameField, BorderLayout.CENTER);

        mainPanel.add(serverVersionLabel, BorderLayout.SOUTH);
        mainPanel.add(serverVersionField, BorderLayout.AFTER_LAST_LINE);

        return mainPanel;
    }

    public String getServerName() {
        return serverNameField.getText();
    }

    public String getServerVersion() {
        return serverVersionField.getText();
    }
}
