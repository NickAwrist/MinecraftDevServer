package com.nickawrist.minecraftdevserver.dialogs.newServerDialog;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.nickawrist.minecraftdevserver.backend.apis.PaperApi;
import com.nickawrist.minecraftdevserver.backend.models.PaperVersions;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class PaperVersionLabeledComponentFactory {

    private ComboBox<String> versionComboBox;
    private LabeledComponent<JPanel> labeledComponent;
    private JCheckBox showPrereleasesCheckBox;

    public LabeledComponent<JPanel> create() {

        versionComboBox = new ComboBox<>();
        versionComboBox.addItem("Loading versions...");

        showPrereleasesCheckBox = new JCheckBox("Show prereleases");
        showPrereleasesCheckBox.setSelected(false);
        showPrereleasesCheckBox.addActionListener(e -> loadVersionsAsync());

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.add(versionComboBox, BorderLayout.CENTER);
        panel.add(showPrereleasesCheckBox, BorderLayout.EAST);

        labeledComponent = LabeledComponent.create(
                panel,
                "Paper version:"
        );
        labeledComponent.setVisible(false);

        loadVersionsAsync();

        return labeledComponent;
    }

    public String getSelectedVersion() {
        return (String) versionComboBox.getSelectedItem();
    }

    public ComboBox<String> getVersionComboBox() {
        return versionComboBox;
    }

    private void loadVersionsAsync() {
        SwingUtilities.invokeLater(() -> {
            try {
                PaperVersions paperVersions = PaperApi.getPaperVersions(showPrereleasesCheckBox.isSelected());
                if(paperVersions == null) { return; }

                versionComboBox.removeAllItems();
                Map<String, String[]> versionsMap = paperVersions.versions();
                List<String> versionsList = versionsMap.values().stream().flatMap(Arrays::stream).toList().reversed();
                String[] versions = versionsList.toArray(String[]::new);
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
