package com.nickawrist.minecraftdevserver.models;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ui.JBColor;
import com.nickawrist.minecraftdevserver.constants.PluginConstants;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ServerConsole {
    private ConsoleView consoleView;
    private JPanel mainPanel;
    private JTextField commandInput;
    private Consumer<String> commandHandler;

    public ServerConsole() {
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(PluginConstants.project).getConsole();
        initializeUI();
    }

    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());

        // Console component
        JComponent consoleComponent = consoleView.getComponent();
        consoleComponent.setBackground(JBColor.BLACK);
        mainPanel.add(consoleComponent, BorderLayout.CENTER);

        // Command input panel
        JPanel inputPanel = new JPanel(new BorderLayout(4, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        commandInput = new JTextField();
        commandInput.putClientProperty("JTextField.placeholderText", "Enter command...");
        commandInput.setBackground(JBColor.background());
        commandInput.setForeground(JBColor.foreground());

        // Send command on Enter key
        commandInput.addActionListener(e -> sendCommand());

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendCommand());

        inputPanel.add(commandInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);
    }

    private void sendCommand() {
        String command = commandInput.getText().trim();
        if (!command.isEmpty() && commandHandler != null) {
            commandHandler.accept(command);
            commandInput.setText("");
        }
    }

    public void setCommandHandler(Consumer<String> handler) {
        this.commandHandler = handler;
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public ConsoleView getConsoleView() {
        return consoleView;
    }
}
