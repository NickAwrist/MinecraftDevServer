package com.nickawrist.minecraftdevserver.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.nickawrist.minecraftdevserver.models.ServerInstance;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import com.intellij.openapi.diagnostic.Logger;

public class MoveJarAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(MoveJarAction.class);

    private final ServerInstance serverInstance;

    public MoveJarAction(ServerInstance serverInstance) {
        super(serverInstance.getServerName() + "-" + serverInstance.getServerVersion());
        this.serverInstance = serverInstance;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null || !virtualFile.exists()) {
            return;
        }

        File sourceFile = new File(virtualFile.getPath());
        File destDir = serverInstance.getServerDirPath().resolve("plugins").toFile();
        File destFile = new File(destDir, sourceFile.getName());

        LOG.info("Moving jar file from " + sourceFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());

        try {
            if (!destDir.exists()) {
                LOG.info("Destination plugins directory does not exist: " + destDir.getAbsolutePath());
                return;
            }

            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Successfully moved jar file to plugins directory");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to move jar file to plugins directory", ex);
        }
    }

}
