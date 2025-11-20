package com.nickawrist.minecraftdevserver.dialogs.newServerDialog;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.util.ui.JBUI;
import com.nickawrist.minecraftdevserver.backend.models.PaperBuild;
import com.nickawrist.minecraftdevserver.backend.utils.JarInstaller;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class DevServerFormDialogue extends DialogWrapper {

    private static final Logger LOG = Logger.getInstance(DevServerFormDialogue.class);

    private JTextField serverNameField;

    private PaperVersionLabeledComponentFactory paperVersionLabeledComponentFactory;
    private PaperBuildLabeledComponentFactory paperBuildLabeledComponentFactory;

    public DevServerFormDialogue(@Nullable Project project) {
        super(project);
        setTitle("Create Dev Server");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        super.setOKActionEnabled(false);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);

        // TODO: Wrap server name in a LabeledComponent
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

        paperVersionLabeledComponentFactory = new PaperVersionLabeledComponentFactory();
        LabeledComponent<ComboBox<String>> paperVersionLabeledComponent = paperVersionLabeledComponentFactory.create();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainPanel.add(paperVersionLabeledComponent, gbc);
        gbc.gridwidth = 1;

        paperBuildLabeledComponentFactory = new PaperBuildLabeledComponentFactory();
        LabeledComponent<ComboBox<Integer>> paperBuildLabeledComponent = paperBuildLabeledComponentFactory.create();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainPanel.add(paperBuildLabeledComponent, gbc);
        gbc.gridwidth = 1;

        // Add listener to version combo box
        paperVersionLabeledComponent.getComponent().addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedVersion = paperVersionLabeledComponentFactory.getSelectedVersion();
                if (selectedVersion != null && !selectedVersion.isEmpty()) {
                    paperBuildLabeledComponentFactory.loadBuildsAsync(selectedVersion);

                    refreshOKButtonState();
                }
            }
        });

        paperBuildLabeledComponent.getComponent().addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                refreshOKButtonState();
            }
        });

        // Add listener to server name field
        serverNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshOKButtonState();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshOKButtonState();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refreshOKButtonState();
            }
        });



        return mainPanel;
    }

    public String getServerName() {
        return serverNameField.getText();
    }

    public String getServerVersion() {
        return paperVersionLabeledComponentFactory.getSelectedVersion();
    }

    public int getServerBuild() {
        return paperBuildLabeledComponentFactory.getSelectedBuild();
    }

    private void refreshOKButtonState() {
        super.setOKActionEnabled(validateInputs());
    }

    private boolean validateInputs() {
        String serverName = getServerName();
        String serverVersion = getServerVersion();
        int serverBuild = getServerBuild();

        return !(serverName.isEmpty() || serverVersion.isEmpty() || serverBuild < 0);
    }

    @Override
    protected void doOKAction() {
        // Get selected values
        String selectedVersion = paperVersionLabeledComponentFactory.getSelectedVersion();
        int selectedBuildNumber = getServerBuild();
        String serverName = getServerName();

        // Validate inputs
        if (selectedVersion.isEmpty() || selectedBuildNumber < 0 || serverName.isEmpty()) {
            LOG.warn("Missing required fields for server creation");
            super.doOKAction();
            return;
        }

        PaperBuild selectedBuild = paperBuildLabeledComponentFactory.getSelectedPaperBuild();
        if (selectedBuild == null) {
            LOG.error("Could not find build " + selectedBuildNumber + " for version " + selectedVersion);
            super.doOKAction();
            return;
        }

        // Download the server in a background thread
        new Thread(() -> {
            try {
                JarInstaller.downloadPaperServer(serverName, selectedBuild, selectedVersion);
            } catch (Exception e) {
                LOG.error("Failed to download Paper server", e);
            }
        }).start();

        super.doOKAction();
    }
}
