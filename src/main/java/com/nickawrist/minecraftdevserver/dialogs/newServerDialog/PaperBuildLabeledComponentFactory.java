package com.nickawrist.minecraftdevserver.dialogs.newServerDialog;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.nickawrist.minecraftdevserver.backend.apis.PaperApi;
import com.nickawrist.minecraftdevserver.backend.models.PaperBuild;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;

public class PaperBuildLabeledComponentFactory {

    private ComboBox<Integer> paperBuildComboBox;
    private LabeledComponent<ComboBox<Integer>> labeledComponent;

    private HashMap<Integer, PaperBuild> buildHashMap;

    public LabeledComponent<ComboBox<Integer>> create() {
        paperBuildComboBox = new ComboBox<>();

        labeledComponent = LabeledComponent.create(
                paperBuildComboBox,
                "Paper build:"
        );
        labeledComponent.setVisible(false);

        return labeledComponent;
    }

    public int getSelectedBuild() {
        if (paperBuildComboBox.getSelectedItem() == null) {
            return -1;
        }
        return (Integer) paperBuildComboBox.getSelectedItem();
    }

    public PaperBuild getSelectedPaperBuild() {
        int selectedBuild = getSelectedBuild();
        return buildHashMap.get(selectedBuild);
    }

    public void loadBuildsAsync(String version) {
        paperBuildComboBox.removeAllItems();

        SwingUtilities.invokeLater(() -> {
            try {
                PaperBuild[] builds = PaperApi.getPaperBuilds(version);
                if(builds == null || builds.length == 0) {
                    // TODO: Add a popup notification
                    return;
                }
                buildHashMap = new HashMap<>();

                Arrays.stream(builds).forEach(build -> buildHashMap.put(build.build(), build));

                paperBuildComboBox.removeAllItems();
                for (int i = builds.length - 1; i >= 0; i--) {
                    paperBuildComboBox.addItem(builds[i].build());
                }
                labeledComponent.setVisible(true);
            } catch (Exception e) {
                paperBuildComboBox.removeAllItems();
            }
        });
    }

}
