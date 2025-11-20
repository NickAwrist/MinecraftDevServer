package com.nickawrist.minecraftdevserver.dialogs.newServerDialog;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.nickawrist.minecraftdevserver.backend.apis.PaperApi;
import com.nickawrist.minecraftdevserver.backend.models.PaperVersions;

import javax.swing.*;

public class PaperVersionLabeledComponentFactory {

    private ComboBox<String> versionComboBox;
    private LabeledComponent<ComboBox<String>> labeledComponent;

    public LabeledComponent<ComboBox<String>> create() {

        versionComboBox = new ComboBox<>();
        versionComboBox.addItem("Loading versions...");

        labeledComponent = LabeledComponent.create(
                versionComboBox,
                "Paper version:"
        );
        labeledComponent.setVisible(false);

        loadVersionsAsync();

        return labeledComponent;
    }

    public String getSelectedVersion() {
        return (String) versionComboBox.getSelectedItem();
    }

    private void loadVersionsAsync() {
        SwingUtilities.invokeLater(() -> {
            try {
                PaperVersions paperVersions = PaperApi.getPaperVersions();
                if(paperVersions == null) { return; }

                String[] versions = paperVersions.versions();
                versionComboBox.removeAllItems();
                for (int i = versions.length - 1; i >= 0; i--) {
                    versionComboBox.addItem(versions[i]);
                }
                labeledComponent.setVisible(true);
            } catch (Exception e) {
                versionComboBox.removeAllItems();
                versionComboBox.addItem("Error loading versions");
            }
        });
    }

}
