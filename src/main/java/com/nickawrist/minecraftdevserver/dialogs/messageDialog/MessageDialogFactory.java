package com.nickawrist.minecraftdevserver.dialogs.messageDialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.nickawrist.minecraftdevserver.constants.PluginConstants;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;


public class MessageDialogFactory extends DialogWrapper {
    private final String message;

    public MessageDialogFactory(String message) {
        super(PluginConstants.project);
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
