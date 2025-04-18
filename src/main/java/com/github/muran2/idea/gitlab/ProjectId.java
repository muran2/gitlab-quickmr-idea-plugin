package com.github.muran2.idea.gitlab;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ProjectId {
    private final String projectId;

    private ProjectId(String projectId) {
        this.projectId = projectId;
    }

    protected static ProjectId of(String projectId) {
        return new ProjectId(projectId);
    }

    public static ProjectId of(long projectId) {
        return new ProjectId(Long.toString(projectId));
    }

    @Override
    public String toString() {
        return this.projectId;
    }

    public String getUrlEncoded() {
        return URLEncoder.encode(this.projectId, StandardCharsets.UTF_8);
    }
}