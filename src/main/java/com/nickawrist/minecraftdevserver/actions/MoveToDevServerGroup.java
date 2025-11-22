package com.nickawrist.minecraftdevserver.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.nickawrist.minecraftdevserver.ServerRepository;
import com.nickawrist.minecraftdevserver.models.ServerInstance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MoveToDevServerGroup extends ActionGroup {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public AnAction @NotNull [] getChildren(AnActionEvent e) {
        List<ServerInstance> servers = ServerRepository.getServers();
        AnAction[] actions = new AnAction[servers.size()];
        for (int i = 0; i < servers.size(); i++) {
            ServerInstance serverInstance = servers.get(i);
            actions[i] = new MoveJarAction(serverInstance);
    }
        return actions;
    }

    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

        boolean isJar = file != null && "jar".equals(file.getExtension());

        e.getPresentation().setVisible(isJar);
        e.getPresentation().setEnabled(isJar);
    }


}
