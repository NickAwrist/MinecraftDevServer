package com.nickawrist.minecraftdevserver.models;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import com.nickawrist.minecraftdevserver.constants.PluginConstants;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.function.Consumer;

public class ServerConsole {
    private ConsoleView consoleView;
    private JPanel mainPanel;
    private JTextField commandInput;
    private Consumer<String> commandHandler;

    public ServerConsole() {
        if (SwingUtilities.isEventDispatchThread()) {
            consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(PluginConstants.project).getConsole();
            initializeUI();
        } else {
            ApplicationManager.getApplication().invokeAndWait(() -> {
                consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(PluginConstants.project).getConsole();
                initializeUI();
            }, ModalityState.defaultModalityState());
        }
    }

    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());

        // Console component
        JComponent consoleComponent = consoleView.getComponent();
        consoleComponent.setBackground(JBColor.BLACK);
        configureConsoleColors();
        mainPanel.add(consoleComponent, BorderLayout.CENTER);

        // Command input panel
        JPanel inputPanel = new JPanel(new BorderLayout(4, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        commandInput = new JTextField();
        commandInput.putClientProperty("JTextField.placeholderText", "Enter command...");
        commandInput.setBackground(JBColor.background());
        commandInput.setForeground(JBColor.foreground());

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

    private void configureConsoleColors() {
        if (!(consoleView instanceof ConsoleViewImpl)) return;

        Editor editor = ((ConsoleViewImpl) consoleView).getEditor();
        if (editor == null) return;
        if (editor instanceof EditorEx) {
            ((EditorEx) editor).setBackgroundColor(JBColor.BLACK);
        }

        EditorColorsScheme scheme = editor.getColorsScheme();
        TextAttributes whiteText = new TextAttributes(JBColor.WHITE, null, null, null, Font.PLAIN);
        scheme.setAttributes(ConsoleViewContentType.NORMAL_OUTPUT_KEY, whiteText);
        scheme.setAttributes(ConsoleViewContentType.SYSTEM_OUTPUT_KEY, whiteText);
    }
}
