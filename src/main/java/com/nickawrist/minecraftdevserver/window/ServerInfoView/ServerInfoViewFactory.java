package com.nickawrist.minecraftdevserver.window.ServerInfoView;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.nickawrist.minecraftdevserver.models.ServerInstance;

import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerInfoViewFactory {

    private static final int SERVER_STATE_POLL_INTERVAL_MS = 200;
    private static final int SERVER_STATE_POLL_ATTEMPTS = 50;

    public JComponent createServerInfoPanel(ServerInstance server) {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("Server Name: " + server.getServerName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel versionLabel = new JLabel("Version: " + server.getServerVersion());
        versionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel uuidLabel = new JLabel("UUID: " + server.getUuid());
        uuidLabel.setFont(uuidLabel.getFont().deriveFont(11f));
        uuidLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(versionLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(uuidLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel statusLabel = new JLabel();
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 12f));

        JButton controlButton = new JButton();
        controlButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlButton.setFocusPainted(false);
        controlButton.setBorderPainted(true);
        controlButton.setOpaque(true);
        controlButton.setForeground(new JBColor(new Color(0xFFFFFF), new Color(0xFFFFFF)));

        Runnable refreshStatus = () -> {
            boolean running = server.isServerRunning();
            statusLabel.setText("Status: " + (running ? "Running" : "Stopped"));
            if (running) {
                controlButton.setText("Stop Server");
                controlButton.setBackground(new JBColor(new Color(0xF44336), new Color(0xF44336)));
            } else {
                controlButton.setText("Start Server");
                controlButton.setBackground(new JBColor(new Color(0x4CAF50), new Color(0x4CAF50)));
            }
            controlButton.setEnabled(true);
        };

        refreshStatus.run();

        controlButton.addActionListener(e -> {
            controlButton.setEnabled(false);
            statusLabel.setText("Status: Updating...");

            new Thread(() -> {
                boolean desiredRunningState;
                try {
                    if (server.isServerRunning()) {
                        server.stopServer();
                        desiredRunningState = false;
                    } else {
                        server.startServer();
                        desiredRunningState = true;
                    }

                    waitForServerState(server, desiredRunningState);
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Status: Error - " + ex.getMessage());
                        controlButton.setEnabled(true);
                    });
                    return;
                }

                SwingUtilities.invokeLater(refreshStatus);
            }).start();
        });

        JPanel controlRow = new JPanel();
        controlRow.setLayout(new BoxLayout(controlRow, BoxLayout.X_AXIS));
        controlRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlRow.setOpaque(false);
        controlRow.add(statusLabel);
        controlRow.add(Box.createRigidArea(new Dimension(8, 0)));
        controlRow.add(controlButton);

        infoPanel.add(controlRow);

        JButton openServerFolderButton = new JButton("Open Server Folder");
        openServerFolderButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        openServerFolderButton.addActionListener(e -> openServerFolder(server));

        JButton openPluginsFolderButton = new JButton("Open Plugins Folder");
        openPluginsFolderButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        openPluginsFolderButton.addActionListener(e -> openPluginsFolder(server));

        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(openServerFolderButton);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(openPluginsFolderButton);

        JComponent consoleComponent = server.getServerConsoleComponent();
        if (consoleComponent != null) {
            infoPanel.add(Box.createRigidArea(new Dimension(0, 12)));
            consoleComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoPanel.add(consoleComponent);
        }

        return infoPanel;
    }

    private static void openServerFolder(ServerInstance server) {
        Path serverDir = server.getServerDirPath();
        if (serverDir == null) {
            Messages.showWarningDialog("Server directory is not available yet.", "Cannot Open Server Folder");
            return;
        }
        openDirectory(serverDir, "Server Folder");
    }

    private static void openPluginsFolder(ServerInstance server) {
        Path serverDir = server.getServerDirPath();
        if (serverDir == null) {
            Messages.showWarningDialog("Server directory is not available yet.", "Cannot Open Plugins Folder");
            return;
        }

        Path pluginsDir = serverDir.resolve("plugins");
        openDirectory(pluginsDir, "Plugins Folder");
    }

    private static void openDirectory(Path directory, String description) {
        try {
            if (!Files.exists(directory)) {
                Messages.showWarningDialog(description + " (" + directory + ") does not exist yet.", "Cannot Open " + description);
                return;
            }

            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Messages.showWarningDialog("Opening folders is not supported on this platform.", "Cannot Open " + description);
                return;
            }

            Desktop.getDesktop().open(directory.toFile());
        } catch (Exception ex) {
            Messages.showErrorDialog("Failed to open " + description + ": " + ex.getMessage(), "Cannot Open " + description);
        }
    }

    private static void waitForServerState(ServerInstance server, boolean shouldBeRunning) {
        for (int attempt = 0; attempt < SERVER_STATE_POLL_ATTEMPTS; attempt++) {
            if (server.isServerRunning() == shouldBeRunning) {
                return;
            }
            try {
                Thread.sleep(SERVER_STATE_POLL_INTERVAL_MS);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
