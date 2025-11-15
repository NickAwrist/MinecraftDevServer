package com.nickawrist.minecraftdevserver.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.nickawrist.minecraftdevserver.ServerRepository;
import com.nickawrist.minecraftdevserver.dialogs.DevServerFormDialogue;
import com.nickawrist.minecraftdevserver.models.ServerInstance;
import org.jetbrains.annotations.NotNull;
import java.util.List;


import javax.swing.*;
import java.awt.*;

public class DevServerToolWindowFactory implements ToolWindowFactory {

    private JPanel contentPanel;
    private JPanel headerPanel;
    private JButton createServerButton;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Header panel with title and optional back button
        headerPanel = new JPanel(new BorderLayout());
        JLabel toolWindowTitle = new JLabel("Servers");
        headerPanel.add(toolWindowTitle, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content panel that will switch between views
        contentPanel = new JPanel(new BorderLayout());
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Create Server button at bottom
        createServerButton = getOpenCreationDialogButton(project);
        mainPanel.add(createServerButton, BorderLayout.SOUTH);

        // Initialize with server list view
        showServerListView();

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);

        toolWindow.getContentManager().addContent(content);
    }

    private @NotNull JButton getOpenCreationDialogButton(@NotNull Project project) {
        JButton openCreationDialogButton = new JButton("Create New Server");
        openCreationDialogButton.addActionListener(e -> {
            DevServerFormDialogue devServerFormDialogue = new DevServerFormDialogue(project);
            devServerFormDialogue.show();
            if (devServerFormDialogue.isOK()) {
                String serverName = devServerFormDialogue.getServerName();
                String serverVersion = devServerFormDialogue.getServerVersion();

                ServerRepository.createServer(serverName, serverVersion);
                showServerListView();
            }
        });
        return openCreationDialogButton;
    }

    private void showServerListView() {
        // Clear content panel
        contentPanel.removeAll();

        // Remove back button from header if present
        Component[] headerComponents = headerPanel.getComponents();
        for (Component comp : headerComponents) {
            if (comp instanceof JButton) {
                headerPanel.remove(comp);
            }
        }

        List<ServerInstance> servers = ServerRepository.getServers();
        if (!servers.isEmpty()) {
            JPanel buttonListPanel = new JPanel();
            buttonListPanel.setLayout(new BoxLayout(buttonListPanel, BoxLayout.Y_AXIS));

            for (ServerInstance server : servers) {
                JButton serverButton = new JButton(server.getServerName() + " - " + server.getServerVersion());
                serverButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
                serverButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                serverButton.setHorizontalAlignment(SwingConstants.LEFT);
                serverButton.setMargin(JBUI.insets(2, 8));
                serverButton.setFocusPainted(false);
                serverButton.setBorderPainted(true);
                serverButton.setContentAreaFilled(true);
                serverButton.addActionListener(e -> showServerInfoView(server));
                buttonListPanel.add(serverButton);
                buttonListPanel.add(Box.createRigidArea(new Dimension(0, 2)));
            }

            JScrollPane scrollPane = new JBScrollPane(buttonListPanel);
            contentPanel.add(scrollPane, BorderLayout.CENTER);
        } else {
            JLabel emptyStateLabel = new JLabel("Create a new server to get started!", SwingConstants.CENTER);
            contentPanel.add(emptyStateLabel, BorderLayout.CENTER);
        }

        // Show create button
        createServerButton.setVisible(true);

        // Refresh the UI
        headerPanel.revalidate();
        headerPanel.repaint();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showServerInfoView(ServerInstance server) {
        // Clear content panel
        contentPanel.removeAll();

        // Add back button to header
        JButton backButton = new JButton("← Back");
        backButton.addActionListener(e -> showServerListView());

        // Remove any existing back button first
        Component[] headerComponents = headerPanel.getComponents();
        for (Component comp : headerComponents) {
            if (comp instanceof JButton) {
                headerPanel.remove(comp);
            }
        }
        headerPanel.add(backButton, BorderLayout.EAST);

        // Create info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("Server Name: " + server.getServerName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel versionLabel = new JLabel("Version: " + server.getServerVersion());
        versionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel uuidLabel = new JLabel("UUID: " + server.getUuid().toString());
        uuidLabel.setFont(uuidLabel.getFont().deriveFont(11f));
        uuidLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(versionLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(uuidLabel);

        contentPanel.add(infoPanel, BorderLayout.CENTER);

        // Hide create button in info view
        createServerButton.setVisible(false);

        // Refresh the UI
        headerPanel.revalidate();
        headerPanel.repaint();
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
