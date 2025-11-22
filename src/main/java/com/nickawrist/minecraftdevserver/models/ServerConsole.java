package com.nickawrist.minecraftdevserver.models;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.editor.Editor;
import com.nickawrist.minecraftdevserver.constants.PluginConstants;

import javax.swing.*;

public class ServerConsole {
    private ConsoleView consoleView;

    public ServerConsole() {
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(PluginConstants.project).getConsole();
        enableSoftWraps();
    }

    private void enableSoftWraps() {
        if (consoleView instanceof ConsoleViewImpl consoleViewImpl) {
            Editor editor = consoleViewImpl.getEditor();
            if (editor != null) {
                editor.getSettings().setUseSoftWraps(true);
            }
        }
    }


    public JComponent getComponent() {
        return consoleView.getComponent();
    }

    public ConsoleView getConsoleView() {
        return consoleView;
    }
}
