package com.github.epsilon.events.impl;

public class ModuleStateChangedEvent {
    private final String moduleName;
    private final boolean enabled;
    private final int notificationHash;

    public ModuleStateChangedEvent(String moduleName, boolean enabled, int notificationHash) {
        this.moduleName = moduleName;
        this.enabled = enabled;
        this.notificationHash = notificationHash;
    }

    public String getModuleName() {
        return moduleName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getNotificationHash() {
        return notificationHash;
    }
}
