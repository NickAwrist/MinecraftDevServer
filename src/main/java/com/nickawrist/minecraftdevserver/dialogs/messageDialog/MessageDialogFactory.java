package com.nickawrist.minecraftdevserver.dialogs.messageDialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MessageDialogFactory extends DialogWrapper {
    private final String message;

    public MessageDialogFactory(@Nullable Project project, String message) {
        super(project);
        this.message = message;
        setTitle("Message");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(message);
        label.setPreferredSize(new Dimension(200, 50));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}
