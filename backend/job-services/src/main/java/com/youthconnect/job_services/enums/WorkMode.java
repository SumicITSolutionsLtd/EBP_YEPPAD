package com.youthconnect.job_services.enums;

/**
 * Work Mode Enumeration
 * LinkedIn-style work location types
 */
public enum WorkMode {
    REMOTE("Remote - Work from anywhere"),
    ONSITE("On-site - Physical office location"),
    HYBRID("Hybrid - Mix of remote and on-site");

    private final String displayName;

    WorkMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
