package com.github.muran2.idea.gitlab.quickmr;

public class SettingsNotInitializedException extends RuntimeException {

    public SettingsNotInitializedException() {
        super("Settings were not initialized in Configuration");
    }

}