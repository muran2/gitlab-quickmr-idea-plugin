package com.github.novotnyr.idea.gitlab.quickmr;

import com.github.novotnyr.idea.git.GitService;
import com.github.novotnyr.idea.gitlab.GitLab;
import com.github.novotnyr.idea.gitlab.MergeRequestRequest;
import com.github.novotnyr.idea.gitlab.MergeRequestResponse;
import com.github.novotnyr.idea.gitlab.quickmr.settings.Settings;
import com.github.novotnyr.idea.gitlab.quickmr.settings.SettingsUi;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class CreateMergeRequestAction extends AnAction {
    private final GitService gitService = ServiceManager.getService(GitService.class);

    @Override
    public void actionPerformed(AnActionEvent event) {
        SelectedModule selectedModule = SelectedModule.fromEvent(event);
        if (selectedModule == null) {
            return;
        }

        Project project = event.getProject();
        Settings settings = ServiceManager.getService(project, Settings.class);
        if (!settings.isInitialized()) {
            Notification notification = new Notification("quickmr", "Quick Merge Request are not configured",
                    "Quick Merge Requests are not configured<br/> <a href='link'>Configure</a>",
                    NotificationType.INFORMATION,
                    new NotificationListener() {
                        @Override
                        public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent hyperlinkEvent) {
                            ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingsUi.class);
                            notification.expire();
                        }
                    }
            );
            Notifications.Bus.notify(notification);
            return;
        }

        GitLab gitLab = new GitLab(settings.getGitLabUri(), settings.getAccessToken());

        MergeRequestRequest requestRequest = new MergeRequestRequest();
        requestRequest.setSourceBranch(getSourceBranch(selectedModule));
        requestRequest.setTargetBranch(settings.getDefaultTargetBranch());
        requestRequest.setAssigneeId(settings.getDefaultAssigneeId());
        requestRequest.setTitle(settings.getDefaultTitle());

        String projectName = getProjectName(selectedModule);

        gitLab.createMergeRequest(projectName, requestRequest)
                .thenAccept(mergeRequestResponse -> createNotification(mergeRequestResponse, project, settings))
                .exceptionally(t -> createErrorNotification(t));
    }

    private String getProjectName(SelectedModule selectedModule) {
        String projectGitUrl = this.gitService.getProjectGitUrl(selectedModule);
        if (projectGitUrl == null) {
            return null;
        }
        return this.gitService.getRepoPathWithoutDotGit(projectGitUrl);
    }

    @NotNull
    private String getSourceBranch(SelectedModule selectedModule) {
        return this.gitService.getCurrentBranch(selectedModule);
    }

    private void createNotification(MergeRequestResponse mergeRequestResponse, Project project, Settings settings) {
        String message = "Merge Request created";

        Notification notification = new Notification("quickmr", "Merge Request Created",
                "Merge Request created<br/><a href='mr'>View in GitLab</a>",
                NotificationType.INFORMATION,
                new NotificationListener() {
                    @Override
                    public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent hyperlinkEvent) {
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.browse(new URI(mergeRequestResponse.getWebUrl()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        Notifications.Bus.notify(notification);
    }

    private Void createErrorNotification(Throwable t) {
        Notification notification = new Notification("quickmr", "Merge Request Failed", "Failed to create merge request: " + t
                .getMessage(), NotificationType.ERROR);

        Notifications.Bus.notify(notification);
        return null;
    }


}