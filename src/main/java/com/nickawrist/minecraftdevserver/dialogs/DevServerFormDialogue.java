package com.nickawrist.minecraftdevserver.dialogs;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.nickawrist.minecraftdevserver.backend.apis.DownloadPaperServer;
import com.nickawrist.minecraftdevserver.backend.apis.PaperVersionsBackend;
import com.nickawrist.minecraftdevserver.backend.apis.PaperBuildsBackend;
import com.nickawrist.minecraftdevserver.backend.models.PaperVersions;
import com.nickawrist.minecraftdevserver.backend.models.PaperBuild;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class DevServerFormDialogue extends DialogWrapper {

    private static final Logger LOG = Logger.getInstance(DevServerFormDialogue.class);

    private final Project project;
    private JTextField serverNameField;
    private ComboBox<String> serverVersionComboBox;
    private JLabel serverBuildLabel;
    private ComboBox<String> serverBuildComboBox;
    private PaperBuild[] currentBuilds;

    public DevServerFormDialogue(@Nullable Project project) {
        super(project);
        this.project = project;
        setTitle("Create Dev Server");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);

        // Server Name Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel serverNameLabel = new JLabel("Server Name:");
        mainPanel.add(serverNameLabel, gbc);

        // Server Name Field
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        serverNameField = new JTextField(20);
        mainPanel.add(serverNameField, gbc);

        // Server Version Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel serverVersionLabel = new JLabel("Server Version:");
        mainPanel.add(serverVersionLabel, gbc);

        // Server Version ComboBox
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        serverVersionComboBox = new ComboBox<>();
        serverVersionComboBox.addItem("Loading versions...");
        mainPanel.add(serverVersionComboBox, gbc);

        // Server Build Label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        serverBuildLabel = new JLabel("Server Build:");
        serverBuildLabel.setVisible(false);
        mainPanel.add(serverBuildLabel, gbc);

        // Server Build ComboBox
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        serverBuildComboBox = new ComboBox<>();
        serverBuildComboBox.setVisible(false);
        mainPanel.add(serverBuildComboBox, gbc);

        // Add listener to version combo box
        serverVersionComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedVersion = (String) e.getItem();
                if (selectedVersion != null && !selectedVersion.equals("Loading versions...")
                    && !selectedVersion.equals("Error loading versions")) {
                    loadBuildsAsync(selectedVersion);
                }
            }
        });

        // Load versions in the background
        loadVersionsAsync();

        return mainPanel;
    }

    private void loadVersionsAsync() {
        SwingUtilities.invokeLater(() -> {
            try {
                PaperVersions paperVersions = PaperVersionsBackend.getPaperVersions();
                String[] versions = paperVersions.versions();

                serverVersionComboBox.removeAllItems();

                // Add versions in reverse order (newest first)
                for (int i = versions.length - 1; i >= 0; i--) {
                    serverVersionComboBox.addItem(versions[i]);
                }
            } catch (Exception e) {
                serverVersionComboBox.removeAllItems();
                serverVersionComboBox.addItem("Error loading versions");
                LOG.error("Failed to load Paper versions", e);
            }
        });
    }

    private void loadBuildsAsync(String version) {
        // Hide and clear the build combo box while loading
        serverBuildComboBox.removeAllItems();
        serverBuildComboBox.addItem("Loading builds...");
        serverBuildLabel.setVisible(true);
        serverBuildComboBox.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            try {
                PaperBuild[] builds = PaperBuildsBackend.getPaperBuilds(version);
                currentBuilds = builds; // Store builds for later use

                serverBuildComboBox.removeAllItems();

                // Add builds in reverse order (newest first)
                for (int i = builds.length - 1; i >= 0; i--) {
                    serverBuildComboBox.addItem(String.valueOf(builds[i].build()));
                }
            } catch (Exception e) {
                currentBuilds = null;
                serverBuildComboBox.removeAllItems();
                serverBuildComboBox.addItem("Error loading builds");
                LOG.error("Failed to load Paper builds for version " + version, e);
            }
        });
    }

    public String getServerName() {
        return serverNameField.getText();
    }

    public String getServerVersion() {
        Object selectedItem = serverVersionComboBox.getSelectedItem();
        return selectedItem != null ? selectedItem.toString() : "";
    }

    public String getServerBuild() {
        Object selectedItem = serverBuildComboBox.getSelectedItem();
        return selectedItem != null ? selectedItem.toString() : "";
    }

    @Override
    protected void doOKAction() {
        // Get the project root directory
        if (project == null) {
            LOG.error("Project is null, cannot download server");
            super.doOKAction();
            return;
        }

        String projectBasePath = project.getBasePath();
        if (projectBasePath == null) {
            LOG.error("Project base path is null, cannot download server");
            super.doOKAction();
            return;
        }

        // Get selected values
        String selectedVersion = getServerVersion();
        String selectedBuildNumber = getServerBuild();
        String serverName = getServerName();

        // Validate inputs
        if (selectedVersion.isEmpty() || selectedBuildNumber.isEmpty() || serverName.isEmpty()) {
            LOG.warn("Missing required fields for server creation");
            super.doOKAction();
            return;
        }

        if (selectedVersion.equals("Loading versions...") || selectedVersion.equals("Error loading versions") ||
            selectedBuildNumber.equals("Loading builds...") || selectedBuildNumber.equals("Error loading builds")) {
            LOG.warn("Invalid selection for server creation");
            super.doOKAction();
            return;
        }

        // Find the selected build
        if (currentBuilds == null) {
            LOG.error("No builds available");
            super.doOKAction();
            return;
        }

        PaperBuild selectedBuild = null;
        int buildNumber = Integer.parseInt(selectedBuildNumber);
        for (PaperBuild build : currentBuilds) {
            if (build.build() == buildNumber) {
                selectedBuild = build;
                break;
            }
        }

        if (selectedBuild == null) {
            LOG.error("Could not find build " + buildNumber);
            super.doOKAction();
            return;
        }

        // Download the server in a background thread
        PaperBuild finalSelectedBuild = selectedBuild;
        new Thread(() -> {
            try {
                LOG.info("Downloading Paper server '" + serverName + "' version " + selectedVersion + " build " + buildNumber + " to " + projectBasePath);
                DownloadPaperServer.downloadPaperServer(serverName, finalSelectedBuild, selectedVersion, projectBasePath);
                LOG.info("Successfully downloaded Paper server to " + projectBasePath + "/" + serverName);
            } catch (Exception e) {
                LOG.error("Failed to download Paper server", e);
            }
        }).start();

        super.doOKAction();
    }
}
