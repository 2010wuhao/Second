package com.joysee.tvbox.settings.upgrade.entity;

import java.util.ArrayList;
import java.util.List;

public class Versions {
    public static final String START_TAG = "Versions";
    private List<Version> versions;

    public Versions() {
        versions = new ArrayList<Version>();
    }

    public void addVersionToList(Version ver) {
        if (ver != null) {
            versions.add(ver);
        }
    }

    public List<Version> getVersions() {
        return versions;
    }
}
