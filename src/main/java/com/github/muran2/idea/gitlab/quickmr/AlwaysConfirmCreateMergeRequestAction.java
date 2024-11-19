package com.github.muran2.idea.gitlab.quickmr;

import com.github.muran2.idea.gitlab.MergeRequestRequest;
import com.github.muran2.idea.gitlab.quickmr.settings.Settings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.concurrent.CompletableFuture;

public class AlwaysConfirmCreateMergeRequestAction extends CreateMergeRequestAction {
    public AlwaysConfirmCreateMergeRequestAction() {
    }

    public AlwaysConfirmCreateMergeRequestAction(Icon icon) {
        super(icon);
    }

    public AlwaysConfirmCreateMergeRequestAction(@Nullable String text) {
        super(text);
    }

    public AlwaysConfirmCreateMergeRequestAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    protected CompletableFuture<Boolean> validate(MergeRequestRequest request, SelectedModule module, Settings settings) {
        return showConfirmationDialog(request, module);
    }

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        Settings settings = project.getService(Settings.class);
        e.getPresentation().setEnabledAndVisible(!settings.isAssigneesEnabled() && !settings.isEnableMergeRequestToFavoriteAssignee());
    }
}
