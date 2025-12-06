package com.nickawrist.minecraftdevserver.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.nickawrist.minecraftdevserver.ServerRepository;
import com.nickawrist.minecraftdevserver.constants.PluginConstants;
import com.nickawrist.minecraftdevserver.dialogs.newServerDialog.DevServerFormDialogue;
import com.nickawrist.minecraftdevserver.models.ServerInstance;
import com.nickawrist.minecraftdevserver.window.ServerInfoView.ServerInfoViewFactory;
import org.jetbrains.annotations.NotNull;
import java.util.List;


import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.ArrayList;
import com.intellij.ui.AnimatedIcon;

public class DevServerToolWindowFactory implements ToolWindowFactory {

    private JPanel contentPanel;
    private JPanel headerPanel;
    private JButton createServerButton;
    private Timer runnerReadyTimer;
    private final ServerInfoViewFactory serverInfoViewFactory = new ServerInfoViewFactory();
    private ServerRepository.RepositoryChangeListener repositoryChangeListener;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        PluginConstants.project = project;

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

        // Subscribe to repository changes to auto-refresh
        repositoryChangeListener = () -> SwingUtilities.invokeLater(this::showServerListView);
        ServerRepository.getInstance().addChangeListener(repositoryChangeListener);

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
                showServerListView();
            }
        });
        return openCreationDialogButton;
    }

    private void showServerListView() {
        stopRunnerPolling();
        // Clear content panel
        contentPanel.removeAll();

        // Remove back button from header if present
        Component[] headerComponents = headerPanel.getComponents();
        for (Component comp : headerComponents) {
            if (comp instanceof JButton) {
                headerPanel.remove(comp);
            }
        }

        List<ServerInstance> servers = ServerRepository.getInstance().getServers();
        if (!servers.isEmpty()) {
            JPanel buttonListPanel = new JPanel();
            buttonListPanel.setLayout(new BoxLayout(buttonListPanel, BoxLayout.Y_AXIS));
            buttonListPanel.setBorder(JBUI.Borders.empty(8));

            List<ServerListEntry> pendingEntries = new ArrayList<>();

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

                // Add right-click context menu for deleting servers
                JPopupMenu contextMenu = new JPopupMenu();
                JMenuItem deleteItem = new JMenuItem("Delete Server");
                deleteItem.addActionListener(e -> {
                    int result = Messages.showYesNoDialog(
                            "Are you sure you want to delete the server '" + server.getServerName() + "'?\nThis will also delete all server files.",
                            "Delete Server",
                            Messages.getWarningIcon()
                    );
                    if (result == Messages.YES) {
                        if (server.hasServerRunner() && server.isServerRunning()) {
                            server.stopServer();
                        }
                        ServerRepository.getInstance().removeServer(server.getUuid());
                    }
                });
                contextMenu.add(deleteItem);
                serverButton.setComponentPopupMenu(contextMenu);

                JPanel rowPanel = new JPanel(new GridBagLayout());
                rowPanel.setOpaque(false);
                rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                rowPanel.setBorder(JBUI.Borders.empty());

                GridBagConstraints buttonConstraints = new GridBagConstraints();
                buttonConstraints.gridx = 0;
                buttonConstraints.gridy = 0;
                buttonConstraints.weightx = 1.0;
                buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
                rowPanel.add(serverButton, buttonConstraints);

                JComponent loadingIndicator;
                if (!server.hasServerRunner()) {
                    serverButton.setEnabled(false);
                    loadingIndicator = createLoadingIndicator();

                    GridBagConstraints loadingConstraints = new GridBagConstraints();
                    loadingConstraints.gridx = 1;
                    loadingConstraints.gridy = 0;
                    loadingConstraints.insets = JBUI.insetsLeft(6);
                    loadingConstraints.anchor = GridBagConstraints.CENTER;
                    rowPanel.add(loadingIndicator, loadingConstraints);

                    pendingEntries.add(new ServerListEntry(server, serverButton, loadingIndicator));
                }

                int rowHeight = serverButton.getPreferredSize().height + JBUI.scale(2);
                rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));

                buttonListPanel.add(rowPanel);
                buttonListPanel.add(Box.createRigidArea(new Dimension(0, 2)));
            }

            startRunnerPolling(pendingEntries);

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
        stopRunnerPolling();
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

        // Create and show the server info view using the factory
        JComponent serverInfoView = serverInfoViewFactory.createServerInfoPanel(server);
        contentPanel.add(serverInfoView, BorderLayout.CENTER);

        // Hide create button in info view
        createServerButton.setVisible(false);

        // Refresh the UI
        headerPanel.revalidate();
        headerPanel.repaint();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JComponent createLoadingIndicator() {
        JPanel loadingPanel = new JPanel();
        loadingPanel.setOpaque(false);
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.X_AXIS));
        JLabel spinner = new JLabel(new AnimatedIcon.Default());
        spinner.setAlignmentY(Component.CENTER_ALIGNMENT);
        spinner.setBorder(JBUI.Borders.emptyRight(4));
        JLabel label = new JLabel("Preparing...");
        label.setAlignmentY(Component.CENTER_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 11f));
        loadingPanel.add(spinner);
        loadingPanel.add(label);
        loadingPanel.setMaximumSize(new Dimension(120, JBUI.scale(24)));
        return loadingPanel;
    }

    private void startRunnerPolling(List<ServerListEntry> pendingEntries) {
        if (pendingEntries.isEmpty()) {
            runnerReadyTimer = null;
            return;
        }
        runnerReadyTimer = new Timer(500, e -> {
            boolean hasPending = false;
            for (ServerListEntry entry : pendingEntries) {
                if (entry.server.hasServerRunner()) {
                    if (!entry.button.isEnabled()) {
                        entry.button.setEnabled(true);
                    }
                    if (entry.loadingIndicator.isVisible()) {
                        entry.loadingIndicator.setVisible(false);
                    }
                } else {
                    hasPending = true;
                }
            }
            if (!hasPending) {
                stopRunnerPolling();
            }
            contentPanel.revalidate();
            contentPanel.repaint();
        });
        runnerReadyTimer.start();
    }

    private void stopRunnerPolling() {
        if (runnerReadyTimer != null) {
            runnerReadyTimer.stop();
            runnerReadyTimer = null;
        }
    }

    private record ServerListEntry(ServerInstance server, JButton button, JComponent loadingIndicator) {
    }
}
